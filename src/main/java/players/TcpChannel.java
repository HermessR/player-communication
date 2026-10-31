package players;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Channel for two players in separate Java processes.
 * Responsibility: pass messages over a TCP socket, one message per line.
 * accept() waits for the other process; connect() joins it.
 */

public final class TcpChannel implements MessageChannel {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private TcpChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }


    public static TcpChannel accept(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            return new TcpChannel(server.accept());
        }
    }

    
    public static TcpChannel connect(String host, int port) throws IOException, InterruptedException {
        IOException last = null;
        for (int attempt = 0; attempt < 50; attempt++) {
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host, port), 1000);
                return new TcpChannel(s);
            } catch (IOException e) {
                last = e;
                Thread.sleep(100);
            }
        }
        throw last;
    }

    @Override
    public void send(String message) {
        writer.println(message);
    }

    @Override
    public String receive() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new EOFException("Other player closed the connection");
        }
        return line;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
