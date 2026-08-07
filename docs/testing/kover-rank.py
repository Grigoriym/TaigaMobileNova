#!/usr/bin/env python3
"""Rank packages in a Kover XML report by missed branches, with the root `excludes` applied.

Why this exists: a report's class universe is whatever compiler output happens to exist on disk
(Kover's report task ends its file collection in `.existing()`), and the root aggregates each
module's *total* variant, which includes the KMP Android library target. So an Android build or a
KSP re-run leaves classes behind that a later `koverXmlReport` counts, and observed class counts
range over 742 / 781 / 787 / 798 / 821 / 854 on the same source. Re-applying the exclusion rules
here makes a ranking usable whichever compilations happened to have run. See
docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md finding 3.

Note this is *not* Kover failing to apply the `excludes` — it applies them faithfully (finding 5:
0-1 leaked classes, and the one leak is a rare intermittent Kover bug worth <=1 line). What varies
is the denominator, not the filtering.

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
    # Kover turns `packages("a.b")` into the class pattern `a.b.*`, and its `*` matches dots too
    # (`#` is its non-dot wildcard) — so a listed package covers all of its subpackages. Matching
    # by equality here kept e.g. `...core.storage.db.entities`, which the real gate excludes via
    # `...core.storage.db.*`. See docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md.
    if any(package == p or package.startswith(p + ".") for p in PACKAGES):
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
