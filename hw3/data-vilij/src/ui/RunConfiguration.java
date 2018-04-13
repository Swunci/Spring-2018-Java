package ui;

import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class RunConfiguration {

    ApplicationTemplate applicationTemplate = new ApplicationTemplate();
    private Stage settings;
    private int iterations = 10;
    private int updateInterval = 1;
    private int labels = 2;
    private boolean continuousRun = true;
    private TextField iterationsTF;
    private TextField updateIntervalTF;
    private CheckBox continuousRunCheckBox;
    private TextField numOfLabelsTF;
    private HBox mainPane;
    private Label numberOfLabels;

    public int getInterations() {
        return iterations;
    }

    public int getUpdateInterval() {
        return updateInterval; }

    public boolean getContinuousRun() {
        return continuousRun;
    }

    public void setIterations(int value) {
        iterations = value;
    }

    public void setUpdateInterval(int value) {
        updateInterval = value;
    }

    public void setContinuousRun(boolean value) {
        continuousRun = value;
    }

    public RunConfiguration() {
        PropertyManager manager = applicationTemplate.manager;
        settings = new Stage();
        settings.initOwner(ApplicationTemplate.getMainStage());
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_TITLE.name()));
        settings.resizableProperty().setValue(Boolean.FALSE);
        mainPane = new HBox();
        layout();
        setCloseButtonActions();
        setGracefulDegradation();
        settings.setScene(new Scene(mainPane, 200, 110));
    }

    private void layout () {
        PropertyManager manager = applicationTemplate.manager;
        VBox leftPane = new VBox();
        VBox rightPane = new VBox();

        leftPane.setPadding(new Insets(10, 5, 10, 5));
        rightPane.setPadding(new Insets(5));
        leftPane.setSpacing(10);
        rightPane.setSpacing(5);
        Label label1 = new Label(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_ITERATIONS.name()));
        Label label2 = new Label(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_UPDATE_INTERVAL.name()));
        Label label3 = new Label(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_CONTINUOUS_RUN.name()));
        numberOfLabels = new Label(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_NUMBER_LABELS.name()));

        iterationsTF = new TextField();
        iterationsTF.setPrefWidth(50);
        updateIntervalTF = new TextField();
        updateIntervalTF.setPrefWidth(50);
        numOfLabelsTF = new TextField();
        numOfLabelsTF.setPrefWidth(50);
        continuousRunCheckBox = new CheckBox();

        displayCurrentSettings();
        leftPane.getChildren().addAll(label1, label2, label3, numberOfLabels);
        rightPane.getChildren().addAll(iterationsTF, updateIntervalTF, continuousRunCheckBox, numOfLabelsTF);

        mainPane.getChildren().addAll(leftPane, rightPane);
    }

    public void displayRunConfiguration () {
        settings.show();
    }

    private void displayCurrentSettings() {
        iterationsTF.setText(String.valueOf(iterations));
        updateIntervalTF.setText(String.valueOf(updateInterval));
        continuousRunCheckBox.setSelected(continuousRun);
        numOfLabelsTF.setText(String.valueOf(labels));
    }

    private void setGracefulDegradation() {
        iterationsTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (!newValue.trim().isEmpty()) {
                        Integer.parseInt(newValue);
                        if (Integer.parseInt(newValue) < 1) {
                            iterationsTF.setText(oldValue);
                        }
                    }
                } catch (Exception e) {
                    iterationsTF.setText(oldValue);
                }
            }
        });
        updateIntervalTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (!newValue.trim().isEmpty()) {
                        Integer.parseInt(newValue);
                        if (Integer.parseInt(newValue) < 1) {
                            updateIntervalTF.setText(oldValue);
                        }
                    }
                } catch (Exception e) {
                    updateIntervalTF.setText(oldValue);
                }
            }
        });
        numOfLabelsTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (!newValue.trim().isEmpty()) {
                        Integer.parseInt(newValue);
                        if (Integer.parseInt(newValue) < 1 || Integer.parseInt(newValue) > 4) {
                            numOfLabelsTF.setText(oldValue);
                        }
                    }
                } catch (Exception e) {
                    numOfLabelsTF.setText(oldValue);
                }
            }
        });
    }

    public void hideNumberOfLabels() {
        numberOfLabels.setVisible(false);
        numOfLabelsTF.setVisible(false);
    }

    private void setCloseButtonActions() {
        settings.setOnCloseRequest(event -> {
            if (iterationsTF.getText().trim().isEmpty() || updateIntervalTF.getText().trim().isEmpty() || numOfLabelsTF.getText().trim().isEmpty()) {
                errorHandler();
                event.consume();
            }
        });
    }

    private void errorHandler() {
        PropertyManager manager = applicationTemplate.manager;
        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        dialog.setResizable(false);
        dialog.setWidth(270);
        dialog.setHeight(150);
        String errTitle = manager.getPropertyValue(AppPropertyTypes.EMPTY_FIELD.name());
        String errMsg = manager.getPropertyValue(AppPropertyTypes.RUN_CONFIGURATION_ERROR.name());
        dialog.show(errTitle, errMsg);
    }
}
