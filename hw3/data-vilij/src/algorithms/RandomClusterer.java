package algorithms;

import data.DataSet;
import javafx.application.Platform;
import settings.AppPropertyTypes;
import ui.AppUI;
import ui.DataVisualizer;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

import static settings.AppPropertyTypes.RUN_BUTTON_TEXT;


public class RandomClusterer extends Clusterer {

    ApplicationTemplate applicationTemplate = DataVisualizer.getApplicationTemplate();

    private DataSet dataset;

    private final int maxIterations;

    private final int updateInterval;

    private boolean stop = false;

    private final AtomicBoolean tocontinue;

    private boolean continuousRun;

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

    public boolean getContinuousRun() { return continuousRun; }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public RandomClusterer(DataSet dataSet, int maxIterations, int updateInterval, int numberOfClusters, boolean continuousRun) {
        super(numberOfClusters);
        dataset = dataSet;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.continuousRun = continuousRun;
        this.tocontinue = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        if(continuousRun) {
            continuousRun();
        }
        else {
            nonContinuousRun();
        }
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
            for (String instanceName : dataset.getLabels().keySet()) {
                dataset.updateLabel(instanceName, String.valueOf((int) (Math.random() * numberOfClusters)));
            }
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: \n", i);
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart(dataset.getLabels(), dataset.getLocations());
                });
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).algorithmFinished();
    }

    private void nonContinuousRun() {
        for (int i = 1; i <= maxIterations; i++) {
            for (String instanceName : dataset.getLabels().keySet()) {
                dataset.updateLabel(instanceName, String.valueOf((int) (Math.random() * numberOfClusters)));
            }
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: \n", i);
                PropertyManager manager = applicationTemplate.manager;
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).updateChart(dataset.getLabels(), dataset.getLocations());
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
        }
        ((AppUI) applicationTemplate.getUIComponent()).algorithmFinished();
    }
}
