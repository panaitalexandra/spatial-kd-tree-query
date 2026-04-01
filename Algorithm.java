import java.util.*;

public class Algorithm {
    private ArrayList<Point> points;

    // L1 and L2 store the initial set of points used to identify medians.
    // L1: Sorted by X (generated via TreeSet).
    // L2: Sorted by Y (generated via Bubble Sort).
    private Point[] L1, L2;

    private Point root; // vf
    private ArrayList<Point> treeNodes; // v
    private ArrayList<Point> resultList; // S

    public Algorithm(ArrayList<Point> points) {
        this.points = points;
        makeListSortedX(); // Sorts L1 by X
        makeListSortedY(); // Sorts L2 by Y

        System.out.print("L1 (sorted by X):");
        for (Point p : L1) System.out.print(p.getId() + " ");
        System.out.println();

        System.out.print("L2 (sorted by Y):");
        for (Point p : L2) System.out.print(p.getId() + " ");
        System.out.println();

        makeBinaryTree();
    }

    private void makeListSortedX() {
        // Sort unique points by X (using TreeSet natural ordering defined in Point)
        TreeSet<Point> set = new TreeSet<>(points);
        L1 = set.toArray(new Point[0]);
    }

    private void makeListSortedY() {
        // Clone list X and sort by Y
        L2 = L1.clone();
        // Bubble sort for Y to compare Y() values
        for (int i = 0; i < L2.length - 1; ++i)
            for (int j = i + 1; j < L2.length; ++j)
                if (L2[i].getY() > L2[j].getY()) {
                    Point temp = L2[i];
                    L2[i] = L2[j];
                    L2[j] = temp;
                }
    }

    public void makeBinaryTree() {
        treeNodes = new ArrayList<>();
        // Masking points for each partition
        boolean[] activePoints = new boolean[L1.length];

        // Root Selection: The median of L1 (sorted by X). Index is L1.length / 2.
        // Root is assigned VERTICAL cut to initiate the X-axis split.
        int mid = L1.length / 2;
        root = L1[mid];
        root.setType(Point.VERTICAL);

        // Masking: The boolean vector filters the set, marking points belonging to the left subtree.
        // The vector uses ID as index, if "mask[id] == true", the point is considered
        // Left: Iteration to the mid - 1
        for (int i = 0; i < mid; ++i)
            if(L1[i].getId() < activePoints.length)
                activePoints[L1[i].getId()] = true;

        // Build Left Subtree: Calls splitByY to alternate the cut to HORIZONTAL
        root.setLeft(splitByY(0, mid - 1, 0, L2.length - 1, activePoints));

        // Reset and mark points for right subtree
        activePoints = new boolean[L1.length];
        for (int i = mid + 1; i < L1.length; ++i)
            if(L1[i].getId() < activePoints.length)
                // position in the mask ~ point s id
                activePoints[L1[i].getId()] = true;

        // Build Right Subtree: Calls splitByY to alternate the cut
        root.setRight(splitByY(mid + 1, L1.length - 1, 0, L2.length - 1, activePoints));

        treeNodes.add(root);
        printTreeDebug();
    }

    // Alternation: Called from a Vertical split. Sets the median as HORIZONTAL and calls splitByX for children
    // 'mask' identifies points in the current spatial region
    private Point splitByY(int xa, int ya, int xo, int yo, boolean[] mask) {
        ArrayList<Point> subList = new ArrayList<>();

        // Filter L2 using the mask to get points in this subdomain
        // Recheck for index and add only if marked true in mask
        for (Point p : L2)
            if (p.getId() < mask.length && mask[p.getId()])
                subList.add(p);

        if (subList.isEmpty())
            return null;

        Point median = subList.get(subList.size() / 2);
        median.setType(Point.HORIZONTAL); // Set cut type T(v) to Horizontal
        treeNodes.add(median);

        // Get the id of the cut line from the original list L2
        int midIndexInSortedY = -1;
        for(int i = 0; i< L2.length; i++)
            if(L2[i].getId() == median.getId()){
                midIndexInSortedY = i;
                break;
            }
        // Generate mask for Bottom child
        boolean[] leftMask = new boolean[L2.length];
        for (int i = 0; i < subList.size() / 2; ++i)
            if(subList.get(i).getId() < leftMask.length)
                leftMask[subList.get(i).getId()] = true;

        // Recursive call alternates back to X (Vertical)
        median.setLeft(splitByX(xa, ya, xo, midIndexInSortedY - 1, leftMask));

        // Generate mask for Top child
        boolean[] rightMask = new boolean[L2.length];
        for (int i = subList.size() / 2 + 1; i < subList.size(); ++i)
            if(subList.get(i).getId() < rightMask.length)
                rightMask[subList.get(i).getId()] = true;

        // Recursive call alternates back to X (Vertical)
        median.setRight(splitByX(xa, ya, midIndexInSortedY + 1, yo, rightMask));

        return median;
    }

