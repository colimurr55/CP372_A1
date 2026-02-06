import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Multithreaded Bulletin Board Server
 * Accepts TCP connections and manages board
 */
public class BBoard {
    private final int port;
    private final Board board;
    private volatile boolean running = true;

    public BBoard(int port, int boardWidth, int boardHeight,
                  int noteWidth, int noteHeight, Set<String> colors) {
        this.port = port;
        this.board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colors);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Bulletin Board Server started on port " + port);
            System.out.println("Board dimensions: " + board.getWidth() + "x" + board.getHeight());
            System.out.println("Note dimensions: " + board.getNoteWidth() + "x" + board.getNoteHeight());
            System.out.println("Valid colors: " + board.getValidColors());
            System.out.println("Waiting for clients...\n");

            int clientId = 0;

            while (running) {
                Socket clientSocket = serverSocket.accept();
                clientId++;

                System.out.println("Client #" + clientId + " connected from " +
                        clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, board, clientId);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Usage: java BBoard <port> <board_width> <board_height> " +
                    "<note_width> <note_height> <color1> ... <colorN>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            int boardWidth = Integer.parseInt(args[1]);
            int boardHeight = Integer.parseInt(args[2]);
            int noteWidth = Integer.parseInt(args[3]);
            int noteHeight = Integer.parseInt(args[4]);

            Set<String> colors = new LinkedHashSet<>();
            for (int i = 5; i < args.length; i++) {
                colors.add(args[i].toLowerCase());
            }

            BBoard server = new BBoard(port, boardWidth, boardHeight, noteWidth, noteHeight, colors);
            server.start();

        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid numeric argument");
            System.exit(1);
        }
    }
}
