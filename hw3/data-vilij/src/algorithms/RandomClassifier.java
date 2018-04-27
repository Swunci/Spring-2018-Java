package algorithms;

import actions.AppActions;
import algorithms.Classifier;
import data.DataSet;

import javafx.application.Platform;
import settings.AppPropertyTypes;
import ui.AppUI;
import ui.DataVisualizer;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static settings.AppPropertyTypes.RUN_BUTTON_TEXT;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    ApplicationTemplate applicationTemplate = DataVisualizer.getApplicationTemplate();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    private boolean stop = false;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {
        if (tocontinue()) {
            continuousRun();
        }
        else {
            nonContinuousRun();
        }
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    }

    private void continuousRun() {
        for (int i = 1; i <= maxIterations; i++) {
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
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant     = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart();
                });
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
                break;
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        if (!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData()) {
            ((AppUI) applicationTemplate.getUIComponent()).getDoneEditButton().setDisable(false);
        }
        ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getAlgorithmTypePane().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getSelectionPane().setDisable(false);
    }

    private void nonContinuousRun() {
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant     = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
                Platform.runLater(() -> {
                    PropertyManager manager = applicationTemplate.manager;
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart();
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText(manager.getPropertyValue(AppPropertyTypes.RESUME_BUTTON_TEXT.name()));
                    if (((AppUI) applicationTemplate.getUIComponent()).getRunningThread() == null) {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));
                    }
                });
                if (i != maxIterations) {
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
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
                break;
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        if (!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData()) {
            ((AppUI) applicationTemplate.getUIComponent()).getDoneEditButton().setDisable(false);
        }
        ((AppUI) applicationTemplate.getUIComponent()).getAlgorithmTypePane().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getSelectionPane().setDisable(false);
    }
}