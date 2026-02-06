import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles communication with a single client in a separate thread.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Board board;
    private final int clientId;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Board board, int clientId) {
        this.socket = socket;
        this.board = board;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send initialization message
            sendInit();

            // Process client commands
            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim();
                if (command.isEmpty()) {
                    continue;
                }

                log("Received: " + command);
                String response = processCommand(command);

                if (response != null) {
                    sendResponse(response);
                }

                if (command.startsWith(Protocol.CMD_DISCONNECT)) {
                    break;
                }
            }

        } catch (IOException e) {
            log("Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Sends the init message to the client.
     */
    private void sendInit() {
        StringBuilder colorsStr = new StringBuilder();
        for (String color : board.getValidColors()) {
            if (colorsStr.length() > 0) colorsStr.append(" ");
            colorsStr.append(color);
        }

        String initMsg = Protocol.formatInit(
                board.getWidth(), board.getHeight(),
                board.getNoteWidth(), board.getNoteHeight(),
                colorsStr.toString()
        );

        out.println(initMsg);
        log("Sent: " + initMsg);
    }

    /**
     * Processes command from client.
     */
    private String processCommand(String command) {
        try {
            String[] parts = command.split("\\s+", 2);
            String cmd = parts[0].toUpperCase();

            switch (cmd) {
                case Protocol.CMD_POST:
                    return handlePost(parts.length > 1 ? parts[1] : "");

                case Protocol.CMD_GET:
                    return handleGet(parts.length > 1 ? parts[1] : "");

                case Protocol.CMD_PIN:
                    return handlePin(parts.length > 1 ? parts[1] : "");

                case Protocol.CMD_UNPIN:
                    return handleUnpin(parts.length > 1 ? parts[1] : "");

                case Protocol.CMD_SHAKE:
                    return board.shake();

                case Protocol.CMD_CLEAR:
                    return board.clear();

                case Protocol.CMD_DISCONNECT:
                    log("Client disconnecting");
                    return "OK Goodbye";

                default:
                    return "ERROR UNKNOWN_COMMAND Unknown command: " + cmd;
            }
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage();
        }
    }

    /**
     * POST command
     * Format: POST <x> <y> <color> <message>
     */
    private String handlePost(String args) {
        try {
            String[] parts = args.split("\\s+", 4);
            if (parts.length < 4) {
                return "ERROR INVALID_FORMAT Expected: POST <x> <y> <color> <message>";
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            String color = parts[2].toLowerCase();
            String message = parts[3];

            return board.postNote(x, y, color, message);

        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }

    /**
     * GET command
     * Format: GET [PINS | color=<c> contains=<x> <y> refersTo=<substring>]
     */
    private String handleGet(String args) {
        try {
            // Special case: GET PINS
            if (args.toUpperCase().equals("PINS")) {
                List<String> pins = board.getAllPins();
                StringBuilder response = new StringBuilder();
                for (String pin : pins) {
                    response.append("PIN ").append(pin).append("\n");
                }
                response.append("END");
                return response.toString();
            }

            // Parse query criteria
            String color = null;
            Integer containsX = null;
            Integer containsY = null;
            String refersTo = null;

            if (!args.isEmpty()) {
                String[] criteria = args.split("\\s+");

                for (int i = 0; i < criteria.length; i++) {
                    if (criteria[i].startsWith("color=")) {
                        color = criteria[i].substring(6).toLowerCase();

                    } else if (criteria[i].equals("contains") && i + 2 < criteria.length) {
                        containsX = Integer.parseInt(criteria[i + 1]);
                        containsY = Integer.parseInt(criteria[i + 2]);
                        i += 2;

                    } else if (criteria[i].startsWith("contains=") && i + 1 < criteria.length) {
                        containsX = Integer.parseInt(criteria[i].substring(9));
                        containsY = Integer.parseInt(criteria[i + 1]);
                        i += 1;

                    } else if (criteria[i].equals("refersTo") && i + 1 < criteria.length) {
                        refersTo = criteria[i + 1];
                        i += 1;

                    } else if (criteria[i].startsWith("refersTo=")) {
                        refersTo = criteria[i].substring(9);
                    }
                }
            }

            List<Note> notes = board.queryNotes(color, containsX, containsY, refersTo);

            StringBuilder response = new StringBuilder();
            for (Note note : notes) {
                boolean pinned = board.isNotePinned(note);

                response.append("NOTE ")
                        .append(note.getX()).append(" ")
                        .append(note.getY()).append(" ")
                        .append(note.getColor()).append(" ")
                        .append(pinned ? "pinned" : "unpinned").append(" ")
                        .append(note.getMessage())
                        .append("\n");
            }
            response.append("END");

            return response.toString();

        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }

    /**
     * PIN command.
     * Format: PIN <x> <y>
     */
    private String handlePin(String args) {
        try {
            String[] parts = args.split("\\s+");
            if (parts.length < 2) {
                return "ERROR INVALID_FORMAT Expected: PIN <x> <y>";
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            return board.placePin(x, y);

        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }

    /**
     * UNPIN command.
     * Format: UNPIN <x> <y>
     */
    private String handleUnpin(String args) {
        try {
            String[] parts = args.split("\\s+");
            if (parts.length < 2) {
                return "ERROR INVALID_FORMAT Expected: UNPIN <x> <y>";
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            return board.removePin(x, y);

        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }

    /**
     * Sends response to client.
     */
    private void sendResponse(String response) {
        String[] lines = response.split("\n");
        for (String line : lines) {
            out.println(line);
            log("Sent: " + line);
        }
    }

    /**
     * Cleans up resources.
     */
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            log("Connection closed");
        } catch (IOException e) {
            log("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Logs message with client ID.
     */
    private void log(String message) {
        System.out.println("[Client #" + clientId + "] " + message);
    }
}
