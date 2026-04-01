import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Drawing extends JPanel {
    private boolean inputMode = true;
    private ArrayList<Point> pointsList;
    private Algorithm algorithm;
    private int pointCounter = 0;
    private int dragX, dragY;

    // Coordinates for the visual rectangle
    private ArrayList<Integer> rectX, rectY, storedRectX, storedRectY;

    private boolean handMode = false;
    private boolean normalMode = true;
    private boolean isDrawn = false;
    private boolean isDragging = false;
    private boolean resizeMode = false;

    private boolean dragN = false, dragS = false, dragE = false, dragW = false;

    private ArrayList<Point> foundPoints = new ArrayList<>();

    private final Color COLOR_BG = new Color(199, 255, 245);
    private final Color COLOR_AXIS = new Color(47, 71, 69);
    private final Color COLOR_POINT = new Color(3, 142, 139);
    private final Color COLOR_RECT = new Color(241, 50, 117);
    private final Color COLOR_FOUND = new Color(0, 25, 21);

    public Drawing() {
        pointsList = new ArrayList<>();
        rectX = new ArrayList<>();
        rectY = new ArrayList<>();
        storedRectX = new ArrayList<>();
        storedRectY = new ArrayList<>();

        this.setBackground(COLOR_BG);

        MyMouseListener listener = new MyMouseListener();
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);

        setSize(500, 500);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Borders
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 499, 499);

        // Axes
        g.setColor(COLOR_AXIS);
        g.drawLine(calcX(-20), calcY(0), calcX(20), calcY(0));
        g.drawLine(0, 250, 500, 250);
        g.drawString("x", calcX(21), calcY(-1));
        g.drawLine(calcX(0), calcY(-20), calcX(0), calcY(20));
        g.drawLine(250, 0, 250, 500);
        g.drawString("y", calcX(-1), calcY(21));

        // Points
        g.setColor(COLOR_POINT);
        for (Point p : pointsList) {
            g.fillOval(calcX(p.getX()) - 3, calcY(p.getY()) - 3, 6, 6);
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("" + p.getId(), calcX(p.getX()) + 4, calcY(p.getY()) - 4);
            g.setColor(COLOR_POINT);
        }

        // Rectangle logic
        if (rectX.size() > 0)
            g.setColor(COLOR_RECT);
            for(int i=0; i<rectX.size(); i++)
                g.fillOval(rectX.get(i) - 3, rectY.get(i) - 3, 7, 7);


        if (rectX.size() == 4) {
            g.setColor(COLOR_RECT);
            // Draw lines between corners: 0-2 (left), 1-3 (right), 0-3 (top), 2-1 (bottom)
            // Based on creation order: 0=TL, 1=BR, 2=BL, 3=TR
            g.drawLine(rectX.get(0), rectY.get(0), rectX.get(3), rectY.get(3)); // Top
            g.drawLine(rectX.get(2), rectY.get(2), rectX.get(1), rectY.get(1)); // Bottom
            g.drawLine(rectX.get(0), rectY.get(0), rectX.get(2), rectY.get(2)); // Left
            g.drawLine(rectX.get(3), rectY.get(3), rectX.get(1), rectY.get(1)); // Right

            // Search Logic
            double xx1 = (rectX.get(0) - 250) / 10.0; // Left X
            double xx2 = (rectX.get(1) - 250) / 10.0; // Right X
            double yy1 = (250 - rectY.get(0)) / 10.0; // Top Y
            double yy2 = (250 - rectY.get(1)) / 10.0; // Bottom Y

            if (algorithm != null) {
                // Ensure correct min/max for search regardless of how rect is dragged
                double searchX1 = Math.min(xx1, xx2);
                double searchX2 = Math.max(xx1, xx2);
                double searchY1 = Math.max(yy1, yy2); // Y is flipped in calculation
                double searchY2 = Math.min(yy1, yy2);

                foundPoints = algorithm.searchRange(searchX1, searchY1, searchX2, searchY2);

                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String msg = "Found " + foundPoints.size() + " points inside.";
                g.drawString(msg, 10, 485);

                g.setColor(COLOR_FOUND);
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(2));

                for (Point p : foundPoints)
                    g.drawOval(calcX(p.getX()) - 4, calcY(p.getY()) - 4, 8, 8);

                g2d.setStroke(oldStroke);
            }

            isDrawn = true;
            storedRectX = new ArrayList<>(rectX);
            storedRectY = new ArrayList<>(rectY);
        }

        // Status Text
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        if (inputMode) {
            g.drawString("MODE: Add Points (Left Click). Right Click to Finish.", 10, 20);
        } else if (resizeMode) {
            g.drawString("MODE: Resize (Grab edges to change size).", 10, 20);
        } else {
            g.drawString("MODE: Search & Drag. Draw a rectangle to begin.", 10, 20);
        }
    }

    public int calcX(double x) { return (int) (250 + x * 10); }
    public int calcY(double y) { return (int) (250 - y * 10); }

    public void setMode(int mode) {
        // Reset flags
        dragN = dragS = dragE = dragW = false;

        if (mode == 1) { // Draw/Input Mode
            if (isDrawn) {
                JOptionPane.showMessageDialog(null, "You cannot add points once the search region is defined!", "Action Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }

            normalMode = true;
            isDragging = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            handMode = false;
            resizeMode = false;
        }
        if (mode == 2) { // Drag Mode
            if (isDrawn) {
                handMode = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                normalMode = false;
                isDragging = false;
                resizeMode = false;
            } else
                JOptionPane.showMessageDialog(null, "Draw a rectangle first!", "Action Blocked", JOptionPane.WARNING_MESSAGE);

        }
        if (mode == 3) { // Resize Mode
            if (isDrawn) {
                resizeMode = true;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                handMode = false;
                normalMode = false;
            } else
                JOptionPane.showMessageDialog(null, "Draw a rectangle first!","Action Blocked", JOptionPane.WARNING_MESSAGE);

        }
        repaint();
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (normalMode) {
                if (inputMode && e.getButton() == MouseEvent.BUTTON3) {
                    inputMode = false;
                    algorithm = new Algorithm(pointsList);
                    repaint();
                }
                if (inputMode && e.getButton() == MouseEvent.BUTTON1) {
                    pointsList.add(new Point((e.getX() - 250) / 10.0, (250 - e.getY()) / 10.0, pointCounter++));
                    repaint();
                }
                if (!inputMode && e.getButton() == MouseEvent.BUTTON1) {
                    int mouseX = e.getX();
                    int mouseY = e.getY();

                    rectX.add(mouseX);
                    rectY.add(mouseY);

                    if (rectX.size() == 2) {
                        int x1 = rectX.get(0);
                        int y1 = rectY.get(0);
                        int x2 = rectX.get(1);
                        int y2 = rectY.get(1);

                        int minX = Math.min(x1, x2);
                        int maxX = Math.max(x1, x2);
                        int minY = Math.min(y1, y2);
                        int maxY = Math.max(y1, y2);

                        rectX.clear(); rectY.clear();

                        // 0: Top-Left, 1: Bottom-Right, 2: Bottom-Left, 3: Top-Right
                        rectX.add(minX); rectY.add(minY);
                        rectX.add(maxX); rectY.add(maxY);
                        rectX.add(minX); rectY.add(maxY);
                        rectX.add(maxX); rectY.add(minY);
                    }
                    repaint();
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // HAND MODE
            if (handMode && !isDragging)
                if (!storedRectX.isEmpty()) {
                    int minX = storedRectX.get(0);
                    int maxX = storedRectX.get(1);
                    int minY = storedRectY.get(0);
                    int maxY = storedRectY.get(1);

                    if (e.getX() > minX && e.getX() < maxX && e.getY() > minY && e.getY() < maxY) {
                        isDragging = true;
                        dragX = e.getX();
                        dragY = e.getY();
                    }
                }

            if (handMode && isDragging) {
                int dx = e.getX() - dragX;
                int dy = e.getY() - dragY;

                // FIX: Actualizam "stored" values direct, pentru ca miscarea sa fie continua
                for(int i=0; i<4; i++) {
                    storedRectX.set(i, storedRectX.get(i) + dx);
                    storedRectY.set(i, storedRectY.get(i) + dy);
                }

                // Sincronizam vizualul cu stocarea
                rectX = new ArrayList<>(storedRectX);
                rectY = new ArrayList<>(storedRectY);

                // Actualizam ancora mouse-ului pentru urmatorul frame
                dragX = e.getX();
                dragY = e.getY();
                repaint();
            }

            // --- LOGICA PENTRU RESIZE ---
            if (resizeMode) {
                // Modificam coordonatele in timp real
                // 0:TL, 1:BR, 2:BL, 3:TR

                // Copiem valorile curente
                int x0 = rectX.get(0), y0 = rectY.get(0); // Top-Left
                int x1 = rectX.get(1), y1 = rectY.get(1); // Bottom-Right
                int x2 = rectX.get(2), y2 = rectY.get(2); // Bottom-Left
                int x3 = rectX.get(3), y3 = rectY.get(3); // Top-Right

                int minSize = 20; // Dimensiunea minimă a chenarului (pixeli)

                if (dragN) {
                    // Tragem de SUS. Nu avem voie să coborâm sub (JOS - minSize)
                    // y2 este coordonata Y a laturii de jos
                    int newY = Math.min(e.getY(), y2 - minSize);
                    y0 = newY; y3 = newY;
                }
                if (dragS) {
                    // Tragem de JOS. Nu avem voie să urcăm peste (SUS + minSize)
                    // y0 este coordonata Y a laturii de sus
                    int newY = Math.max(e.getY(), y0 + minSize);
                    y1 = newY; y2 = newY;
                }
                if (dragW) {
                    // Tragem din STÂNGA. Nu trecem de (DREAPTA - minSize)
                    // x1 este coordonata X a laturii din dreapta
                    int newX = Math.min(e.getX(), x1 - minSize);
                    x0 = newX; x2 = newX;
                }
                if (dragE) {
                    // Tragem din DREAPTA. Nu trecem de (STÂNGA + minSize)
                    // x0 este coordonata X a laturii din stânga
                    int newX = Math.max(e.getX(), x0 + minSize);
                    x1 = newX; x3 = newX;
                }

                rectX.clear(); rectY.clear();
                rectX.add(x0); rectY.add(y0);
                rectX.add(x1); rectY.add(y1);
                rectX.add(x2); rectY.add(y2);
                rectX.add(x3); rectY.add(y3);

                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Cand dam drumul la mouse, salvam pozitia finala ca fiind cea de baza
            // Acest lucru este crucial mai ales pentru Resize
            if (resizeMode)
                if(rectX.size() == 4) {
                    storedRectX = new ArrayList<>(rectX);
                    storedRectY = new ArrayList<>(rectY);
                }
            isDragging = false;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (resizeMode && !storedRectX.isEmpty()) {
                int tolerance = 10; // Distanta in pixeli
                int mx = e.getX();
                int my = e.getY();

                // Punctele dreptunghiului stocat
                int minX = Math.min(storedRectX.get(0), storedRectX.get(1));
                int maxX = Math.max(storedRectX.get(0), storedRectX.get(1));
                int minY = Math.min(storedRectY.get(0), storedRectY.get(1));
                int maxY = Math.max(storedRectY.get(0), storedRectY.get(1));

                // Reset flags
                dragN = dragS = dragE = dragW = false;

                // Detectie si schimbare cursor
                boolean onTop = Math.abs(my - minY) < tolerance && mx > minX && mx < maxX;
                boolean onBottom = Math.abs(my - maxY) < tolerance && mx > minX && mx < maxX;
                boolean onLeft = Math.abs(mx - minX) < tolerance && my > minY && my < maxY;
                boolean onRight = Math.abs(mx - maxX) < tolerance && my > minY && my < maxY;

                if (onTop) {
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    dragN = true;
                } else if (onBottom) {
                    setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    dragS = true;
                } else if (onLeft) {
                    setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                    dragW = true;
                } else if (onRight) {
                    setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                    dragE = true;
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }
    }
}