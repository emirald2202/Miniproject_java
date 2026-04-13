# ── Windows Run Script ──────────────────────────────────────────────
# Uses lib_win\ for JavaFX native libraries (Windows builds)
# Run from project root: .\run.ps1
# ────────────────────────────────────────────────────────────────────

$LIB = "lib_win"

$env:PATH = "$PWD\$LIB;" + $env:PATH

java `
  --module-path $LIB `
  --add-modules javafx.controls,javafx.fxml `
  -cp out `
  Main
