#!/usr/bin/env bash
#
# Fails a commit that weakens one of this repo's gates without saying so out loud.
#
# The gates (detekt, ktlint, the tests, the Kover floor, CI) constrain the code a session writes.
# Nothing constrains a session from widening a gate so its own step passes. This does not prevent
# that — it makes it impossible to do quietly: a commit that trips a wire has to carry a
#
#   Gate-change: <what was widened, and why>
#
# line in its message. The wires are chosen so ordinary feature work never touches them.
#
# Usage: check-guardrails.sh [<range>]     (default HEAD~1..HEAD)

set -euo pipefail

MARKER='Gate-change:'
EMPTY_TREE='4b825dc642cb6eb9a060e54bf8d69288fbee4904'
RANGE="${1:-HEAD~1..HEAD}"

# Files that ordinary feature work has no reason to touch: the build's own rules.
TRIPWIRE_PATHS=(
  '.github/'
  'build-logic/'
  'config/detekt/'
  '.editorconfig'
  'gradle/libs.versions.toml'
)

failed=0

# --no-merges: on a pull_request run, actions/checkout defaults to the synthetic
# refs/pull/N/merge commit, which carries the PR's whole diff under an auto-generated
# message that can never carry a Gate-change: trailer. Skipping merges means only the
# real, author-written commits — which can carry the trailer — are checked.
for sha in $(git rev-list --reverse --no-merges "$RANGE"); do
  base="$(git rev-parse --verify --quiet "${sha}^" || echo "$EMPTY_TREE")"
  subject="$(git log -1 --format=%s "$sha")"
  reasons=()

  while IFS= read -r file; do
    [ -n "$file" ] || continue
    for wire in "${TRIPWIRE_PATHS[@]}"; do
      case "$wire" in
        */) [ "${file##"$wire"}" != "$file" ] && reasons+=("touches $file") ;;
        *)  [ "$file" = "$wire" ] && reasons+=("touches $file") ;;
      esac
    done
  done <<<"$(git diff --name-only "$base" "$sha")"

  added="$(git diff "$base" "$sha" -- '*.kt' '*.kts' | grep '^+' | grep -v '^+++' || true)"
  grep -qE '@Ignore\b' <<<"$added" && reasons+=('adds an @Ignore — a test that no longer runs')
  grep -qE '@Suppress\(' <<<"$added" && reasons+=('adds a @Suppress — a lint rule that no longer applies')

  [ ${#reasons[@]} -eq 0 ] && continue

  if git log -1 --format=%B "$sha" | grep -q "^${MARKER}"; then
    echo "ok   ${sha:0:7} ${subject} — declared, ${#reasons[@]} tripwire(s)"
    continue
  fi

  failed=1
  echo
  echo "FAIL ${sha:0:7} ${subject}"
  printf '       - %s\n' "${reasons[@]}"
done

if [ "$failed" -ne 0 ]; then
  cat <<EOF

A commit above changes something that decides whether other commits pass.
That is allowed — silently is not. Say so in the commit message:

    ${MARKER} <what was widened, and why>

If the change was not deliberate, reverting that file is the other way out.
EOF
  exit 1
fi

echo "Guardrails: nothing tripped in ${RANGE}."
