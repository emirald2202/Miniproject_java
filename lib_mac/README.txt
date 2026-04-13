── lib_mac — macOS JavaFX Libraries ────────────────────────────────

Place the macOS version of JavaFX SDK JARs here.

Download from: https://gluonhq.com/products/javafx/
  → Select: JavaFX 21 LTS  |  Platform: macOS  |  Type: SDK
  → Pick "aarch64" if you have Apple Silicon (M1/M2/M3)
  → Pick "x64"     if you have Intel Mac

After extracting, copy these files into this folder (lib_mac/):
  • javafx.base.jar
  • javafx.controls.jar
  • javafx.fxml.jar
  • javafx.graphics.jar
  • javafx.media.jar
  • javafx-swt.jar
  • javafx.web.jar
  + all .dylib native files

Run the app on Mac/Linux:
  bash run.sh

Or in VSCode:
  Run > "Run (Mac/Linux) — lib_mac"
