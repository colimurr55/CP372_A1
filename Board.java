import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * bulletin board that manages notes and pins.
 */
public class Board {
    private final int width;
    private final int height;
    private final int noteWidth;
    private final int noteHeight;
    private final Set<String> validColors;

    private final List<Note> notes;

    private final Set<String> pins;

    public Board(int width, int height, int noteWidth, int noteHeight, Set<String> validColors) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColors = new HashSet<>(validColors);

        this.notes = new CopyOnWriteArrayList<>();
        this.pins = new HashSet<>();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getNoteWidth() { return noteWidth; }
    public int getNoteHeight() { return noteHeight; }
    public Set<String> getValidColors() { return new HashSet<>(validColors); }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private String pinKey(int x, int y) {
        return x + "," + y;
    }

    /**
     * Returns true if the given note contains one pin within its boundaries.
     */
    public synchronized boolean isNotePinned(Note note) {
        for (String p : pins) {
            String[] parts = p.split(",");
            int px = Integer.parseInt(parts[0]);
            int py = Integer.parseInt(parts[1]);

            if (note.contains(px, py)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Posts a new note to the board.
     * returns an error message on failure, OK message on success
     */
    public synchronized String postNote(int x, int y, String color, String message) {
        // Validate color
        if (!validColors.contains(color)) {
            return "ERROR INVALID_COLOR " + color;
        }

        // Validate bounds
        if (x < 0 || y < 0 || x + noteWidth > width || y + noteHeight > height) {
            return "ERROR OUT_OF_BOUNDS Note would extend beyond board boundaries";
        }

        // Create new note
        Note newNote = new Note(x, y, color, message, noteWidth, noteHeight);

        // Check for complete overlap
        for (Note existing : notes) {
            if (existing.completelyOverlaps(newNote)) {
                return "ERROR OVERLAP_ERROR Note completely overlaps existing note";
            }
        }

        notes.add(newNote);
        return "OK Note posted successfully";
    }

    /**
     * Places a pin anywhere on the board.
     */
    public synchronized String placePin(int x, int y) {
        if (!inBounds(x, y)) {
            return "ERROR OUT_OF_BOUNDS Pin must be inside the board";
        }

        String key = pinKey(x, y);

        if (pins.contains(key)) {
            return "ERROR PIN_ALREADY_EXISTS Pin already exists at (" + x + "," + y + ")";
        }

        pins.add(key);
        return "OK Pin placed at (" + x + "," + y + ")";
    }

    /**
     * Removes a pin anywhere on the board.
     */
    public synchronized String removePin(int x, int y) {
        String key = pinKey(x, y);

        if (!pins.contains(key)) {
            return "ERROR NO_PIN_AT_COORDINATE No pin found at coordinate (" + x + "," + y + ")";
        }

        pins.remove(key);
        return "OK Pin removed from (" + x + "," + y + ")";
    }

    /**
     * Removes all unpinned notes from the board.
     * A note is pinned if it contains at least one pin coordinate.
     */
    public synchronized String shake() {
        int beforeCount = notes.size();

        notes.removeIf(note -> !isNotePinned(note));

        int afterCount = notes.size();
        int removed = beforeCount - afterCount;

        return "OK Removed " + removed + " unpinned note(s)";
    }

    /**
     * Removes all notes and pins from the board.
     */
    public synchronized String clear() {
        int noteCount = notes.size();
        int pinCount = pins.size();

        notes.clear();
        pins.clear();

        return "OK Cleared " + noteCount + " note(s) and " + pinCount + " pin(s)";
    }

    /**
     * Gets all pins on the board.
     */
    public synchronized List<String> getAllPins() {
        List<String> out = new ArrayList<>();
        for (String key : pins) {
            out.add(key.replace(",", " "));
        }
        Collections.sort(out);
        return out;
    }

    /**
     * Queries notes based on search filters.
     */
    public synchronized List<Note> queryNotes(String color, Integer containsX, Integer containsY, String refersTo) {
        List<Note> results = new ArrayList<>();

        for (Note note : notes) {
            // Check color filter
            if (color != null && !note.getColor().equals(color)) {
                continue;
            }

            // Check contains filter
            if (containsX != null && containsY != null) {
                if (!note.contains(containsX, containsY)) {
                    continue;
                }
            }

            // Check refersTo filter
            if (refersTo != null && !note.getMessage().toLowerCase().contains(refersTo.toLowerCase())) {
                continue;
            }

            results.add(note);
        }

        return results;
    }

    /**
     * Gets all notes on the board.
     */
    public synchronized List<Note> getAllNotes() {
        return new ArrayList<>(notes);
    }
}
