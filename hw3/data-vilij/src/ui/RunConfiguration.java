package ui;

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

    private Stage settings;
    private int iterations = 10;
    private int updateInterval = 1;
    private boolean continuousRun = true;
    private TextField iterationsTF;
    private TextField updateIntervalTF;
    private CheckBox continuousRunCheckBox;
    private HBox mainPane;
    private Label numberOfLabels;

    public int getInterations() {
        return iterations;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

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

    public void setNumberOfLabelsText(String value) {
        numberOfLabels.setText(value);
    }

    public RunConfiguration() {
        settings = new Stage();
        settings.initOwner(ApplicationTemplate.getMainStage());
        settings.initModality(Modality.APPLICATION_MODAL);
        settings.setTitle("Run Configuration");
        settings.resizableProperty().setValue(Boolean.FALSE);
        mainPane = new HBox();
        layout();
        setGracefulDegradation();
        settings.setScene(new Scene(mainPane, 200, 110));
    }

    private void layout () {
        VBox leftPane = new VBox();
        VBox rightPane = new VBox();

        leftPane.setPadding(new Insets(10, 5, 10, 5));
        rightPane.setPadding(new Insets(5));
        leftPane.setSpacing(10);
        rightPane.setSpacing(5);
        Label label1 = new Label("Iterations:");
        Label label2 = new Label("Update Interval:");
        Label label3 = new Label("Continuous Run:");

        iterationsTF = new TextField();
        iterationsTF.setPrefWidth(50);
        updateIntervalTF = new TextField();
        updateIntervalTF.setPrefWidth(50);
        continuousRunCheckBox = new CheckBox();
        displayCurrentSettings();
        numberOfLabels = new Label("Wadu hek");
        leftPane.getChildren().addAll(label1, label2, label3, numberOfLabels);
        rightPane.getChildren().addAll(iterationsTF, updateIntervalTF, continuousRunCheckBox);

        mainPane.getChildren().addAll(leftPane, rightPane);
    }

    public void displayRunConfiguration () {
        settings.show();
    }

    private void displayCurrentSettings() {
        iterationsTF.setText(String.valueOf(iterations));
        updateIntervalTF.setText(String.valueOf(updateInterval));
        continuousRunCheckBox.setSelected(continuousRun);
    }

    private void setGracefulDegradation() {
        iterationsTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (!newValue.equals("")) {
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
                    if (!newValue.equals("")) {
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
    }

    public void hideNumberOfLabels() {
        numberOfLabels.setVisible(false);
    }
}
