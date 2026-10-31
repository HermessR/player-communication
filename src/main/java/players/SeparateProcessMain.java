package players;

import java.io.IOException;

/**
 * Starts one player per Java process (different PIDs).
 * Usage: SeparateProcessMain <initiator|responder> [port] [host]
 * Responsibility: open the TcpChannel for the given role and run one Player.
 */


public class SeparateProcessMain {

    private static final int MAX_MESSAGES = 10;
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: SeparateProcessMain <initiator|responder> [port] [host]");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String host = args.length > 2 ? args[2] : "localhost";

        switch (mode) {
            case "responder" -> {
                System.out.println("[Player2 | PID " + ProcessHandle.current().pid()
                        + "] waiting for the initiator on port " + port + "...");
                try (MessageChannel channel = TcpChannel.accept(port)) {
                    System.out.println("[Player2] initiator connected");
                    new Player("Player2", Player.Role.RESPONDER, channel, MAX_MESSAGES).run();
                }
            }
            case "initiator" -> {
                try (MessageChannel channel = TcpChannel.connect(host, port)) {
                    System.out.println("[Player1] connected, starting the conversation");
                    new Player("Player1", Player.Role.INITIATOR, channel, MAX_MESSAGES).run();
                }
            }
            default -> {
                System.err.println("Unknown mode: " + mode);
                System.exit(1);
            }
        }
    }
}
