# Player Communication

Two `Player` instances exchange messages. One `Player` class, two execution models, chosen only by the channel and the launcher.

## Rules implemented
- Player1 (initiator) sends the first message, `Hello`.
- Every player, on receiving a message, replies with `received + " " + number of messages it has sent` (including that reply).
- Design decision: the counter is the number of messages the player has sent, including the reply being sent. Replies therefore grow (`Hello 1 2 2 3 3 ...`).
- A player pauses 500 ms before each send, so the exchange is readable in the terminal.
- Each player stops once it has sent 10 and received 10 messages (the initiator: 10 sent, 10 received back).

## Structure (`src/main/java/players`)
| Class | Responsibility |
|---|---|
| `MessageChannel` | Transport contract: send / receive / close |
| `QueueChannel` | In-memory transport for players in the same JVM |
| `TcpChannel` | Socket transport for players in separate JVMs |
| `Player` | The reply rule, counters, stop condition |
| `SameProcessMain` | Launcher: 2 players as threads in one JVM |
| `SeparateProcessMain` | Launcher: 1 player per JVM |

## Quick start
```
./start.sh same      # same process only
./start.sh separate  # separate processes, one terminal window per player
```

## Build (Java 17+, Maven)
```
mvn clean package
```

## Run: same process
```
java -cp target/classes players.SameProcessMain
```

## Run: separate processes (two terminals)
```
java -cp target/classes players.SeparateProcessMain initiator
java -cp target/classes players.SeparateProcessMain responder
```
Each log line prints the PID.
