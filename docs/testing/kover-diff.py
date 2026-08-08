#!/usr/bin/env python3
"""Diff every <package>/<class> counter between two Kover XML reports.

Why this exists: a before/after coverage comparison is only valid if nothing outside the
target moved. koverXmlReport's non-deterministic excludes flip (see CLAUDE.md "Testing" and
docs/revisit.md #8) means two reports from the same tree can differ by hundreds of classes
with mismatched denominators — reading two dumps by eye in that situation has produced both
a false regression and a false "nothing changed" before. This script answers "did anything
besides what I touched move?" directly instead.

It loads {(name, type): (covered, total)} for every <package> and <class> element in both
reports, then prints — split by element type, because the excludes flip only ever disturbs
`<package>` totals (dropped/added classes leave the key set rather than moving a value; a
`<class>` row that moves is real):

  - the symmetric difference of the key sets (present in one report but not the other)
  - keys present in both whose denominator (covered+missed) differs
  - keys present in both, same denominator, whose covered count differs

A clean run shows "identical" at both levels. A straddled pair (different excludes mode)
typically shows package-level noise and an empty class-level diff outside your target —
read the class-level list as the actual answer.

Usage:
    ./gradlew koverXmlReport && cp build/reports/kover/report.xml /tmp/before.xml
    # ... make your change ...
    ./gradlew koverXmlReport && cp build/reports/kover/report.xml /tmp/after.xml
    python3 docs/testing/kover-diff.py /tmp/before.xml /tmp/after.xml
"""

import sys
import xml.etree.ElementTree as ET


def load(path):
    """Returns (package_counters, class_counters), each {(name, type): (covered, total)}."""
    root = ET.parse(path).getroot()
    packages = {}
    classes = {}
    for package in root.findall("package"):
        pkg_name = package.get("name").replace("/", ".")
        for counter in package.findall("counter"):
            covered = int(counter.get("covered"))
            total = covered + int(counter.get("missed"))
            packages[(pkg_name, counter.get("type"))] = (covered, total)
        for clazz in package.findall("class"):
            class_name = clazz.get("name").replace("/", ".")
            for counter in clazz.findall("counter"):
                covered = int(counter.get("covered"))
                total = covered + int(counter.get("missed"))
                classes[(class_name, counter.get("type"))] = (covered, total)
    return packages, classes


def diff(before: dict, after: dict, label: str, limit: int = 20) -> None:
    before_keys, after_keys = set(before), set(after)
    only_before = before_keys - after_keys
    only_after = after_keys - before_keys
    common = before_keys & after_keys
    denom_changed = [k for k in common if before[k][1] != after[k][1]]
    value_changed = [
        k for k in common if k not in denom_changed and before[k][0] != after[k][0]
    ]

    print(f"--- {label} ---")
    print(
        f"keys: {len(before_keys)} before, {len(after_keys)} after, "
        f"symmetric difference {len(only_before) + len(only_after)}"
    )
    if only_before:
        print(f"  only in before ({len(only_before)}):")
        for name, typ in sorted(only_before)[:limit]:
            print(f"    {name} [{typ}]")
    if only_after:
        print(f"  only in after ({len(only_after)}):")
        for name, typ in sorted(only_after)[:limit]:
            print(f"    {name} [{typ}]")
    if denom_changed:
        print(f"  denominator changed ({len(denom_changed)}):")
        for name, typ in sorted(denom_changed)[:limit]:
            print(f"    {name} [{typ}]  {before[(name, typ)]} -> {after[(name, typ)]}")
    if value_changed:
        print(f"  covered changed, denominator same ({len(value_changed)}):")
        for name, typ in sorted(value_changed)[:limit]:
            print(f"    {name} [{typ}]  {before[(name, typ)]} -> {after[(name, typ)]}")
    if not (only_before or only_after or denom_changed or value_changed):
        print("  identical")
    print()


def main(before_path: str, after_path: str) -> None:
    before_pkgs, before_classes = load(before_path)
    after_pkgs, after_classes = load(after_path)
    diff(before_pkgs, after_pkgs, "package-level")
    diff(before_classes, after_classes, "class-level")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit("usage: kover-diff.py <before.xml> <after.xml>")
    main(sys.argv[1], sys.argv[2])
