#!/usr/bin/env bash
# image-compressor local runner - Linux, macOS, Windows Git Bash/MSYS.
# Windows cmd: use run-local.bat (standalone).
# Plugin JAR only - init | test | all (no start/smoke).
#
#   ./run-local.sh init | test | all
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE="image-compressor"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"

usage() {
  cat <<EOF
Local image-compressor (plugin JAR for biosdk-services)

  Linux / macOS / Git Bash:
    ./run-local.sh init | test | all

  Windows cmd:
    run-local.bat init | test | all

  init  - package this module (skip tests)
  test  - Maven unit tests
  all   - init + test

Working directory must be image-compressor/
EOF
  exit "${1:-0}"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "error: '$1' is required on PATH" >&2
    exit 1
  }
}

check_prereqs() {
  need_cmd java
  need_cmd mvn
  echo "os: ${UNAME_S}"
  local ver
  ver="$(java -version 2>&1 | head -n 1 || true)"
  echo "java: $ver"
  if ! echo "$ver" | grep -E '"21[\. "]' >/dev/null 2>&1; then
    echo "warn: JDK 21 is required. Continuing anyway." >&2
  fi
}

mvn_mod() {
  (
    cd "$MODULE_DIR"
    mvn "$@"
  )
}

cmd_init() {
  check_prereqs
  echo "==> packaging ${MODULE} (skip tests)"
  mvn_mod clean package -DskipTests "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
  echo "init complete"
  echo "  thin jar:  target/${MODULE}-*-SNAPSHOT.jar"
  echo "  fat jar:   target/${MODULE}-*-jar-with-dependencies.jar"
}

cmd_test() {
  check_prereqs
  echo "==> maven tests"
  mvn_mod test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
}

cmd_all() {
  echo "==> all: init + test"
  cmd_init
  cmd_test
}

main() {
  local cmd="${1:-}"
  case "$cmd" in
    -h|--help|help) usage 0 ;;
    init) cmd_init ;;
    test) cmd_test ;;
    all) cmd_all ;;
    "") usage 1 ;;
    *) echo "error: unknown command '$cmd'" >&2; usage 1 ;;
  esac
}

main "$@"
