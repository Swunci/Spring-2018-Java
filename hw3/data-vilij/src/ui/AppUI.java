package ui;

import actions.AppActions;
import data.DataSet;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;                // toolbar button to take a screenshot of the data
    private Button doneEditButton;
    private Button runButton;
    private ChoiceBox algorithmTypes;
    private Text informationText;
    private TextArea textArea;                    // text area for new data input
    private boolean hasNewText;                   // whether or not the text area has any new data since last display
    private LineChart<Number, Number> chart;      // the chart where data will be displayed
    private VBox leftPane;
    private VBox algorithmTypePane;
    private HBox selectionPane;
    private ObservableList<String> choices;
    private String scrnshotIconPath;
    private String settingsIconPath;
    private ToggleGroup classificationRadioGroup = new ToggleGroup();
    private ToggleGroup clusteringRadioGroup = new ToggleGroup();
    private ArrayList<String> classificationAlgorithmNames;
    private ArrayList<String> clusteringAlgorithmNames;
    private RadioButton[] classificationRadioButton;
    private RadioButton[] clusteringRadioButton;
    private Button[] classificationConfigureButton;
    private Button[] clusteringConfigureButton;
    private ArrayList<String> classifierAlgorithmClassNames;
    private ArrayList<String> clustererAlgorithmClassNames;
    private Runnable algorithm = () -> {};
    private Thread runningThread;

    public Button getScrnshotButton() {
        return scrnshotButton;
    }

    public Button getDoneEditButton() {
        return doneEditButton;
    }

    public Button getRunButton() {
        return runButton;
    }

    public ChoiceBox getAlgorithmTypes() {
        return algorithmTypes;
    }

    public Text getInformationText() {
        return informationText;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public VBox getAlgorithmTypePane() {
        return algorithmTypePane;
    }

    public HBox getSelectionPane() {
        return selectionPane;
    }

    public ObservableList<String> getChoices() {
        return choices;
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public Runnable getAlgorithm() {
        return algorithm;
    }

    public Thread getRunningThread() {
        return runningThread;
    }

    public void disableNewButton(boolean value) {
        newButton.setDisable(value);
    }

    public void disableSaveButton(boolean value) {
        saveButton.setDisable(value);
    }

    public void disableScrnshotButton(boolean value) {
        scrnshotButton.setDisable(value);
    }

    public void disableDoneEditButton(boolean value) {
        doneEditButton.setDisable(value);
    }

    public void setAlgorithmTypes(ChoiceBox choices) {
        algorithmTypes = choices;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public void setInformationText(String information) {
        informationText.setText(information);
    }

    public void setChoices(ObservableList<String> choices) {
        this.choices = choices;
    }

    public void setAlgorithm(Runnable algorithm) {
        this.algorithm = algorithm;
    }

    public void setRunningThread(Thread runningThread) {
        this.runningThread = runningThread;
    }

    public AppUI() { }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        super.setResourcePaths(applicationTemplate);
        String iconsPath = SEPARATOR + String.join(SEPARATOR, manager.getPropertyValue(GUI_RESOURCE_PATH.name()), manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshotIconPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        settingsIconPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SETTINGS_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        scrnshotButton = setToolbarButton(scrnshotIconPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()),true);
        toolBar = new ToolBar(newButton, saveButton, loadButton, scrnshotButton, exitButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() {
        return textArea.getText();
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(CHART_TITLE.name()));
        chart.setAnimated(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalZeroLineVisible(false);

        textArea = new TextArea();
        textArea.setPrefSize(240, 190);
        chart.setPrefSize(700, 550);
        informationText = new Text();
        doneEditButton = new Button(manager.getPropertyValue(DONE_BUTTON_TEXT.name()));
        runButton = new Button(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));

        classificationAlgorithmNames = new ArrayList<>();
        Class appPropertyTypesClass = AppPropertyTypes.class;
        for (Object o: appPropertyTypesClass.getEnumConstants()) {
            if (!(o.toString().toLowerCase().equals(manager.getPropertyValue(CLASSIFICATION.name()).toLowerCase()))) {
                if (o.toString().toLowerCase().contains(manager.getPropertyValue(CLASSIFICATION.name()).toLowerCase())) {
                    classificationAlgorithmNames.add(manager.getPropertyValue(((Enum) o).name()));
                }
            }
        }

        clusteringAlgorithmNames = new ArrayList<>();
        for (Object o: appPropertyTypesClass.getEnumConstants()) {
            if (!(o.toString().toLowerCase().equals(manager.getPropertyValue(CLUSTERING.name()).toLowerCase()))) {
                if (o.toString().toLowerCase().contains(manager.getPropertyValue(CLUSTERING.name()).toLowerCase())) {
                    clusteringAlgorithmNames.add(manager.getPropertyValue(((Enum) o).name()));
                }
            }
        }

        classificationRadioButton = new RadioButton[classificationAlgorithmNames.size()];
        classificationConfigureButton = new Button[classificationAlgorithmNames.size()];
        for (int i = 0; i < classificationAlgorithmNames.size(); i++) {
            classificationRadioButton[i] = new RadioButton(classificationAlgorithmNames.get(i));
            classificationConfigureButton[i] = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
            classificationConfigureButton[i].setUserData(new RunConfiguration());
            classificationRadioButton[i].setUserData(classificationConfigureButton[i]);
            classificationRadioButton[i].setToggleGroup(classificationRadioGroup);
        }

        clusteringRadioButton = new RadioButton[clusteringAlgorithmNames.size()];
        clusteringConfigureButton = new Button[clusteringAlgorithmNames.size()];
        for (int i = 0; i < clusteringAlgorithmNames.size(); i++) {
            clusteringRadioButton[i] = new RadioButton(clusteringAlgorithmNames.get(i));
            clusteringConfigureButton[i] = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
            clusteringConfigureButton[i].setUserData(new RunConfiguration());
            clusteringRadioButton[i].setUserData(clusteringConfigureButton[i]);
            clusteringRadioButton[i].setToggleGroup(clusteringRadioGroup);
        }

        classifierAlgorithmClassNames = new ArrayList<>();
        for (Object o: appPropertyTypesClass.getEnumConstants()) {
            if (!(o.toString().toLowerCase().equals(manager.getPropertyValue(CLASSIFIER.name()).toLowerCase()))) {
                if (o.toString().toLowerCase().contains(manager.getPropertyValue(CLASSIFIER.name()).toLowerCase())) {
                    classifierAlgorithmClassNames.add(manager.getPropertyValue(((Enum) o).name()));
                }
            }
        }

        clustererAlgorithmClassNames = new ArrayList<>();
        for (Object o: appPropertyTypesClass.getEnumConstants()) {
            if (!(o.toString().toLowerCase().equals(manager.getPropertyValue(CLUSTERER.name()).toLowerCase()))) {
                if (o.toString().toLowerCase().contains(manager.getPropertyValue(CLUSTERER.name()).toLowerCase())) {
                    clustererAlgorithmClassNames.add(manager.getPropertyValue(((Enum) o).name()));
                }
            }
        }

        leftPane = new VBox();

        VBox informationPane = new VBox();
        algorithmTypePane = new VBox();
        selectionPane = new HBox();
        VBox rightPane = new VBox();
        HBox mainPane = new HBox();

        Text leftPaneTitle = new Text(manager.getPropertyValue(LEFT_PANE_TITLE.name()));
        String fontName = manager.getPropertyValue(LEFT_PANE_TITLE_FONT.name());
        Double fontSize = Double.parseDouble(manager.getPropertyValue(LEFT_PANE_TITLE_SIZE.name()));
        leftPaneTitle.setFont(Font.font(fontName, fontSize));

        leftPane.setAlignment(Pos.TOP_CENTER);
        leftPane.getChildren().addAll(leftPaneTitle, textArea, doneEditButton, informationPane, algorithmTypePane, selectionPane, runButton);
        leftPane.setPadding(new Insets(20));
        leftPane.setSpacing(5);

        informationPane.getChildren().add(informationText);
        informationText.wrappingWidthProperty().bind(textArea.widthProperty());

        Text algorithmTypePaneText = new Text(manager.getPropertyValue(ALGORITHM_TYPE_PANE_TITLE.name()));
        String algorithmTypePaneFont = manager.getPropertyValue(ALGORITHM_TYPE_PANE_FONT.name());
        Double algorithmTypePaneFontSize = Double.parseDouble(manager.getPropertyValue(ALGORITHM_TYPE_PANE_FONT_SIZE.name()));
        algorithmTypePaneText.setFont(Font.font(algorithmTypePaneFont, algorithmTypePaneFontSize));

        choices = FXCollections.observableArrayList(manager.getPropertyValue(CLASSIFICATION.name()), manager.getPropertyValue(CLUSTERING.name()));
        algorithmTypes = new ChoiceBox(choices);
        algorithmTypes.setDisable(true);
        setAlgorithmTypes(algorithmTypes);
        algorithmTypePane.getChildren().addAll(algorithmTypePaneText, algorithmTypes);
        algorithmTypePane.setAlignment(Pos.CENTER);
        algorithmTypePane.setSpacing(5);

        selectionPane.setAlignment(Pos.CENTER);
        selectionPane.setSpacing(5);

        rightPane.setAlignment(Pos.CENTER);
        rightPane.getChildren().addAll(chart);

        leftPane.setVisible(false);
        runButton.setVisible(false);
        mainPane.getChildren().addAll(leftPane, rightPane);
        appPane.getChildren().add(mainPane);
    }

    public void displayLeftPane() {
        leftPane.setVisible(true);
        newButton.setDisable(true);
    }

    public void enableAlgorithmTypes(boolean value) {
        algorithmTypes.valueProperty().set(null);
        algorithmTypes.setDisable(!value);
    }

    public void loadDataInformation(int numInstances, int numLabels, ArrayList<String> labelNames, String dataFilePath) {
        PropertyManager manager = applicationTemplate.manager;
        String text = numInstances + manager.getPropertyValue(DATA_INFORMATION_ONE.name()) + numLabels
                                    + manager.getPropertyValue(DATA_INFORMATION_TWO.name()) + dataFilePath + "\n"
                                    + manager.getPropertyValue(DATA_INFORMATION_THREE.name());
        for (String labelName : labelNames) {
            text += "\n" + manager.getPropertyValue(DATA_INFORMATION_DASH.name()) + labelName;
        }
        setInformationText(text);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setScrnshotButtonActions();
        setDoneEditButtonActions();
        setAlgorithmTypesActions();
        setAlgorithmButtonActions();
        setSettingsButtonActions();
        setRunButtonActions();
        setCloseButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setScrnshotButtonActions() {
        scrnshotButton.setOnAction(event -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setDoneEditButtonActions() {
        doneEditButton.setOnAction(event -> {
            PropertyManager manager = applicationTemplate.manager;
            if (doneEditButton.getText().equals(manager.getPropertyValue(EDIT_BUTTON_TEXT.name()))) {
                doneEditButton.setText(manager.getPropertyValue(DONE_BUTTON_TEXT.name()));
                disableTextArea(false);
            } else if (doneEditButton.getText().equals(manager.getPropertyValue(DONE_BUTTON_TEXT.name()))) {
                String[] strings = textArea.getText().split("\n");
                ArrayList<String> data = new ArrayList<>();
                for (String string : strings) {
                    data.add(string);
                }
                if (!(textArea.getText().isEmpty()) && textArea.getText() != null) {
                    int x = ((AppData) applicationTemplate.getDataComponent()).parseData(data);
                    if (x == 0) {
                        enableAlgorithmTypes(true);
                        doneEditButton.setText(manager.getPropertyValue(EDIT_BUTTON_TEXT.name()));
                        disableTextArea(true);
                        ArrayList<String> labelNames = ((AppData) applicationTemplate.getDataComponent()).getLabelNames(data);
                        int counter = labelNames.size();
                        for (String labelName: labelNames) {
                            if (labelName.toLowerCase().equals(manager.getPropertyValue(NULL_LABEL.name()))) {
                                counter--;
                            }
                        }
                        ((AppData) applicationTemplate.getDataComponent()).setNumOfLabels(counter);
                        if (counter != 2) {
                            algorithmTypePane.getChildren().remove(algorithmTypes);
                            choices = FXCollections.observableArrayList(manager.getPropertyValue(CLUSTERING.name()));
                            setAlgorithmTypes(new ChoiceBox(choices));
                            algorithmTypePane.getChildren().add(algorithmTypes);
                        }
                        else {
                            algorithmTypePane.getChildren().remove(algorithmTypes);
                            choices = FXCollections.observableArrayList(manager.getPropertyValue(CLASSIFICATION.name()), manager.getPropertyValue(CLUSTERING.name()));
                            setAlgorithmTypes(new ChoiceBox(choices));
                            algorithmTypePane.getChildren().add(algorithmTypes);
                        }
                        setAlgorithmTypesActions();
                        selectionPane.getChildren().clear();
                        loadDataInformation(data.size(), labelNames.size(), labelNames, "");
                    } else {
                        ((AppActions) applicationTemplate.getActionComponent()).saveErrorHandlingHelper(x);
                        enableAlgorithmTypes(false);
                    }
                }
                else {
                    doneEditButton.setText(manager.getPropertyValue(EDIT_BUTTON_TEXT.name()));
                    disableTextArea(true);
                }
            }
        });
    }

    public void setAlgorithmTypesActions() {
        algorithmTypes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    PropertyManager manager = applicationTemplate.manager;
                    if (newValue.toString().equals(manager.getPropertyValue(CLASSIFICATION.name()))) {
                        classificationRadioGroup.selectToggle(null);
                        runButton.setVisible(false);
                        selectionPane.getChildren().clear();
                        VBox options = new VBox();
                        VBox settings = new VBox();

                        for (int i = 0; i < classificationAlgorithmNames.size(); i++) {
                            options.getChildren().add(classificationRadioButton[i]);
                            settings.getChildren().add(classificationConfigureButton[i]);
                        }

                        options.setPadding(new Insets(10, 0, 10, 0));
                        options.setSpacing(12);

                        options.setPadding(new Insets(5, 0, 10, 0));
                        settings.setSpacing(5);

                        selectionPane.getChildren().addAll(options, settings);

                    } else if (newValue.toString().equals(manager.getPropertyValue(CLUSTERING.name()))) {
                        clusteringRadioGroup.selectToggle(null);
                        runButton.setVisible(false);
                        selectionPane.getChildren().clear();
                        VBox options = new VBox();
                        VBox settings = new VBox();

                        for (int i = 0; i < clusteringAlgorithmNames.size(); i++) {
                            options.getChildren().add(clusteringRadioButton[i]);
                            settings.getChildren().add(clusteringConfigureButton[i]);
                        }

                        options.setPadding(new Insets(10, 0, 10, 0));
                        options.setSpacing(12);

                        options.setPadding(new Insets(5, 0, 10, 0));
                        settings.setSpacing(5);

                        selectionPane.getChildren().addAll(options, settings);
                    }
                }
            }
        });
    }

    private void setAlgorithmButtonActions() {
        classificationRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (classificationRadioGroup.getSelectedToggle() != null) {
                    runButton.setVisible(true);
                    runButton.setDisable(false);
                }
            }
        });
        clusteringRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (clusteringRadioGroup.getSelectedToggle() != null) {
                    runButton.setVisible(true);
                    runButton.setDisable(false);
                }
            }
        });
    }

    private void setSettingsButtonActions() {
        for (Button button: classificationConfigureButton) {
            button.setOnAction(event -> {
                RunConfiguration setting = (RunConfiguration) button.getUserData();
                setting.displayRunConfiguration();
                setting.hideNumberOfLabels();
            });
        }

        for (Button button: clusteringConfigureButton) {
            button.setOnAction(event -> {
                RunConfiguration setting = (RunConfiguration) button.getUserData();
                setting.displayRunConfiguration();
            });
        }
    }

    private void setRunButtonActions() {
        runButton.setOnAction(event -> {
            PropertyManager manager = applicationTemplate.manager;
            runButton.setText(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));
            if (algorithmTypes.getSelectionModel().getSelectedItem().toString().equals(manager.getPropertyValue(CLASSIFICATION.name()))) {
                boolean tocontinue = ((RunConfiguration) ((Button) classificationRadioGroup.getSelectedToggle().getUserData()).getUserData()).getContinuousRun();
                int maxIterations = ((RunConfiguration) ((Button) classificationRadioGroup.getSelectedToggle().getUserData()).getUserData()).getInterations();
                int updateInterval = ((RunConfiguration) ((Button) classificationRadioGroup.getSelectedToggle().getUserData()).getUserData()).getUpdateInterval();
                DataSet dataSet = new DataSet();
                if ((!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData())) {
                    try {
                        String[] lines = textArea.getText().split("\n");
                        for (String line : lines) {
                            dataSet.addInstance(line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        dataSet = DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < classifierAlgorithmClassNames.size(); i++) {
                    String[] values = classifierAlgorithmClassNames.get(i).split("\\.");
                    if (classificationRadioGroup.getSelectedToggle().toString().replaceAll("\\s+", "").toLowerCase().contains(values[1].toLowerCase())) {
                        try {
                            Class<?> klass = Class.forName(classifierAlgorithmClassNames.get(i));
                            Constructor konstructor = klass.getConstructors()[0];
                            if (runningThread == null) {
                                algorithm = (Runnable) konstructor.newInstance(dataSet, maxIterations, updateInterval, tocontinue);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        disableUI();

                        if (tocontinue) {
                            runningThread = new Thread(algorithm);
                            runningThread.setDaemon(true);
                            scrnshotButton.setDisable(true);
                            runningThread.start();
                            Task<Void> task = new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    while (runningThread.isAlive()) {
                                    }
                                    return null;
                                }
                            };
                            task.setOnSucceeded(e -> {
                                runButton.setDisable(false);
                            });
                            new Thread(task).start();
                        } else {
                            if (runningThread == null) {
                                runningThread = new Thread(algorithm);
                                runningThread.setDaemon(true);
                                runningThread.start();
                            } else {
                                synchronized (runningThread) {
                                    runningThread.notify();
                                }
                            }
                        }
                        break;
                    }
                }

            }

            else if (algorithmTypes.getSelectionModel().getSelectedItem().toString().equals(manager.getPropertyValue(CLUSTERING.name()))) {
                int maxIterations = ((RunConfiguration) ((Button) clusteringRadioGroup.getSelectedToggle().getUserData()).getUserData()).getInterations();
                int updateInterval = ((RunConfiguration) ((Button) clusteringRadioGroup.getSelectedToggle().getUserData()).getUserData()).getUpdateInterval();
                boolean tocontinue = ((RunConfiguration) ((Button) clusteringRadioGroup.getSelectedToggle().getUserData()).getUserData()).getContinuousRun();
                int numOfLabels = ((RunConfiguration) ((Button) clusteringRadioGroup.getSelectedToggle().getUserData()).getUserData()).getNumOfLabels();
                DataSet dataSet = new DataSet();
                if ((!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData())) {
                    try {
                        String[] lines = textArea.getText().split("\n");
                        for (String line : lines) {
                            dataSet.addInstance(line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        dataSet = DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < clustererAlgorithmClassNames.size(); i++) {
                    String[] values = clustererAlgorithmClassNames.get(i).split("\\.");
                    if (clusteringRadioGroup.getSelectedToggle().toString().replaceAll("\\s+", "").toLowerCase().contains(values[1].toLowerCase())) {
                        try {
                            Class<?> klass = Class.forName(clustererAlgorithmClassNames.get(i));
                            Constructor konstructor = klass.getConstructors()[0];
                            if (runningThread == null) {
                                algorithm = (Runnable) konstructor.newInstance(dataSet, maxIterations, updateInterval, numOfLabels, tocontinue);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        disableUI();
                        if (tocontinue) {
                            runningThread = new Thread(algorithm);
                            runningThread.setDaemon(true);
                            scrnshotButton.setDisable(true);
                            runningThread.start();
                            Task<Void> task = new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    while (runningThread.isAlive()) {
                                    }
                                    return null;
                                }
                            };
                            task.setOnSucceeded(e -> {
                                runButton.setDisable(false);
                            });
                            new Thread(task).start();
                        } else {
                            if (runningThread == null) {
                                runningThread = new Thread(algorithm);
                                runningThread.setDaemon(true);
                                runningThread.start();
                            } else {
                                synchronized (runningThread) {
                                    runningThread.notify();
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    public void disableTextArea(boolean value) {
        if (value) {
            textArea.setEditable(false);
            textArea.setOpacity(.75);
        }
        else {
            textArea.setEditable(true);
            textArea.setOpacity(1);
        }
    }

    public void updateChart() {
        try {
            chart.getData().clear();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.clear();
            if ((!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData())) {
                dataComponent.loadData(textArea.getText());
            }
            else {
                File file = ((AppActions) applicationTemplate.getActionComponent()).getDataFilePath().toFile();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String input;
                while ((input = br.readLine()) != null) {
                    ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().processString(input);
                }
            }
            dataComponent.displayData();
            ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().updateLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateChart(Map<String, String> dataLabels, Map<String, Point2D> dataPoints) {
        chart.getData().clear();
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        dataComponent.clear();
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number, Number> seriesData = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(seriesData);
            });
            chart.getData().add(series);
            Node line = series.getNode().lookup(".chart-series-line");
            line.setStyle("-fx-stroke: transparent;");
        }
    }

    private void setCloseButtonActions() {
        Stage mainStage = DataVisualizer.getPrimaryStage();
        mainStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
    }

    public void resetUI() {
        newButton.setDisable(false);
        runButton.setDisable(false);
        algorithmTypePane.setDisable(false);
        selectionPane.setDisable(false);
    }

    public void disableUI() {
        runButton.setDisable(true);
        doneEditButton.setDisable(true);
        algorithmTypePane.setDisable(true);
        selectionPane.setDisable(true);
    }

    public void algorithmFinished() {
        ((AppUI) applicationTemplate.getUIComponent()).setRunningThread(null);
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        if (!((AppActions) applicationTemplate.getActionComponent()).getIsLoadedData()) {
            ((AppUI) applicationTemplate.getUIComponent()).getDoneEditButton().setDisable(false);
        }
        ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getAlgorithmTypePane().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getSelectionPane().setDisable(false);
    }
}
