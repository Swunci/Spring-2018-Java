package dataprocessors;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private double yTotal, xMin, xMax;
    private int counter = 0;


    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  }
                  catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        yTotal = 0;
        counter = 0;
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());

                XYChart.Data<Number, Number> seriesData = new XYChart.Data<>(point.getX(), point.getY());
                seriesData.setNode(new HoverNode(entry.getKey()));
                series.getData().add(seriesData);

                yTotal += point.getY();
                if (counter == 0) {
                    xMax = point.getX();
                    xMin = point.getY();
                } else {
                    if (point.getX() > xMax)
                        xMax = point.getX();
                    if (point.getX() < xMin)
                        xMin = point.getX();
                }
                counter++;
            });
            chart.getData().add(series);
            Node line = series.getNode().lookup(".chart-series-line");
            line.setStyle("-fx-stroke: transparent;");
        }
        if (counter > 0) {
            double yAverage = yTotal / (double) counter;
            XYChart.Series<Number, Number> average = new XYChart.Series<>();
            average.setName("Average y-value");
            if (xMin == xMax) {
                xMin -= xMin / 2.0;
                xMax += xMax / 2.0;
            }
            average.getData().add(new XYChart.Data<>(xMin, yAverage));
            average.getData().add(new XYChart.Data<>(xMax, yAverage));
            chart.getData().add(average);
            average.getNode().toBack();
            for (XYChart.Data<Number, Number> data : average.getData()) {
                StackPane nodes = (StackPane) data.getNode();
                nodes.setVisible(false);
            }
        }
    }

    class HoverNode extends StackPane {          //node structure and action for data points
        HoverNode(String name) {
            Tooltip label = new Tooltip(name);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Node node = (Node) mouseEvent.getSource();
                    Tooltip.install(node, label);
                    setCursor(Cursor.CROSSHAIR);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                }
            });
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
}
