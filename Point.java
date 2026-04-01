public class Point implements Comparable<Point> {
    // Represents the coordinate m(v) depending on the cut type T(v)
    private double x, y;
    private int id;

    // Corresponds to property T(v) from theory (vertical v or horizontal h)
    private int type;

    // Constants for cut direction
    public final static int HORIZONTAL = 1001;
    public final static int VERTICAL = 1002;

    private Point right, left; // Children in the tree

    public Point(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getId() { return id; }

    @Override
    public int compareTo(Point other) {
        // Natural ordering used to generate L1 (sorted by X) via TreeSet
        if (x > other.getX()) return 1;
        if ((x == other.getX()) && (y == other.getY())) return 0;
        return -1;
    }

    public void setType(int type) { this.type = type; }
    public int getType() { return type; }

    public void setRight(Point right) { this.right = right; }
    public void setLeft(Point left) { this.left = left; }

    public Point getRight() { return right; }
    public Point getLeft() { return left; }
}