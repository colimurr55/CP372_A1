/**
 * Protocol constants and utilities for the Bulletin Board system.
 */
public class Protocol {
    // Command types
    public static final String CMD_POST = "POST";
    public static final String CMD_GET = "GET";
    public static final String CMD_PIN = "PIN";
    public static final String CMD_UNPIN = "UNPIN";
    public static final String CMD_SHAKE = "SHAKE";
    public static final String CMD_CLEAR = "CLEAR";
    public static final String CMD_DISCONNECT = "DISCONNECT";
    
    // Response types
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_INIT = "INIT";
    public static final String RESP_NOTE = "NOTE";
    public static final String RESP_PIN = "PIN";
    public static final String RESP_END = "END";
    
    // Error codes
    public static final String ERR_INVALID_FORMAT = "INVALID_FORMAT";
    public static final String ERR_OUT_OF_BOUNDS = "OUT_OF_BOUNDS";
    public static final String ERR_OVERLAP_ERROR = "OVERLAP_ERROR";
    public static final String ERR_INVALID_COLOR = "INVALID_COLOR";
    public static final String ERR_NO_NOTE = "NO_NOTE_AT_COORDINATE";
    public static final String ERR_NO_PIN = "NO_PIN_AT_COORDINATE";
    public static final String ERR_UNKNOWN_COMMAND = "UNKNOWN_COMMAND";
    
    /**
     * Formats the initial server response sent when a client connects.
     */
    public static String formatInit(int boardWidth, int boardHeight, 
                                    int noteWidth, int noteHeight, String colors) {
        return String.format("INIT %d %d %d %d %s", 
                           boardWidth, boardHeight, noteWidth, noteHeight, colors);
    }
    
    /**
     * Formats a successful response.
     */
    public static String formatOK(String message) {
        return "OK " + message;
    }
    
    /**
     * Formats an error response.
     */
    public static String formatError(String errorCode, String message) {
        return "ERROR " + errorCode + " " + message;
    }
}
