#!/bin/bash
# ── Mac / Linux Run Script ──────────────────────────────────────────
# Uses lib_mac/ for JavaFX native libraries (macOS builds)
# Run from project root: bash run.sh
# ────────────────────────────────────────────────────────────────────

LIB="lib_mac"

java \
  --module-path "$LIB" \
  --add-modules javafx.controls,javafx.fxml \
  -cp out \
  Main
