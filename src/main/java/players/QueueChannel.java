package players;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Channel for two players in the same Java process.
 * Responsibility: pass messages between two threads using one BlockingQueue per direction.
 */

public final class QueueChannel implements MessageChannel {

    private final BlockingQueue<String> outgoing;
    private final BlockingQueue<String> incoming;

    private QueueChannel(BlockingQueue<String> outgoing, BlockingQueue<String> incoming) {
        this.outgoing = outgoing;
        this.incoming = incoming;
    }

   
    public static QueueChannel[] createPair() {
        BlockingQueue<String> aToB = new LinkedBlockingQueue<>();
        BlockingQueue<String> bToA = new LinkedBlockingQueue<>();
        return new QueueChannel[] {
            new QueueChannel(aToB, bToA),
            new QueueChannel(bToA, aToB)
        };
    }

    @Override
    public void send(String message) {
        outgoing.add(message);
    }

    @Override
    public String receive() throws InterruptedException {
        return incoming.take();
    }

    @Override
    public void close() {
       
    }
}
