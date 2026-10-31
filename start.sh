
#!/usr/bin/env bash
# Starts the Player communication program.
#
# Usage:
#   ./start.sh same       Both players in one Java process (same PID)
#   ./start.sh separate   One player per Java process (separate PIDs)

set -euo pipefail

cd "$(dirname "$0")"

MODE="${1:-}"
CP="target/classes"
PORT=5000
SCRIPT="$PWD/start.sh"

# Runs a player in its own terminal window.
run_player_window() {
    local role="$1"

    java -cp "$CP" players.SeparateProcessMain "$role" "$PORT"

    echo
    read -r -p "Player finished. Press Enter to close this window..." || true
}

# Internal modes used by the separate player windows.
case "$MODE" in
    run-responder) run_player_window responder; exit 0 ;;
    run-initiator) run_player_window initiator; exit 0 ;;
esac

# Opens a new terminal window.
# Returns 1 if no supported terminal is available.
open_terminal() {
    local title="$1"
    local arg="$2"

    case "$(uname -s)" in
        Darwin)
            osascript -e \
                "tell application \"Terminal\" to do script \"bash '$SCRIPT' $arg\"" \
                >/dev/null
            ;;

        MINGW*|MSYS*|CYGWIN*)
            MSYS_NO_PATHCONV=1 cmd.exe /c start "$title" bash "$SCRIPT" "$arg"
            ;;

        *)
            if command -v gnome-terminal >/dev/null 2>&1; then
                gnome-terminal --title="$title" -- bash "$SCRIPT" "$arg"
            elif command -v konsole >/dev/null 2>&1; then
                konsole -p tabtitle="$title" -e bash "$SCRIPT" "$arg" &
            elif command -v xfce4-terminal >/dev/null 2>&1; then
                xfce4-terminal --title="$title" -x bash "$SCRIPT" "$arg"
            elif command -v xterm >/dev/null 2>&1; then
                xterm -T "$title" -e bash "$SCRIPT" "$arg" &
            elif command -v x-terminal-emulator >/dev/null 2>&1; then
                x-terminal-emulator -e bash "$SCRIPT" "$arg" &
            else
                return 1
            fi
            ;;
    esac
}

# Runs both players in the same JVM.
run_same() {
    echo
    echo "== Mode 1: both players in the same process =="
    java -cp "$CP" players.SameProcessMain
}

# Runs each player in a separate JVM.
run_separate() {
    echo
    echo "== Mode 2: one player per process =="

    if open_terminal "Player2 (responder)" run-responder; then
        sleep 1
        open_terminal "Player1 (initiator)" run-initiator

        echo "Opened two terminal windows:"
        echo "  Player2 (responder) waits for the connection."
        echo "  Player1 (initiator) starts sending messages."
        echo "Each window shows its own PID. Press Enter to close it when finished."
    else
        echo "No supported terminal found; running both players in this terminal instead."

        java -cp "$CP" players.SeparateProcessMain responder "$PORT" &
        local responder_pid=$!

        java -cp "$CP" players.SeparateProcessMain initiator "$PORT"
        wait "$responder_pid"
    fi
}

# Validate the requested mode.
case "$MODE" in
    same|separate) ;;
    *)
        echo "Usage: $0 <same|separate>" >&2
        exit 1
        ;;
esac

# Check required tools.
for tool in java mvn; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        echo "Error: '$tool' not found in PATH" >&2
        exit 1
    fi
done

# Build the project.
echo "== Building =="
mvn -q clean compile

# Start the selected mode.
case "$MODE" in
    same)     run_same ;;
    separate) run_separate ;;
esac
