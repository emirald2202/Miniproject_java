






set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

JAVAFX_PATH="lib_linux"
MODULES="javafx.controls,javafx.fxml"

echo "╔══════════════════════════════════════════╗"
echo "║     Civilian Complaint Portal — Linux    ║"
echo "╚══════════════════════════════════════════╝"


echo "[1/2] Compiling..."
rm -rf out
javac --module-path "$JAVAFX_PATH" --add-modules "$MODULES" -d out \
    Main.java \
    enums/*.java \
    exceptions/*.java \
    profile/*.java \
    users/*.java \
    complaints/*.java \
    containers/*.java \
    store/*.java \
    priority/*.java \
    search/*.java \
    threads/*.java \
    gui/*.java \
    2>&1
echo "      Compilation successful."


if [ "$1" = "--test" ]; then
    echo "[2/2] Running backend tests..."
    java --module-path "$JAVAFX_PATH" --add-modules "$MODULES" -cp out Main --test
else
    echo "[2/2] Launching GUI..."
    java --module-path "$JAVAFX_PATH" --add-modules "$MODULES" -cp out Main "$@"
fi
