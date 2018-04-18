package ui;

import actions.AppActions;
import algorithms.RandomClassifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import java.util.ArrayList;

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
    private Button displayButton;                 // workspace button to display data on the chart
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
    private ToggleGroup radioGroup;
    private ArrayList<Button> classificationConfigs;
    private ArrayList<Button> clusteringConfigs;

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

    public void setInformationText(String information) {
        informationText.setText(information);
    }

    public void setChoices(ObservableList<String> choices) {
        this.choices = choices;
    }
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

        textArea = new TextArea();
        textArea.setPrefSize(240, 190);
        chart.setPrefSize(700, 550);
        informationText = new Text();
        displayButton = new Button(manager.getPropertyValue(DISPLAY_TEXT.name()));
        doneEditButton = new Button(manager.getPropertyValue(DONE_BUTTON_TEXT.name()));
        runButton = new Button(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));

        classificationConfigs = new ArrayList<>();
        clusteringConfigs = new ArrayList<>();

        Button classificationSB1 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        Button classificationSB2 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        Button classificationSB3 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        Button clusteringSB1 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        Button clusteringSB2 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        Button clusteringSB3 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));

        classificationSB1.setUserData(new RunConfiguration());
        classificationSB2.setUserData(new RunConfiguration());
        classificationSB3.setUserData(new RunConfiguration());
        clusteringSB1.setUserData(new RunConfiguration());
        clusteringSB2.setUserData(new RunConfiguration());
        clusteringSB3.setUserData(new RunConfiguration());

        classificationConfigs.add(classificationSB1);
        classificationConfigs.add(classificationSB2);
        classificationConfigs.add(classificationSB3);
        clusteringConfigs.add(clusteringSB1);
        clusteringConfigs.add(clusteringSB2);
        clusteringConfigs.add(clusteringSB3);

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

        radioGroup = new ToggleGroup();

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
        setDisplayButtonActions();
        setDoneEditButtonActions();
        setAlgorithmTypesActions();
        setAlgorithmButtonActions();
        setSettingsButtonActions();
        setRunButtonActions();
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

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    dataComponent.displayData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                        runButton.setVisible(false);
                        selectionPane.getChildren().clear();
                        VBox options = new VBox();
                        VBox settings = new VBox();

                        RadioButton rb1 = new RadioButton(manager.getPropertyValue(CLASSIFICATION_ALGORITHM_NAME_1.name()));
                        RadioButton rb2 = new RadioButton(manager.getPropertyValue(CLASSIFICATION_ALGORITHM_NAME_2.name()));
                        RadioButton rb3 = new RadioButton(manager.getPropertyValue(CLASSIFICATION_ALGORITHM_NAME_3.name()));
                        rb1.setUserData(classificationConfigs.get(0));
                        rb2.setUserData(classificationConfigs.get(1));
                        rb3.setUserData(classificationConfigs.get(2));
                        rb1.setToggleGroup(radioGroup);
                        rb2.setToggleGroup(radioGroup);
                        rb3.setToggleGroup(radioGroup);
                        options.getChildren().addAll(rb1, rb2, rb3);
                        options.setPadding(new Insets(10, 0, 10, 0));
                        options.setSpacing(12);

                        for (Button button : classificationConfigs) {
                            settings.getChildren().add(button);
                        }
                        options.setPadding(new Insets(5, 0, 10, 0));
                        settings.setSpacing(5);

                        selectionPane.getChildren().addAll(options, settings);

                    } else if (newValue.toString().equals(manager.getPropertyValue(CLUSTERING.name()))) {
                        runButton.setVisible(false);
                        selectionPane.getChildren().clear();
                        VBox options = new VBox();
                        VBox settings = new VBox();

                        RadioButton rb1 = new RadioButton(manager.getPropertyValue(CLUSTERING_ALGORITHM_NAME_1.name()));
                        RadioButton rb2 = new RadioButton(manager.getPropertyValue(CLUSTERING_ALGORITHM_NAME_2.name()));
                        RadioButton rb3 = new RadioButton(manager.getPropertyValue(CLUSTERING_ALGORITHM_NAME_3.name()));
                        rb1.setUserData(clusteringConfigs.get(0));
                        rb2.setUserData(clusteringConfigs.get(1));
                        rb3.setUserData(clusteringConfigs.get(2));
                        rb1.setToggleGroup(radioGroup);
                        rb2.setToggleGroup(radioGroup);
                        rb3.setToggleGroup(radioGroup);
                        options.getChildren().addAll(rb1, rb2, rb3);
                        options.setPadding(new Insets(10, 0, 10, 0));
                        options.setSpacing(12);

                        for (Button button : clusteringConfigs) {
                            settings.getChildren().add(button);
                        }
                        options.setPadding(new Insets(5, 0, 10, 0));
                        settings.setSpacing(5);

                        selectionPane.getChildren().addAll(options, settings);
                    }
                }
            }
        });
    }

    private void setAlgorithmButtonActions() {
        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (radioGroup.getSelectedToggle() != null) {
                    runButton.setVisible(true);
                }
            }
        });
    }

    private void setSettingsButtonActions() {
        for (Button button: classificationConfigs) {
            button.setOnAction(event -> {
                RunConfiguration setting = (RunConfiguration) button.getUserData();
                setting.displayRunConfiguration();
                setting.hideNumberOfLabels();
            });
        }

        for (Button button: clusteringConfigs) {
            button.setOnAction(event -> {
                RunConfiguration setting = (RunConfiguration) button.getUserData();
                setting.displayRunConfiguration();
            });
        }
    }

    private void setRunButtonActions() {
        runButton.setOnAction(event -> {
            // TODO: Running the algorithm
            PropertyManager manager = applicationTemplate.manager;
            if (algorithmTypes.getSelectionModel().getSelectedItem().toString().equals(manager.getPropertyValue(CLASSIFICATION.name()))) {
                try {
                    DataSet dataSet = new DataSet();
                    String[] lines = textArea.getText().split("\n");
                    for (String line : lines) {
                        dataSet.addInstance(line);
                    }
                    int maxIterations = ((RunConfiguration) ((Button) radioGroup.getSelectedToggle().getUserData()).getUserData()).getInterations();
                    int updateInterval = ((RunConfiguration) ((Button) radioGroup.getSelectedToggle().getUserData()).getUserData()).getUpdateInterval();
                    boolean tocontinue = ((RunConfiguration) ((Button) radioGroup.getSelectedToggle().getUserData()).getUserData()).getContinuousRun();
                    RandomClassifier rc = new RandomClassifier(dataSet, maxIterations, updateInterval, tocontinue);
                    rc.run();


                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    dataComponent.displayData();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (algorithmTypes.getSelectionModel().getSelectedItem().toString().equals(manager.getPropertyValue(CLUSTERING.name()))) {
                // TODO: Clustering run actions
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
}
