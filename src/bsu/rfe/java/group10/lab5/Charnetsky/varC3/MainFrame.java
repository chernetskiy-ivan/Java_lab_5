package bsu.rfe.java.group10.lab5.Charnetsky.varC3;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class MainFrame extends JFrame {
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;
    private JFileChooser fileChooser = null;
    private JMenuItem resetGraphicsMenuItem;
    private GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;
    private int amountOfLoadedGraphics = 0;

    private JCheckBoxMenuItem showMarkersMenuItem;
    private JCheckBoxMenuItem showAxisMenuItem;
    private JMenuItem addGraphicsMenuItem;
    //private JCheckBoxMenuItem rotateMenuItem;

    // Класс-слушатель событий, связанных с отображением меню
    private class GraphicsMenuListener implements MenuListener {
        // Обработчик, вызываемый перед показом меню
        public void menuSelected(MenuEvent e) {
            // Доступность или недоступность элементов меню "График" определяется загруженностью данных
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            //rotateMenuItem.setEnabled(fileLoaded);
            if(amountOfLoadedGraphics > 0 && amountOfLoadedGraphics < 2)
                addGraphicsMenuItem.setEnabled(true);
            else
                addGraphicsMenuItem.setEnabled(false);
        }
        // Обработчик, вызываемый после того, как меню исчезло с экрана
        public void menuDeselected(MenuEvent e) {
        }
        // Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
        public void menuCanceled(MenuEvent e) {
        }
    }

    public MainFrame() {
        super("Обработка событий от мыши");
        this.setSize(700, 500);
        Toolkit kit = Toolkit.getDefaultToolkit();
        this.setLocation((kit.getScreenSize().width - 700) / 2, (kit.getScreenSize().height - 500) / 2);
        this.setExtendedState(6);
        amountOfLoadedGraphics = 0;

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent event) {
                if (MainFrame.this.fileChooser == null) {
                    MainFrame.this.fileChooser = new JFileChooser();
                    MainFrame.this.fileChooser.setCurrentDirectory(new File("."));
                }

                MainFrame.this.fileChooser.showOpenDialog(MainFrame.this);
                MainFrame.this.openGraphics(MainFrame.this.fileChooser.getSelectedFile(), "СОЗДАНИЕ ГРАФИКА");
            }
        };
        fileMenu.add(openGraphicsAction);

        Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.display.reset();
            }
        };
        this.resetGraphicsMenuItem = fileMenu.add(resetGraphicsAction);
        this.resetGraphicsMenuItem.setEnabled(false);

        // Создать пункт меню "График"
        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);
        // Создать действие для реакции на активацию элемента
        // "Показывать оси координат"
        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                // свойство showAxis класса GraphicsDisplay истина,
                // если элемент меню showAxisMenuItem отмечен флажком,
                // ложь - в противном случае
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        // Добавить соответствующий элемент в меню
        graphicsMenu.add(showAxisMenuItem);
        // Элемент по умолчанию включен (отмечен флажком)
        showAxisMenuItem.setSelected(true);

        // Повторить действия для элемента "Показывать маркеры точек"
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                // по аналогии с showAxisMenuItem
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        // Элемент по умолчанию выключен
        showMarkersMenuItem.setSelected(true);
        // Зарегистрировать обработчик событий, связанных с меню "График"
        graphicsMenu.addMenuListener(new GraphicsMenuListener());

        Action addGraphicsAction = new AbstractAction("Добавить функцию") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile(), "ДОБАВЛЕНИЕ ВТОРОГО ГРАФИКА");
            }
        };
        addGraphicsMenuItem = new JMenuItem(addGraphicsAction);
        graphicsMenu.add(addGraphicsMenuItem);
        addGraphicsMenuItem.setEnabled(false);

        this.getContentPane().add(this.display, "Center");
    }

    // Считывание данных графика из существующего файла
    protected void openGraphics(File selectedFile, String options) {
        try {
            // Шаг 1 - Открыть поток чтения данных, связанный с файлом
            DataInputStream in = new DataInputStream(
                    new FileInputStream(selectedFile));
            /* Шаг 2- Зная объѐм данных в потоке ввода можно вычислить,
             * сколько памяти нужно зарезервировать в массиве:
             * Всего байт в потоке - in.available() байт;
             * Размер числа Double - Double.SIZE бит, или Double.SIZE/8 байт;
             * Так как числа записываются парами, то число пар меньше в 2 раза
             */
            //Double[][] graphicsData = new Double[in.available()/(Double.SIZE/8)/2][];
            ArrayList graphicsData = new ArrayList(50);
            // Шаг 3 – Цикл чтения данных (пока в потоке есть данные)
            //int i = 0;
            while (in.available() > 0) {
                // Первой из потока читается координата точки X
                Double x = in.readDouble();
                // Затем - значение графика Y в точке X
                Double y = in.readDouble();
                // Прочитанная пара координат добавляется в массив
                graphicsData.add(new Double[]{x, y});
                //System.out.println("X: " + x + "\tY: " + y);
            }
            // Шаг 4 - Проверка, имеется ли в списке в результате чтения
            // хотя бы одна пара координат
            if (graphicsData != null && graphicsData.size() > 0) {
                // Да - установить флаг загруженности данных
                fileLoaded = true;
                this.resetGraphicsMenuItem.setEnabled(true);
                // Вызывать метод отображения графика
                if (options.equals("СОЗДАНИЕ ГРАФИКА")) {
                    display.showGraphics(graphicsData);
                    amountOfLoadedGraphics = 1;
                } else if (options.equals("ДОБАВЛЕНИЕ ВТОРОГО ГРАФИКА")) {
                    display.addNewAndShowGraphics(graphicsData);
                    amountOfLoadedGraphics++;
                }
            }
            // Шаг 5 - Закрыть входной поток
            in.close();
        } catch (FileNotFoundException ex) {
            // В случае исключительной ситуации типа "Файл не найден"
            // показать сообщение об ошибке
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Указанный файл не найден", "Ошибка загрузки данных",
                    JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException ex) {
            // В случае ошибки ввода из файлового потока
            // показать сообщение об ошибке
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Ошибка чтения координат точек из файла",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }
}
