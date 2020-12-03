package bsu.rfe.java.group10.lab5.Charnetsky.varC3;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import javax.swing.JPanel;
import java.awt.geom.AffineTransform;

@SuppressWarnings("serials")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private Double[][] graphicsData2;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean wantRotate = false;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double scale;
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke graphicsStroke2;
    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {3,1,3,1,3,1,1,1,1,1,1}, 0.0f);

        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);

        graphicsStroke2 = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);

        graphicsData2 = new Double[][]{};
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        repaint();
    }

    public void addNewAndRepaint(Double[][] Data) {
        this.graphicsData2 = Data;
        repaint();
    }

    public void setWantRotate(boolean rotate){
        this.wantRotate = rotate;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        for (int i = 1; i < graphicsData2.length; i++) {
            if (graphicsData2[i][1] < minY) {
                minY = graphicsData2[i][1];
            }
            if (graphicsData2[i][1] > maxY) {
                maxY = graphicsData2[i][1];
            }
        }

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        scale = Math.min(scaleX, scaleY);

        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }

        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if(wantRotate) {
            AffineTransform rat = new AffineTransform();
            rat.setToTranslation(200, 1150);
            rat.rotate(-Math.PI / 2);
            canvas.transform(rat);
        }
        
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);

        canvas.setStroke(graphicsStroke2);
        canvas.setColor(Color.YELLOW);

        GeneralPath graphics2 = new GeneralPath();
        for (int i = 0; i < graphicsData2.length; i++) {
            Point2D.Double point2 = xyToPoint(graphicsData2[i][0],
                    graphicsData2[i][1]);
            if (i > 0) {
                graphics2.lineTo(point2.getX(), point2.getY());
            } else {
                graphics2.moveTo(point2.getX(), point2.getY());
            }
        }
        canvas.draw(graphics2);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            Ellipse2D.Double marker = new Ellipse2D.Double();

            double buffer = Math.abs(point[1]);
            double sum = 0.0;
            while(buffer >= 1){
                sum += buffer % 10;
                buffer /= 10;
            }

            if(sum > 10.0)
                canvas.setColor(Color.RED);
            else
                canvas.setColor(Color.BLUE);

            Point2D.Double center = xyToPoint(point[0], point[1]);
            Point2D.Double corner = shiftPoint(center, 5.5, 5.5);
            GeneralPath marker1 = new GeneralPath();
            marker.setFrameFromCenter(center, corner);
            marker1.moveTo(center.getX() - 5.5,center.getY());
            marker1.lineTo(center.getX() + 5.5,center.getY());
            marker1.moveTo(center.getX(), center.getY() - 5.5);
            marker1.lineTo(center.getX(), center.getY() + 5.5);
            canvas.draw(marker);
            canvas.draw(marker1);
        }

        for (Double[] point : graphicsData2) {
            Ellipse2D.Double marker = new Ellipse2D.Double();

            double buffer = Math.abs(point[1]);
            double sum = 0.0;
            while(buffer >= 1){
                sum += buffer % 10;
                buffer /= 10;
            }

            if(sum > 10.0)
                canvas.setColor(Color.RED);
            else
                canvas.setColor(Color.BLUE);

            Point2D.Double center = xyToPoint(point[0], point[1]);
            Point2D.Double corner = shiftPoint(center, 5.5, 5.5);

            GeneralPath marker1 = new GeneralPath();
            marker.setFrameFromCenter(center, corner);
            marker1.moveTo(center.getX() - 5.5,center.getY());
            marker1.lineTo(center.getX() + 5.5,center.getY());
            marker1.moveTo(center.getX(), center.getY() - 5.5);
            marker1.lineTo(center.getX(), center.getY() + 5.5);
            canvas.draw(marker);
            canvas.draw(marker1);
        }
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();

        if (minX <= 0.0 && maxX >= 0.0) {

            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();

            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }

        if (minY <= 0.0 && maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}