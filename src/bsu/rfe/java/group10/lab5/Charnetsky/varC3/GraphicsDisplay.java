package bsu.rfe.java.group10.lab5.Charnetsky.varC3;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private ArrayList<Double[]> graphicsData2 = null;
    private ArrayList<Double[]> originalData2 = null;

    private int selectedMarker = -1;
    private int selectedMarker2 = -1;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double[][] viewport = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private double scaleX;
    private double scaleY;
    private double scale;

    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;

    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke gridStroke;
    private BasicStroke graphicsStroke2;
    private BasicStroke markerStroke2;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private Font labelsFont;
    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
    private boolean scaleMode = false;
    private boolean changeMode = false;
    private double[] originalPoint = new double[2];
    private java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double();


    public GraphicsDisplay() {
        this.setBackground(Color.WHITE);
        this.axisStroke = new BasicStroke(2.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{9, 2, 9, 2, 9, 2, 3, 2, 3, 2, 3, 2}, 0.0f);
        this.gridStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{4.0F, 4.0F}, 0.0F);
        this.markerStroke = new BasicStroke(1.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);
        // Перо для рисования второго графика
        graphicsStroke2 = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        // Перо для рисования контуров маркеров второго графика
        markerStroke2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 10);
        formatter.setMaximumFractionDigits(5);
        this.addMouseListener(new GraphicsDisplay.MouseHandler());
        this.addMouseMotionListener(new GraphicsDisplay.MouseMotionHandler());
    }

    public void showGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }

        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void addNewAndShowGraphics(ArrayList<Double[]> graphicsData2) {
        this.graphicsData2 = graphicsData2;
        this.originalData2 = new ArrayList(graphicsData2.size());
        Iterator var3 = graphicsData2.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData2.add(newPoint);
        }

        for(int i = 1; i < graphicsData2.size(); ++i) {
            if (((Double[])graphicsData2.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData2.get(i))[1];
            }

            if (((Double[])graphicsData2.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData2.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        scale = Math.min(scaleX, scaleY);
        if (scale==scaleX) {
            /* Если за основу был взят масштаб по оси X, значит по оси Y
            делений меньше, т.е. подлежащий отображению диапазон по Y будет меньше
            высоты окна. Значит необходимо добавить делений, сделаем это так:
            1) Вычислим, сколько делений влезет по Y при выбранном масштабе -
            getSize().getHeight()/scale;
            2) Вычтем из этого значения сколько делений требовалось изначально;
            3) Набросим по половине недостающего расстояния на maxY и minY */
            double yIncrement;
            //if (!rotation)
            yIncrement = (getSize().getHeight()/scale - (this.viewport[0][1] - this.viewport[1][1])) / 2;
            //else
            //    yIncrement = (getSize().getWidth()/scale - (maxY - minY)) / 2;

            //maxY += yIncrement;
            //minY -= yIncrement;
            viewport[0][1] += yIncrement;
            viewport[1][1] += yIncrement;
            //System.out.println("Choosed scaleX");
        }
        if (scale==scaleY) {
            // Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement;
            //if(!rotation)
            xIncrement = (getSize().getWidth()/scale- (this.viewport[1][0] - this.viewport[0][0]) )/2;
            //else
            //    xIncrement = (getSize().getHeight()/scale-(maxX-minX))/2;
            //maxX += xIncrement;
            //minX -= xIncrement;
            viewport[1][0] += xIncrement;
            viewport[0][0] -= xIncrement;

            //System.out.println("Choosed scaleY");
        }
        //zoomToRegion();
        if (this.graphicsData != null && this.graphicsData.size() != 0) {
            Graphics2D canvas = (Graphics2D)g;
            //this.paintGrid(canvas);
            if (showAxis) paintAxis(canvas);
            this.paintGraphics(canvas);
            if (showMarkers) paintMarkers(canvas);
            paintLabels(canvas);
            this.paintSelection(canvas);
        }
    }

    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
    }

    private void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        Iterator var5 = this.graphicsData.iterator();

        while(var5.hasNext()) {
            Double[] point = (Double[])var5.next();
            if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                if (currentX != null && currentY != null) {
                    canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                }

                currentX = point[0];
                currentY = point[1];
            }
        }

        if(graphicsData2 != null)
        {
            canvas.setStroke(graphicsStroke2);
            canvas.setColor(Color.ORANGE);
            Iterator var6 = this.graphicsData2.iterator();
            currentX = null;
            currentY = null;
            while (var6.hasNext()) {
                Double[] point = (Double[]) var6.next();
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    if (currentX != null && currentY != null) {
                        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                    }

                    currentX = point[0];
                    currentY = point[1];
                }
            }
        }

    }

    private void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);

        java.awt.geom.Ellipse2D.Double lastMarker = null;
        Line2D.Double lastLineVertical = null;
        Line2D.Double lastLineHorizontal = null;
        java.awt.geom.Ellipse2D.Double lastMarker2 = null;
        Line2D.Double lastLineVertical2 = null;
        Line2D.Double lastLineHorizontal2 = null;
        int i = -1;
        Iterator var5 = this.graphicsData.iterator();

        while(var5.hasNext()) {
            Double[] point = (Double[])var5.next();
            ++i;
            if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                if (SumOfNumbersInCeilPartOfPointLowerThanTEN(point[1])) {
                    //System.out.println(point[1] + ": " + true);
                    canvas.setColor(Color.BLUE);
                } else {
                    //System.out.println(point[1] + ": " + false);
                    canvas.setColor(Color.RED);
                }
                double radius;
                if (i == this.selectedMarker) {
                    radius = 7;
                    //canvas.setColor(Color.GREEN);
                } else {
                    radius = 5.5;
                }

                java.awt.geom.Ellipse2D.Double marker = new java.awt.geom.Ellipse2D.Double();
                Point2D center = this.translateXYtoPoint(point[0], point[1]);
                Point2D corner = new java.awt.geom.Point2D.Double(center.getX() + radius, center.getY() + radius);
                Line2D.Double lineVertical = new Line2D.Double();
                Line2D.Double lineHorizontal = new Line2D.Double();
                lineVertical.setLine(center.getX(), center.getY() - radius, center.getX(), center.getY() + radius);
                lineHorizontal.setLine(center.getX() - radius, center.getY(), center.getX() + radius, center.getY());
                marker.setFrameFromCenter(center, corner);
                if (i == this.selectedMarker) {
                    lastMarker = marker;
                    lastLineVertical = lineVertical;
                    lastLineHorizontal = lineHorizontal;
                } else {
                    canvas.draw(marker);
                    canvas.draw(lineHorizontal);
                    canvas.draw(lineVertical);
                    //canvas.fill(marker);
                }
            }
        }
        if(graphicsData2 != null) {
            Iterator var6 = this.graphicsData2.iterator();
            i = -1;
            while (var6.hasNext()) {
                Double[] point = (Double[]) var6.next();
                ++i;
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    if (SumOfNumbersInCeilPartOfPointLowerThanTEN(point[1])) {
                        //System.out.println(point[1] + ": " + true);
                        canvas.setColor(Color.BLUE);
                    } else {
                        //System.out.println(point[1] + ": " + false);
                        canvas.setColor(Color.RED);
                    }
                    double radius;
                    if (i == this.selectedMarker2) {
                        radius = 7;
                        //canvas.setColor(Color.GREEN);
                    } else {
                        radius = 5.5;
                    }

                    java.awt.geom.Ellipse2D.Double marker = new java.awt.geom.Ellipse2D.Double();
                    Point2D center = this.translateXYtoPoint(point[0], point[1]);
                    Point2D corner = new java.awt.geom.Point2D.Double(center.getX() + radius, center.getY() + radius);
                    Line2D.Double lineVertical = new Line2D.Double();
                    Line2D.Double lineHorizontal = new Line2D.Double();
                    lineVertical.setLine(center.getX(), center.getY() - radius, center.getX(), center.getY() + radius);
                    lineHorizontal.setLine(center.getX() - radius, center.getY(), center.getX() + radius, center.getY());
                    marker.setFrameFromCenter(center, corner);
                    if (i == this.selectedMarker2) {
                        lastMarker2 = marker;
                        lastLineVertical2 = lineVertical;
                        lastLineHorizontal2 = lineHorizontal;
                    } else {
                        canvas.draw(marker);
                        canvas.draw(lineHorizontal);
                        canvas.draw(lineVertical);
                        //canvas.fill(marker);
                    }
                }
            }
        }

        if (lastMarker != null || lastMarker2 != null) {
            canvas.setColor(Color.GREEN);
            //canvas.setPaint(Color.GREEN);
            if(lastMarker != null) {
                canvas.draw(lastMarker);
                canvas.draw(lastLineHorizontal);
                canvas.draw(lastLineVertical);
            }
            if(lastMarker2 != null) {
                canvas.draw(lastMarker2);
                canvas.draw(lastLineHorizontal2);
                canvas.draw(lastLineVertical2);
            }
            //canvas.fill(lastMarker);
        }

    }

    private void paintLabels(Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        if (this.viewport[1][1] < 0.0D && this.viewport[0][1] > 0.0D) {
            labelYPos = 0.0D;
        } else {
            labelYPos = this.viewport[1][1];
        }

        double labelXPos;
        if (this.viewport[0][0] < 0.0D && this.viewport[1][0] > 0.0D) {
            labelXPos = 0.0D;
        } else {
            labelXPos = this.viewport[0][0];
        }

        double pos = this.viewport[0][0];


        double step;
        java.awt.geom.Point2D.Double point;
        String label;
        Rectangle2D bounds;
        if (showAxis) {
            for (step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
                point = this.translateXYtoPoint(pos, labelYPos);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float) (point.getX() + 5.0D), (float) (point.getY() - bounds.getHeight()));
            }

            pos = this.viewport[1][1];

            for (step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
                point = this.translateXYtoPoint(labelXPos, pos);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float) (point.getX() + 5.0D), (float) (point.getY() - bounds.getHeight()));
            }
        }
        if (showMarkers) {
            if (this.selectedMarker >= 0 || selectedMarker2 >= 0) {
                if(this.selectedMarker >= 0)
                {
                    point = this.translateXYtoPoint(((Double[]) this.graphicsData.get(this.selectedMarker))[0], ((Double[]) this.graphicsData.get(this.selectedMarker))[1]);
                    label = "X=" + formatter.format(((Double[]) this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[]) this.graphicsData.get(this.selectedMarker))[1]);
                    bounds = this.labelsFont.getStringBounds(label, context);
                    canvas.setColor(Color.DARK_GRAY);
                    canvas.drawString(label, (float) (point.getX() + 5.0D), (float) (point.getY() - bounds.getHeight()));
                }
                if(selectedMarker2 >= 0)
                {
                    point = this.translateXYtoPoint(((Double[]) this.graphicsData2.get(this.selectedMarker2))[0], ((Double[]) this.graphicsData2.get(this.selectedMarker2))[1]);
                    label = "X=" + formatter.format(((Double[]) this.graphicsData2.get(this.selectedMarker2))[0]) + ", Y=" + formatter.format(((Double[]) this.graphicsData2.get(this.selectedMarker2))[1]);
                    bounds = this.labelsFont.getStringBounds(label, context);
                    canvas.setColor(Color.DARK_GRAY);
                    canvas.drawString(label, (float) (point.getX() + 5.0D), (float) (point.getY() - bounds.getHeight()));
                }
            }
        }

    }

    private void paintGrid(Graphics2D canvas) {
        canvas.setStroke(this.gridStroke);
        canvas.setColor(Color.GRAY);
        double pos = this.viewport[0][0];

        double step;
        for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(pos, this.viewport[0][1]), this.translateXYtoPoint(pos, this.viewport[1][1])));
        }

        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));
        pos = this.viewport[1][1];

        for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], pos), this.translateXYtoPoint(this.viewport[1][0], pos)));
        }

        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));
    }

    private void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        Rectangle2D bounds;
        java.awt.geom.Point2D.Double labelPos;
        if (this.viewport[0][0] <= 0.0D && this.viewport[1][0] >= 0.0D) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(0.0D, this.viewport[0][1]), this.translateXYtoPoint(0.0D, this.viewport[1][1])));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(-(this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D), this.translateXYtoPoint(0.0D, this.viewport[0][1])));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint((this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D), this.translateXYtoPoint(0.0D, this.viewport[0][1])));
            bounds = this.axisFont.getStringBounds("y", context);
            labelPos = this.translateXYtoPoint(0.0D, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0D));
        }

        if (this.viewport[1][1] <= 0.0D && this.viewport[0][1] >= 0.0D) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], 0.0D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, (this.viewport[0][1] - this.viewport[1][1]) * 0.005D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, -(this.viewport[0][1] - this.viewport[1][1]) * 0.005D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));
            bounds = this.axisFont.getStringBounds("x", context);
            labelPos = this.translateXYtoPoint(this.viewport[1][0], 0.0D);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0D), (float)(labelPos.y - bounds.getHeight() / 2.0D));
        }

    }

    protected Point2D.Double translateXYtoPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[0][1] - y;
        return new java.awt.geom.Point2D.Double(deltaX * this.scale, deltaY * this.scale);
    }

    protected double[] translatePointToXY(int x, int y) {
        return new double[]{this.viewport[0][0] + (double)x / this.scale, this.viewport[0][1] - (double)y / this.scale};
    }

    protected int findSelectedPointInFirstGraphic(int x, int y) {
        if (this.graphicsData == null) {
            return -1;
        } else {
            int pos = 0;

            for(Iterator var5 = this.graphicsData.iterator(); var5.hasNext(); ++pos) {
                Double[] point = (Double[])var5.next();
                java.awt.geom.Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0D) {
                    return pos;
                }
            }

            return -1;
        }
    }

    protected int findSelectedPointInSecondGraphic(int x, int y) {
        if (this.graphicsData2 == null) {
            return -1;
        } else {
            int pos = 0;

            for(Iterator var6 = this.graphicsData2.iterator(); var6.hasNext(); ++pos) {
                Double[] point = (Double[]) var6.next();
                java.awt.geom.Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0D) {
                    return pos;
                }
            }
            return -1;
        }
    }

    public ArrayList<Double[]> getGraphicsData() {
        return graphicsData;
    }

    public void reset() {
        this.showGraphics(this.originalData);
        this.setShowMarkers(true);
        this.setShowAxis(true);
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
    }

    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    GraphicsDisplay.this.viewport = (double[][])GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1);
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                } else {
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY, GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }

                GraphicsDisplay.this.repaint();
            }

        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPointInFirstGraphic(ev.getX(), ev.getY());
                GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                if (GraphicsDisplay.this.selectedMarker >= 0) {
                    GraphicsDisplay.this.changeMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
                } else {
                    GraphicsDisplay.this.scaleMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                    GraphicsDisplay.this.selectionRect.setFrame((double)ev.getX(), (double)ev.getY(), 1.0D, 1.0D);
                }

            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
                if (GraphicsDisplay.this.changeMode) {
                    GraphicsDisplay.this.changeMode = false;
                } else {
                    GraphicsDisplay.this.scaleMode = false;
                    double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                    GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);
                    GraphicsDisplay.this.viewport = new double[2][2];
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                    GraphicsDisplay.this.repaint();
                }

            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            selectedMarker = findSelectedPointInFirstGraphic(ev.getX(), ev.getY());
            selectedMarker2 = findSelectedPointInSecondGraphic(ev.getX(), ev.getY());
            if (selectedMarker >= 0 || selectedMarker2 >= 0) {
                setCursor(Cursor.getPredefinedCursor(8));
            } else {
                setCursor(Cursor.getPredefinedCursor(0));
            }

            repaint();
        }

        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] + (currentPoint[1] - ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1]);
                if (newY > GraphicsDisplay.this.viewport[0][1]) {
                    newY = GraphicsDisplay.this.viewport[0][1];
                }

                if (newY < GraphicsDisplay.this.viewport[1][1]) {
                    newY = GraphicsDisplay.this.viewport[1][1];
                }

                ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] = newY;
                GraphicsDisplay.this.repaint();
            } else {
                double width = (double)ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0D) {
                    width = 5.0D;
                }

                double height = (double)ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0D) {
                    height = 5.0D;
                }

                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);
                GraphicsDisplay.this.repaint();
            }

        }
    }

    private boolean SumOfNumbersInCeilPartOfPointLowerThanTEN(Double number) {
        Double numberCeil = Math.abs(Math.ceil(number));
        if((Double)numberCeil - number != 0)
        {
            numberCeil -= 1;
        }
        int sum = 0;
        if(numberCeil < 10)
        {
            return true;
        }
        else if(numberCeil >= 10)
        {
            while(numberCeil.intValue() > 0)
            {
                sum += numberCeil%10;
                numberCeil /= 10;
            }
            if(sum < 10)
                return true;
            else
                return false;
        }
        return false;
    }
}