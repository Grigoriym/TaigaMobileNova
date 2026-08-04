#!/usr/bin/env python3
"""Rank packages in a Kover XML report by missed branches, with the root `excludes` applied.

Why this exists: `koverXmlReport` non-deterministically flips between applying the root
`build.gradle.kts` `excludes` block and ignoring it (observed class counts 742 / 821 / 854 —
see CLAUDE.md "Testing" and docs/revisit.md #8). A report from the wrong side of the flip
counts thousands of lines no test can ever move, which silently inflates any ranking taken
from it.

Rather than trying to force a 742-class run, this script re-applies the exclusion rules to
whatever report you have. On 2026-08-03 it reduced an 854-class report to 742 classes and
reproduced a genuine 742-class run's totals to the digit, so the two are interchangeable.

Caveat found 2026-08-04: on a 787-class report (a mode where the `excludes` *were* applied and the
surplus is Android-variant / Room classes) it stops at 745, because the three
`com.grappim.taigamobile.core.storage.db.entities` classes are named by neither the root `excludes`
nor the lists below. BRANCH totals still match the gate exactly; LINE runs ~53 lines high. See
docs/revisit.md #8 — fix both lists together if that entry is ever addressed.

Usage:
    ./gradlew koverXmlReport
    python3 docs/testing/kover-rank.py build/reports/kover/report.xml

Keep the suffix/package lists below in sync with the `kover { reports { filters { excludes` }}}
block in the root build.gradle.kts.
"""

import collections
import sys
import xml.etree.ElementTree as ET

SUFFIXES = [
    "Api", "ApiImpl", "DTO", "Repository",
    "Delegate", "Plugin", "Module",
    "TimberLogger", "PagingSource", "Exception",
    "App", "Desktop", "Activity",
    "DrawerDestination", "IconSource",
    "UI", "Widget", "Screen", "Dialog", "BottomSheet",
    "Destination", "NavigationExtensions", "Graph", "NavHost",
    "ComposableSingletons",
    "ImmutableListSerializer", "BuildKonfig",
]
EXTRA_SUFFIXES = ["ResultExtensionKt", "TryCatchExtensionsKt", "ApiConstants", "BuildConfig"]
PACKAGES = [
    "com.grappim.taigamobile.strings.generated.resources",
    "com.grappim.taigamobile.core.storage.db",
    "com.grappim.taigamobile.core.storage.db.dao",
    "com.grappim.taigamobile.core.storage.db.wrapper",
    "com.grappim.taigamobile.core.storage.di",
    "com.grappim.taigamobile.core.storage.network",
    "com.grappim.taigamobile.core.storage.cache",
]


def is_excluded(package: str, class_name: str) -> bool:
    if package in PACKAGES:
        return True
    # Kover's suffix match is on the outer class, so strip the `Foo$Bar` nesting first.
    base = class_name.split("/")[-1].split("$")[0]
    if any(base.endswith(s) or base.endswith(s + "Kt") for s in SUFFIXES):
        return True
    if any(base.endswith(s) for s in EXTRA_SUFFIXES):
        return True
    return "Preferences" in base


def main(path: str, top: int = 25) -> None:
    root = ET.parse(path).getroot()
    rows = collections.defaultdict(lambda: [0, 0, 0, 0])  # branch cov/total, line cov/total
    kept = 0
    for package in root.findall("package"):
        name = package.get("name").replace("/", ".")
        for clazz in package.findall("class"):
            if is_excluded(name, clazz.get("name")):
                continue
            kept += 1
            row = rows[name]
            for counter in clazz.findall("counter"):
                covered = int(counter.get("covered"))
                total = covered + int(counter.get("missed"))
                if counter.get("type") == "BRANCH":
                    row[0] += covered
                    row[1] += total
                elif counter.get("type") == "LINE":
                    row[2] += covered
                    row[3] += total

    branch = (sum(r[0] for r in rows.values()), sum(r[1] for r in rows.values()))
    line = (sum(r[2] for r in rows.values()), sum(r[3] for r in rows.values()))
    print(f"classes kept: {kept}  (raw report had {len(root.findall('.//class'))})")
    print(f"BRANCH {branch[0]}/{branch[1]} {100 * branch[0] / branch[1]:.2f}%   "
          f"LINE {line[0]}/{line[1]} {100 * line[0] / line[1]:.2f}%\n")
    print(f"{'package':66} {'missedB':>8} {'branch':>13} {'line':>13}")
    for name, r in sorted(rows.items(), key=lambda kv: -(kv[1][1] - kv[1][0]))[:top]:
        print(f"{name:66} {r[1] - r[0]:8} {r[0]:5}/{r[1]:<7} {r[2]:5}/{r[3]:<7}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "build/reports/kover/report.xml")
