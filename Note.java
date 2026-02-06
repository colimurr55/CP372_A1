import java.io.Serializable;

/**
 * Represents a note on the bulletin board.
 */
public class Note implements Serializable {
    private final int x;
    private final int y;
    private final String color;
    private final String message;
    private final int width;
    private final int height;

    public Note(int x, int y, String color, String message, int width, int height) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getColor() { return color; }
    public String getMessage() { return message; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public boolean completelyOverlaps(Note other) {
        return this.x == other.x && this.y == other.y &&
               this.width == other.width && this.height == other.height;
    }

    @Override
    public String toString() {
        return String.format("Note[pos=(%d,%d), color=%s, msg=\"%s\"]",
                x, y, color, message);
    }
}