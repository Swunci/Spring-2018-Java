package algorithms;

import data.DataSet;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import settings.AppPropertyTypes;
import ui.AppUI;
import ui.DataVisualizer;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static settings.AppPropertyTypes.RUN_BUTTON_TEXT;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    ApplicationTemplate applicationTemplate = DataVisualizer.getApplicationTemplate();

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private final boolean continuousRun;
    private boolean stop = false;

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean continuousRun) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
        this.continuousRun = continuousRun;
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    public boolean getContinuousRun() { return continuousRun; }
    @Override
    public void run() {
        if(continuousRun) {
            continuousRun();
        }
        else {
            nonContinuousRun();
        }
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i = (++i % instanceNames.size());
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

    private void continuousRun() {
        initializeCentroids();
        int iteration = 1;
        while (iteration++ <= maxIterations & tocontinue.get()) {
            if (stop) {
                try {
                    synchronized (((AppUI) applicationTemplate.getUIComponent()).getRunningThread()) {
                        Platform.runLater(() -> {
                            ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
                        });
                        ((AppUI) applicationTemplate.getUIComponent()).getRunningThread().wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assignLabels();
            if (iteration % updateInterval == 0) {
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart(dataset.getLabels(), dataset.getLocations());
                });
                System.out.printf("Iteration number %d: \n", iteration);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            recomputeCentroids();
        }
        ((AppUI) applicationTemplate.getUIComponent()).algorithmFinished();
    }

    private void nonContinuousRun() {
        initializeCentroids();
        int iteration = 1;
        while (iteration++ <= maxIterations & tocontinue.get()) {
            assignLabels();
            if (iteration % updateInterval == 0) {
                System.out.printf("Iteration number %d: \n", iteration);
                Platform.runLater(() -> {
                    PropertyManager manager = applicationTemplate.manager;
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart(dataset.getLabels(), dataset.getLocations());
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText(manager.getPropertyValue(AppPropertyTypes.RESUME_BUTTON_TEXT.name()));
                    if (((AppUI) applicationTemplate.getUIComponent()).getRunningThread() == null) {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));
                    }
                });
                if (iteration != maxIterations) {
                    try {
                        synchronized ((((AppUI) applicationTemplate.getUIComponent()).getRunningThread())) {
                            ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
                            ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                            ((AppUI) applicationTemplate.getUIComponent()).getRunningThread().wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recomputeCentroids();
        }
        ((AppUI) applicationTemplate.getUIComponent()).algorithmFinished();
    }
}