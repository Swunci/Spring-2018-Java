package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
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

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton;                // toolbar button to take a screenshot of the data
    private Button                       displayButton;                 // workspace button to display data on the chart
    private Button                       doneEditButton;
    private Button                       runButton;
    private ChoiceBox                    algorithmTypes;
    private Text                         informationText;
    private TextArea                     textArea;                      // text area for new data input
    private boolean                      hasNewText;                    // whether or not the text area has any new data since last display
    private LineChart<Number, Number>    chart;                         // the chart where data will be displayed
    private VBox                         leftPane;
    private ObservableList<String>       choices;

    public ChoiceBox getAlgorithmTypes() { return algorithmTypes; }
    public Text getInformationText() { return informationText; }
    public TextArea getTextArea() { return textArea; }
    public LineChart<Number, Number> getChart() { return chart; }

    public void disableNewButton(boolean value) { newButton.setDisable(value);}
    public void disableSaveButton(boolean value) { saveButton.setDisable(value); }
    public void disableScrnshotButton(boolean value) { scrnshotButton.setDisable(value); }
    public void disableDoneEditButton(boolean value) { doneEditButton.setDisable(value); }
    public void setAlgorithmTypes(ChoiceBox choices) { algorithmTypes = choices; }
    public void setInformationText(String information) { informationText.setText(information); }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                                                   manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                                   manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
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

    public String getCurrentText() { return textArea.getText(); }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(CHART_TITLE.name()));

        textArea = new TextArea();
        textArea.setPrefSize(240, 190);
        chart.setPrefSize(700, 550);
        informationText = new Text();
        displayButton = new Button(manager.getPropertyValue(DISPLAY_TEXT.name()));
        doneEditButton = new Button(manager.getPropertyValue(DONE_BUTTON_TEXT.name()));
        runButton = new Button(manager.getPropertyValue(RUN_BUTTON_TEXT.name()));
        leftPane = new VBox();

        VBox informationPane = new VBox();
        VBox algorithmTypePane = new VBox();
        VBox selectionPane = new VBox();
        VBox rightPane = new VBox();
        HBox mainPane = new HBox();

        Text leftPaneTitle = new Text(manager.getPropertyValue(LEFT_PANE_TITLE.name()));
        String fontName = manager.getPropertyValue(LEFT_PANE_TITLE_FONT.name());
        Double fontSize = Double.parseDouble(manager.getPropertyValue(LEFT_PANE_TITLE_SIZE.name()));
        leftPaneTitle.setFont(Font.font(fontName, fontSize));

        leftPane.setAlignment(Pos.TOP_CENTER);
        leftPane.getChildren().addAll(leftPaneTitle, textArea, doneEditButton, informationPane, algorithmTypePane, selectionPane);
        leftPane.setPadding(new Insets(20));
        leftPane.setSpacing(5);

        informationPane.getChildren().add(informationText);
        informationText.wrappingWidthProperty().bind(textArea.widthProperty());

        Text algorithmTypePaneText = new Text(manager.getPropertyValue(ALGORITHM_TYPE_PANE_TITLE.name()));
        String algorithmTypePaneFont = manager.getPropertyValue(ALGORITHM_TYPE_PANE_FONT.name());
        Double algorithmTypePaneFontSize = Double.parseDouble(manager.getPropertyValue(ALGORITHM_TYPE_PANE_FONT_SIZE.name()));
        algorithmTypePaneText.setFont(Font.font(algorithmTypePaneFont, algorithmTypePaneFontSize));

        choices = FXCollections.observableArrayList("Classification", "Clustering");
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
        String text = numInstances + " instances with " + numLabels + " labels loaded from " + dataFilePath + "\n The labels are:";
        for (String labelName: labelNames) {
            text += "\n- " + labelName;
        }
        setInformationText(text);
    }

    private void setAlgorithmTypesActions() {
        getAlgorithmTypes().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue ov, Number value, Number new_value) {
                if (new_value.intValue() == 0) {
                    // TODO: Classification Algorithms UI
                }
                else {
                    // TODO: Clustering Algorithms UI
                }
            }
        });
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setDoneEditButtonActions();
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
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
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
            }
            else if (doneEditButton.getText().equals(manager.getPropertyValue(DONE_BUTTON_TEXT.name()))) {
                String[] strings = textArea.getText().split("\n");
                ArrayList<String> data = new ArrayList<>();
                for (String string: strings) {
                    data.add(string);
                }
                // TODO: Check if textArea data is valid
                doneEditButton.setText(manager.getPropertyValue(EDIT_BUTTON_TEXT.name()));
                disableTextArea(true);
            }
        });
    }

    private void setRunButtonActions() {
        runButton.setOnAction(event -> {
            // TODO: Running the algorithm
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