    // Alternation: Called from a Horizontal split. Sets median as VERTICAL and calls splitByY
    private Point splitByX(int xa, int ya, int xo, int yo, boolean[] mask) {
        ArrayList<Point> subList = new ArrayList<>();
        for (Point p : L1)
            if (p.getId() < mask.length && mask[p.getId()])
                subList.add(p);

        if (subList.isEmpty())
            return null;

        Point median = subList.get(subList.size() / 2);
        median.setType(Point.VERTICAL); // Set cut type T(v) to Vertical
        treeNodes.add(median);

        int midIndexInSortedX = -1;
        for(int i = 0; i< L1.length; i++)
            if(L1[i].getId() == median.getId()){
                midIndexInSortedX = i;
                break;
            }

        boolean[] leftMask = new boolean[L1.length];
        for (int i = 0; i < subList.size() / 2; ++i)
            if(subList.get(i).getId() < leftMask.length)
                leftMask[subList.get(i).getId()] = true;

        median.setLeft(splitByY(xa, midIndexInSortedX - 1, xo, yo, leftMask));

        boolean[] rightMask = new boolean[L1.length];
        for (int i = subList.size() / 2 + 1; i < subList.size(); ++i)
            if(subList.get(i).getId() < rightMask.length)
                rightMask[subList.get(i).getId()] = true;

        median.setRight(splitByY(midIndexInSortedX + 1, ya, xo, yo, rightMask));

        return median;
    }

    // Range Search O(M) where M is complexity of search/output.
    // Inputs: x1=left, x2=right, y2=bottom, y1=top. Assumes standard math logic (y increasing upwards)
    public ArrayList<Point> searchRange(double x1, double y1, double x2, double y2) {
        resultList = new ArrayList<>();
        System.out.println("Total points in plane: " + treeNodes.size());
        System.out.println("Rectangle: (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");

        recursiveSearch(root, x1, y1, x2, y2);
        return resultList;
    }

    private void recursiveSearch(Point p, double x1, double y1, double x2, double y2) {
        if (p == null)
            return;

        double minLimit, maxLimit, pointCoord;

        // Determine limits based on Node Type T(v)
        if (p.getType() == Point.VERTICAL) {
            // Cut is Vertical: compare X coordinate with left (x1) and right (x2) limits
            minLimit = x1;
            maxLimit = x2;
            pointCoord = p.getX();
        } else {
            // Cut is Horizontal: compare Y coordinate with bottom (y2) and top (y1) limits
            minLimit = y2;
            maxLimit = y1;
            pointCoord = p.getY();
        }

        // Inclusion Condition: Checks if point P(v) is physically inside rectangle D
        // Verifies 4 inequalities: x >= x1, x <= x2, y <= y1, y >= y2
        if (p.getX() >= x1 && p.getX() <= x2 && p.getY() <= y1 && p.getY() >= y2) {
            System.out.println("Point added! " + p.getId());
            resultList.add(p);
        }

        // Traversal Decision: Checks if cut line m(v) intersects the search region
        // 1. Explore Left Subtree if pointCoord >= minLimit
        if (pointCoord >= minLimit)
            if (p.getLeft() != null)
                recursiveSearch(p.getLeft(), x1, y1, x2, y2);

        // 2. Explore Right Subtree if pointCoord <= maxLimit
        // If pointCoord is between minLimit and maxLimit, BOTH subtrees are explored
        if (pointCoord <= maxLimit)
            if (p.getRight() != null)
                recursiveSearch(p.getRight(), x1, y1, x2, y2);
    }

    private void printTreeDebug() {
        System.out.println("Root " + root.getId());
        for (Point p : treeNodes) {

            System.out.print("Node " + p.getId());

            if (p.getRight() != null)
                System.out.print(" Right " + p.getRight().getId());

            if (p.getLeft() != null)
                System.out.print(" Left " + p.getLeft().getId());

            System.out.println();
        }
    }
}