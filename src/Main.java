import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.Panel;


public class Main extends JFrame {
    private static final long serialVersionUID = 1L;

    private JFrame frame = this;
    private JPanel contentPane;
    public JPanel buttonPanel;
    public JLabel iterationLabel;

    static int iterations = 1000;
    static double zoom = 100;
    static double zoomMult = Math.pow(10, .1 / 2);
    static double zoomMultScroll = Math.pow(10, .1 / 4);
    static int precision = 16;
    static double centerX = -.65;
    static double centerY = 0;
    static int yScan = -1;
    static int colorSmoothness = 100;
    static int colorOffset = 0;

    static boolean movingCenter = false;
    static int canvasPrevMouseX = 0;
    static int canvasPrevMouseY = 0;
    static int canvasCurOffsetX = 0;
    static int canvasCurOffsetY = 0;

    static BufferedImage b;
    static Canvas canvas;
    static ScrollPane scrollPane;

    static long iterSum = 0;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main frame = new Main();
                    frame.setVisible(true);

                    JOptionPane.showMessageDialog(frame,
                            "Left/right click or scroll to zoom\n" +
                            "Drag to reposition");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    int drawID = 0;
    int antiAlias = 1;
    public void drawMS() {
        final int curID = ++drawID;
        new Thread(new Runnable() {
            public void run() {
                if (canvas != null) canvas.paint(null);
                iterSum = 0;
                iterationLabel.setText("0");
                for (int y0 = 0; y0 < b.getHeight() && drawID == curID; y0++) {
                    for (int x0 = 0; x0 < b.getWidth() && drawID == curID; x0++) {
                        double x = (x0 + centerX - b.getWidth() / 2) / zoom + centerX;
                        double y = (y0 + centerY - b.getHeight() / 2) / zoom + centerY;

                        Color c = null;
                        double cR = 0;
                        double cG = 0;
                        double cB = 0;
                        double e = 1 / zoom / antiAlias;
                        for (int dx = 0; dx < antiAlias; dx++) {
                            for (int dy = 0; dy < antiAlias; dy++) {
                                int n = MandelbrotSet.test(new Complex(x + e * dx , y + e * dy), iterations);
                                iterSum += n;
                                c = getColor(n);
                                
                                double goodR = c.getRed() / 255.;
                                double goodG = c.getGreen() / 255.;
                                double goodB = c.getBlue() / 255.;
                                
                                cR += goodR * goodR;
                                cG += goodG * goodG;
                                cB += goodB * goodB;
                            }
                        }
                        int d = antiAlias * antiAlias;
                        try {
                            c = new Color((int)(Math.sqrt(cR / d) * 255),
                                          (int)(Math.sqrt(cG / d) * 255),
                                          (int)(Math.sqrt(cB / d) * 255));
                        } catch (Exception ex) {}
                        b.setRGB(x0, y0, c.getRGB());
                    }
                    yScan = y0;
                    iterationLabel.setText(NumberFormat.getIntegerInstance().format(iterSum));
                    if (canvas != null) canvas.paint(null);
                }
                yScan = -1;
                if (canvas != null) canvas.paint(null);
            }
        }).start();
    }

    Color getColor(int loops) {
        return loops == -1 ? Color.BLACK : Color.getHSBColor((float)((double)loops / colorSmoothness + colorOffset / 360.), 1, 1);
    }

