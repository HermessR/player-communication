package players;

import java.io.IOException;

/**
 * A participant in the conversation.
 * Responsibilities:
 *  - the initiator sends the first message;
 *  - on every received message, reply with: message + " " + number of messages sent;
 *  - stop after sending and receiving 10 messages.
 * It does not know how messages travel and does not create threads or processes.
 */


public final class Player implements Runnable {

  
    public enum Role { INITIATOR, RESPONDER }

    private static final String FIRST_MESSAGE = "Hello";


    private static final long SEND_DELAY_MS = 500;

    private final String name;
    private final Role role;
    private final MessageChannel channel;
    private final int maxMessages;

    private int sentCount = 0;
    private int receivedCount = 0;

    public Player(String name, Role role, MessageChannel channel, int maxMessages) {
        this.name = name;
        this.role = role;
        this.channel = channel;
        this.maxMessages = maxMessages;
    }

    @Override
    public void run() {
        log("started as " + role);
        try {
            if (role == Role.INITIATOR) {
                send(FIRST_MESSAGE);
            }
            while (!isFinished()) {
                onMessage(receive());
            }
            log("finished: sent " + sentCount + ", received " + receivedCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("interrupted");
        } catch (IOException e) {
            log("communication error: " + e.getMessage());
        }
    }

    /** Stop condition: all expected messages sent and received. */
    private boolean isFinished() {
        return sentCount >= maxMessages && receivedCount >= maxMessages;
    }

    /** The single reply rule: received message + counter of messages sent. */
    private void onMessage(String received) throws IOException, InterruptedException {
        if (sentCount < maxMessages) {
            send(received + " " + (sentCount + 1));
        }
    }

    private String receive() throws IOException, InterruptedException {
        String message = channel.receive();
        receivedCount++;
        log("received: " + message);
        return message;
    }

    private void send(String message) throws IOException, InterruptedException {
        Thread.sleep(SEND_DELAY_MS);
        channel.send(message);
        sentCount++;
        log("sent:     " + message);
    }

    private void log(String text) {
        System.out.printf("[%s | PID %d | %s] %s%n",
                name, ProcessHandle.current().pid(), Thread.currentThread().getName(), text);
    }
}
