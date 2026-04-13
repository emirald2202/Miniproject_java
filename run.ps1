




$LIB = "lib_win"

$env:PATH = "$PWD\$LIB;" + $env:PATH

java `
  --module-path $LIB `
  --add-modules javafx.controls,javafx.fxml `
  -cp out `
  Main
