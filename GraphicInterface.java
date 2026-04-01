import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GraphicInterface extends JFrame {

    private Drawing canvas;
    private JButton b1, b2, b3, b4;
    private JTextArea consoleArea;

    public GraphicInterface() {
        super("Bidimensional binary tree method ");
        this.setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(5, 1, 5, 5));
        sidebar.setBackground(Color.LIGHT_GRAY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        b1 = new JButton("Input points");
        b2 = new JButton("Move search region");
        b3 = new JButton("Resize search region");
        b4 = new JButton("Bye-Bye");

        b4.setForeground(new Color(117, 77, 234));
        b4.setFont(new Font("Arial", Font.BOLD, 12));

        ActionListener modeListener = e -> {
                if (e.getSource() == b1) {
                    System.out.println(">> Input Mode activated");
                    canvas.setMode(1);
                }
                if (e.getSource() == b2) {
                    System.out.println(">> Command: Move Mode activated");
                    canvas.setMode(2);
                }
                if (e.getSource() == b3) {
                    System.out.println(">> Command: Resize Mode activated");
                    canvas.setMode(3);
                }
            };

        b1.addActionListener(modeListener);
        b2.addActionListener(modeListener);
        b3.addActionListener(modeListener);

        b4.addActionListener(e -> {
            System.exit(0);
        });

        sidebar.add(new JLabel("Controls:", SwingConstants.CENTER));
        sidebar.add(b1);
        sidebar.add(b2);
        sidebar.add(b3);
        sidebar.add(b4);

        canvas = new Drawing();

        consoleArea = new JTextArea(8, 50);
        consoleArea.setEditable(false);
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(new Color(115, 244, 255)); // Verde terminal
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                consoleArea.append(String.valueOf((char) b));
                // Scroll automat
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String s = new String(b, off, len);
                consoleArea.append(s);
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            }
        });

        System.setOut(out);
        System.setErr(out);

        System.out.println("System initialized...");
        System.out.println("Waiting for user input.");

        this.add(sidebar, BorderLayout.WEST);
        this.add(canvas, BorderLayout.CENTER);
        this.add(scrollPane, BorderLayout.SOUTH);
    }
}
