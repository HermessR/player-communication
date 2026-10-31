package players;

import java.io.IOException;

/**
 * Link between two players.
 * Responsibility: define send(), receive() (blocking) and close(), so Player
 * does not depend on how messages travel.
 */


public interface MessageChannel extends AutoCloseable {

 
    void send(String message) throws IOException;

    String receive() throws IOException, InterruptedException;

    @Override
    void close() throws IOException;
}