    /**
     * Create the frame.
     */
    @SuppressWarnings("serial")
    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 876, 611);
        contentPane = new JPanel();
        contentPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                scrollPane.setBounds(0, 0, contentPane.getWidth(), contentPane.getHeight() - 50);
                buttonPanel.setBounds(0, contentPane.getHeight() - 50, buttonPanel.getWidth(), 50);
            }
        });
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        final JFileChooser saveDialog = new JFileChooser();
        saveDialog.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File arg0) {
                return arg0.isDirectory() || arg0.getName().toLowerCase().endsWith(".png");
            }
            @Override
            public String getDescription() {
                return "Portable Network Graphics (*.png)";
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setBounds(0, 522, 860, 50);
        contentPane.add(buttonPanel);
        GridBagLayout gbl_buttonPanel = new GridBagLayout();
        gbl_buttonPanel.columnWidths = new int[] {70, 100, 110, 90, 110, 80, 100, 200, 0};
        gbl_buttonPanel.rowHeights = new int[]{25, 25, 0};
        gbl_buttonPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_buttonPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        buttonPanel.setLayout(gbl_buttonPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel label_6 = new JLabel("Anti-Alias:", SwingConstants.CENTER);
        buttonPanel.add(label_6, gbc);
        GridBagConstraints gbc_1 = new GridBagConstraints();
        gbc_1.fill = GridBagConstraints.BOTH;
        gbc_1.insets = new Insets(0, 0, 5, 5);
        gbc_1.gridx = 1;
        gbc_1.gridy = 0;
        JLabel label_1 = new JLabel("Max Iterations:", SwingConstants.CENTER);
        buttonPanel.add(label_1, gbc_1);
        GridBagConstraints gbc_2 = new GridBagConstraints();
        gbc_2.fill = GridBagConstraints.BOTH;
        gbc_2.insets = new Insets(0, 0, 5, 5);
        gbc_2.gridx = 2;
        gbc_2.gridy = 0;
        JLabel label_3 = new JLabel("Color Smoothness:", SwingConstants.CENTER);
        buttonPanel.add(label_3, gbc_2);
        GridBagConstraints gbc_3 = new GridBagConstraints();
        gbc_3.fill = GridBagConstraints.BOTH;
        gbc_3.insets = new Insets(0, 0, 5, 5);
        gbc_3.gridx = 3;
        gbc_3.gridy = 0;
        JLabel label = new JLabel("Color Offset:", SwingConstants.CENTER);
        buttonPanel.add(label, gbc_3);
        GridBagConstraints gbc_4 = new GridBagConstraints();
        gbc_4.fill = GridBagConstraints.BOTH;
        gbc_4.insets = new Insets(0, 0, 5, 5);
        gbc_4.gridx = 4;
        gbc_4.gridy = 0;
        JLabel label_2 = new JLabel("Go To Coordinate:", SwingConstants.CENTER);
        buttonPanel.add(label_2, gbc_4);

        JButton gotoButton = new JButton("Go To");
        gotoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String z = JOptionPane.showInputDialog("Zoom:", 100);
                String x = JOptionPane.showInputDialog("Center X:", 0);
                String y = JOptionPane.showInputDialog("Center Y:", 0);

                try {
                    double newZ = Double.parseDouble(z);
                    double newX = Double.parseDouble(x);
                    double newY = Double.parseDouble(y);
                    zoom = newZ;
                    centerX = newX;
                    centerY = newY;
                    drawMS();
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(frame, "Error parsing values!");
                }
            }
        });

        JLabel label_7 = new JLabel("Set Size:");
        GridBagConstraints gbc_label_7 = new GridBagConstraints();
        gbc_label_7.insets = new Insets(0, 0, 5, 5);
        gbc_label_7.gridx = 5;
        gbc_label_7.gridy = 0;
        buttonPanel.add(label_7, gbc_label_7);
        GridBagConstraints gbc_5 = new GridBagConstraints();
        gbc_5.fill = GridBagConstraints.BOTH;
        gbc_5.insets = new Insets(0, 0, 5, 5);
        gbc_5.gridx = 6;
        gbc_5.gridy = 0;
        JLabel label_4 = new JLabel("Save Rendering:", SwingConstants.CENTER);
        buttonPanel.add(label_4, gbc_5);
        GridBagConstraints gbc_6 = new GridBagConstraints();
        gbc_6.fill = GridBagConstraints.BOTH;
        gbc_6.insets = new Insets(0, 0, 5, 0);
        gbc_6.gridx = 7;
        gbc_6.gridy = 0;
        JLabel label_5 = new JLabel("Total Iterations:", SwingConstants.CENTER);
        buttonPanel.add(label_5, gbc_6);

        final JSpinner iterSpinner = new JSpinner();
        iterSpinner.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(1), null, new Integer(1)));
        iterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                iterations = (Integer) iterSpinner.getValue();
                drawMS();
            }
        });

        final JSpinner aaSpinner = new JSpinner();
        aaSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        aaSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                antiAlias = (Integer) aaSpinner.getValue();
                drawMS();
            }
        });
        GridBagConstraints gbc_aaSpinner = new GridBagConstraints();
        gbc_aaSpinner.fill = GridBagConstraints.BOTH;
        gbc_aaSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_aaSpinner.gridx = 0;
        gbc_aaSpinner.gridy = 1;
        buttonPanel.add(aaSpinner, gbc_aaSpinner);
        aaSpinner.setValue(antiAlias);
        GridBagConstraints gbc_iterSpinner = new GridBagConstraints();
        gbc_iterSpinner.fill = GridBagConstraints.BOTH;
        gbc_iterSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_iterSpinner.gridx = 1;
        gbc_iterSpinner.gridy = 1;
        buttonPanel.add(iterSpinner, gbc_iterSpinner);
        iterSpinner.setValue(iterations);

        final JSpinner colorOffsetSpinner = new JSpinner();
        colorOffsetSpinner.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
        colorOffsetSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                colorOffsetSpinner.setValue(((Integer)colorOffsetSpinner.getValue() % 360 + 360) % 360);
                colorOffset = (Integer) colorOffsetSpinner.getValue();
                drawMS();
            }
        });

        final JSpinner colorSpinner = new JSpinner();
        colorSpinner.setModel(new SpinnerNumberModel(new Integer(100), new Integer(0), null, new Integer(1)));
        colorSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                colorSmoothness = (Integer) colorSpinner.getValue();
                drawMS();
            }
        });
        GridBagConstraints gbc_colorSpinner = new GridBagConstraints();
        gbc_colorSpinner.fill = GridBagConstraints.BOTH;
        gbc_colorSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_colorSpinner.gridx = 2;
        gbc_colorSpinner.gridy = 1;
        buttonPanel.add(colorSpinner, gbc_colorSpinner);
        colorSpinner.setValue(colorSmoothness);
        GridBagConstraints gbc_colorOffsetSpinner = new GridBagConstraints();
        gbc_colorOffsetSpinner.fill = GridBagConstraints.BOTH;
        gbc_colorOffsetSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_colorOffsetSpinner.gridx = 3;
        gbc_colorOffsetSpinner.gridy = 1;
        buttonPanel.add(colorOffsetSpinner, gbc_colorOffsetSpinner);
        GridBagConstraints gbc_gotoButton = new GridBagConstraints();
        gbc_gotoButton.fill = GridBagConstraints.BOTH;
        gbc_gotoButton.insets = new Insets(0, 0, 0, 5);
        gbc_gotoButton.gridx = 4;
        gbc_gotoButton.gridy = 1;
        buttonPanel.add(gotoButton, gbc_gotoButton);

        JButton btnSize = new JButton("Size");
        btnSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String w = JOptionPane.showInputDialog("Width:", canvas.getWidth());
                if (w == null) return;
                String h = JOptionPane.showInputDialog("Height:", canvas.getHeight());
                if (h == null) return;
                try {
                    int wi = Integer.parseInt(w);
                    int hi = Integer.parseInt(h);
                    canvas.setBounds(canvas.getX(), canvas.getY(), wi, hi);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid integer!");
                }
            }
        });
        GridBagConstraints gbc_btnSize = new GridBagConstraints();
        gbc_btnSize.fill = GridBagConstraints.BOTH;
        gbc_btnSize.insets = new Insets(0, 0, 0, 5);
        gbc_btnSize.gridx = 5;
        gbc_btnSize.gridy = 1;
        buttonPanel.add(btnSize, gbc_btnSize);

        JButton button = new JButton("Save");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (saveDialog.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        String file = saveDialog.getSelectedFile().toString();
                        if (file.toLowerCase().endsWith(".png")) file = file.substring(0, file.length() - 4);
                        ImageIO.write(b, "PNG", new File(file + ".png"));

                        BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".txt"));
                        String nl = System.getProperty("line.separator");
                        bw.write("Zoom: " + zoom + nl +
                                "Center X: " + centerX + nl +
                                "Center Y: " + centerY + nl + nl +
                                "Anti-Alias: " + antiAlias + nl +
                                "Iterations: " + iterations + nl +
                                "Color Smoothness: " + colorSmoothness + nl +
                                "Color Offset: " + colorOffset + nl);
                        bw.close();
                        JOptionPane.showMessageDialog(frame, "Image saved successfully!");
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(frame, "Error saving image:\n" + e1.getMessage());
                    }
                }
            }
        });
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.fill = GridBagConstraints.BOTH;
        gbc_button.insets = new Insets(0, 0, 0, 5);
        gbc_button.gridx = 6;
        gbc_button.gridy = 1;
        buttonPanel.add(button, gbc_button);

        iterationLabel = new JLabel("0", SwingConstants.CENTER);
        GridBagConstraints gbc_iterationLabel = new GridBagConstraints();
        gbc_iterationLabel.fill = GridBagConstraints.BOTH;
        gbc_iterationLabel.gridx = 7;
        gbc_iterationLabel.gridy = 1;
        buttonPanel.add(iterationLabel, gbc_iterationLabel);

        scrollPane = new ScrollPane();
        scrollPane.setBounds(0, 0, 860, 522);
        contentPane.add(scrollPane);

        Panel panel = new Panel();
        panel.setBounds(85, 102, 327, 290);
        scrollPane.add(panel);
        panel.setLayout(null);

        canvas = new Canvas() {
            public void paint(Graphics g) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                if (b == null || b.getWidth() != width || b.getHeight() != height) {

                    BufferedImage b2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    int dx = 0, dy = 0;
                    if (b != null) {
                        dx = canvas.getWidth() - b.getWidth();
                        dy = canvas.getHeight() - b.getHeight();
                    }

                    b2.getGraphics().drawImage(b, dx / 2, dy / 2, null);
                    b = b2;
                    drawMS();
                } if (b != null) {
                    int x = (int)canvasCurOffsetX;
                    int y = (int)canvasCurOffsetY;

                    try {
                        if (canvas.getBufferStrategy() == null)
                            canvas.createBufferStrategy(2);
                        g = getBufferStrategy().getDrawGraphics();

                        g.clearRect(0, 0, b.getWidth(), b.getHeight());
                        g.drawImage(b, x, y, null);
                        g.setColor(Color.RED);
                        if (yScan != -1)
                            g.drawLine(0, yScan, b.getWidth(), yScan);

                        g.setColor(new Color(255, 255, 255, 63));
                        g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                        g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                        
                        getBufferStrategy().show();
                    } catch (IllegalStateException e) {}
                }

            }
        };
        panel.add(canvas);
        canvas.setBackground(Color.GRAY);
        canvas.setBounds(10, 10, 400, 400);

        canvas.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                System.out.println(e.getPropertyName());
            }
        });
        canvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                canvasCurOffsetX += e.getX() - canvasPrevMouseX;
                canvasCurOffsetY += e.getY() - canvasPrevMouseY;
                canvasPrevMouseX = e.getX();
                canvasPrevMouseY = e.getY();
                movingCenter = true;
                canvas.repaint();
            }

            public void mouseMoved(MouseEvent e) {
                double x = (e.getX() - b.getWidth() / 2) / zoom + centerX;
                double y = (e.getY() - b.getHeight() / 2) / zoom + centerY;
                updateTitle(x, y);
            }
        });
        canvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                boolean zoomIn;
                if (e.getButton() == MouseEvent.BUTTON1)
                    zoomIn = true;
                else if (e.getButton() == MouseEvent.BUTTON3)
                    zoomIn = false;
                else
                    return;
                double mult = zoomMult;
                if (!zoomIn) mult = 1 / mult;
                zoom(e.getX(), e.getY(), mult);
            }

            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                canvasPrevMouseX = e.getX();
                canvasPrevMouseY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                if (movingCenter) {
                    movingCenter = false;
                    BufferedImage b2 = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_RGB);
                    b2.getGraphics().drawImage(b, (int)canvasCurOffsetX, (int)canvasCurOffsetY, null);
                    b = b2;
                    centerX -= canvasCurOffsetX / zoom;
                    centerY -= canvasCurOffsetY / zoom;
                    canvasCurOffsetX = 0;
                    canvasCurOffsetY = 0;
                    canvas.repaint();
                    drawMS();
                }
            }
        });

        canvas.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom(e.getX(), e.getY(), Math.pow(zoomMultScroll, -e.getWheelRotation()));
                e.consume();
            }
        });

        b = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    public void zoom(int mouseX, int mouseY, Double zoomAmount) {
        centerX = (mouseX - b.getWidth() / 2) / zoom + centerX;
        centerY = (mouseY - b.getHeight() / 2) / zoom + centerY;

        zoom *= zoomAmount;

        centerX -= (mouseX - b.getWidth() / 2) / zoom;
        centerY -= (mouseY - b.getHeight() / 2) / zoom;
        updateTitle(centerX, centerY);

        BufferedImage b2 = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_RGB);
        int x = (int)(zoomAmount * -mouseX + mouseX);
        int y = (int)(zoomAmount * -mouseY + mouseY);
        int x2 = (int)((b.getWidth() - mouseX) * zoomAmount + mouseX);
        int y2 = (int)((b.getHeight() - mouseY) * zoomAmount + mouseY);
        Graphics2D g = (Graphics2D) b2.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.clearRect(0, 0, b2.getWidth(), b2.getHeight());
        g.drawImage(b, x, y, x2 - x, y2 - y, null);
        b = b2;

        drawMS();
    }

    public void updateTitle(double x, double y) {
        setTitle(Math.round(zoom) + "% - (" + x + ", " + y + ")");
    }
}
