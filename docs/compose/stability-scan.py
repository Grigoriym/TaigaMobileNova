#!/usr/bin/env python3
"""Aggregate Compose Compiler stability reports across all modules.

Requires reports to already exist (see docs/compose/stability-reports.md for how to
generate them). Walks every module's build/compose_reports/ directory, parses the
*-classes.txt and *-composables.txt files, and prints one line per unstable class or
composable parameter found.
"""

import glob
import re
import sys


def module_from_path(path: str) -> str:
    idx = path.find("/build/compose_reports/")
    return path[:idx]


def scan_classes_file(path: str) -> list[str]:
    module = module_from_path(path)
    hits = []
    current_class = None
    current_unstable_members = []

    def flush():
        if current_class and current_unstable_members:
            for member in current_unstable_members:
                hits.append(f"{module} | class {current_class} | {member}")

    with open(path) as f:
        for line in f:
            match = re.match(r"^(stable|unstable|runtime) class (\S+)", line)
            if match:
                flush()
                current_class = match.group(2)
                current_unstable_members = []
                continue
            member_match = re.match(r"^  unstable (val|var) (.+)$", line)
            if member_match:
                current_unstable_members.append(member_match.group(2))
    flush()
    return hits


def scan_composables_file(path: str) -> list[str]:
    module = module_from_path(path)
    hits = []
    current_fun = None
    current_unstable_params = []

    def flush():
        if current_fun and current_unstable_params:
            for param in current_unstable_params:
                hits.append(f"{module} | {current_fun} | {param}")

    with open(path) as f:
        for line in f:
            if re.match(r"^\S", line) and "fun " in line:
                flush()
                fun_match = re.search(r"fun (\S+)\(", line)
                current_fun = fun_match.group(1) if fun_match else line.strip()
                current_unstable_params = []
                continue
            param_match = re.match(r"^  unstable (.+)$", line)
            if param_match:
                current_unstable_params.append(param_match.group(1))
    flush()
    return hits


def main() -> int:
    class_hits = []
    for path in sorted(glob.glob("**/build/compose_reports/*-classes.txt", recursive=True)):
        class_hits.extend(scan_classes_file(path))

    composable_hits = []
    for path in sorted(glob.glob("**/build/compose_reports/*-composables.txt", recursive=True)):
        composable_hits.extend(scan_composables_file(path))

    if not class_hits and not composable_hits:
        print("No unstable classes or composable parameters found.")
        return 0

    print(f"=== Unstable classes ({len(class_hits)}) ===")
    for hit in class_hits:
        print(hit)

    print(f"\n=== Composables with unstable parameters ({len(composable_hits)}) ===")
    for hit in composable_hits:
        print(hit)

    return 0


if __name__ == "__main__":
    sys.exit(main())
