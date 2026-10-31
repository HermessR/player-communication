package players;

/**
 * Starts both players in one Java process (same PID).
 * Responsibility: create the QueueChannel pair, run each Player in its own thread, wait for both.
 */

public class SameProcessMain {

    private static final int MAX_MESSAGES = 10;

    public static void main(String[] args) throws InterruptedException {
        QueueChannel[] ends = QueueChannel.createPair();

        Thread initiator = new Thread(
                new Player("Player1", Player.Role.INITIATOR, ends[0], MAX_MESSAGES), "player1-thread");
        Thread responder = new Thread(
                new Player("Player2", Player.Role.RESPONDER, ends[1], MAX_MESSAGES), "player2-thread");

        
        initiator.start();
        responder.start();
        

        initiator.join();
        responder.join();
        System.out.println("Done. Both players ran in PID " + ProcessHandle.current().pid());
    }
}
