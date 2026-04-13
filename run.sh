





LIB="lib_mac"

java \
  --module-path "$LIB" \
  --add-modules javafx.controls,javafx.fxml \
  -cp out \
  Main
