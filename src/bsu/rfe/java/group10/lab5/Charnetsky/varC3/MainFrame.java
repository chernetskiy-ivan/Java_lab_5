package bsu.rfe.java.group10.lab5.Charnetsky.varC3;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private JFileChooser fileChooser = null;
    private JMenuItem addSecondGraphicsMenuItem;
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private JCheckBoxMenuItem rotateOnOrOff;
    private GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;
    public MainFrame() {
        super("Построение графиков функций на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);
        setExtendedState(MAXIMIZED_BOTH);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile(), "Открыть файл с графиком");
            }
        };
        fileMenu.add(openGraphicsAction);

        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);
        Action openGraphics2Action = new AbstractAction("Добавить второй график"){
            public void actionPerformed(ActionEvent event){
                if(fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile(), "Добавить второй график");
            }
        };
        addSecondGraphicsMenuItem = graphicsMenu.add(openGraphics2Action);
        addSecondGraphicsMenuItem.setEnabled(true);

        Action actionRotate = new AbstractAction("Повернуть на 90 градусов влево") {
            public void actionPerformed(ActionEvent e) {
                display.setWantRotate(rotateOnOrOff.isSelected());
            }
        };
        rotateOnOrOff = new JCheckBoxMenuItem(actionRotate);
        graphicsMenu.add(rotateOnOrOff);

        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);


        showAxisMenuItem.setSelected(true);
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };

        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);
        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile, String Flag) {
        try {

            DataInputStream in = new DataInputStream(new
                    FileInputStream(selectedFile));
            Double[][] graphicsData = new Double[in.available()/(Double.SIZE/8)/2][];
            int i = 0;
            while (in.available()>0) {
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[] {x, y};
            }

            if(Flag.equals("Открыть файл с графиком")) {
                if (graphicsData.length > 0) {
                    fileLoaded = true;
                    display.showGraphics(graphicsData);
                }
            }
            if(Flag.equals("Добавить второй график")){
                if (graphicsData.length > 0) {
                    fileLoaded = true;
                    display.addNewAndRepaint(graphicsData);
                    addSecondGraphicsMenuItem.setEnabled(false);
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private class GraphicsMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            rotateOnOrOff.setEnabled(fileLoaded);
            addSecondGraphicsMenuItem.setEnabled(fileLoaded);
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }
}