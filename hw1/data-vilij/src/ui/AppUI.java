package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.util.Stack;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
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

    public TextArea getTextArea() { return textArea; }

    public void saveSetDisable(boolean value) { saveButton.setDisable(value); }

    public void setScrnshotButtonDisable(boolean value) { scrnshotButton.setDisable(value); }

    private void layout() {
        // Create chart, text area, and display button
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        textArea = new TextArea();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_TEXT.name()));

        // Set textArea and chart size
        textArea.setPrefSize(250, 220);
        chart.setPrefSize(700, 550);

        // Chart styling
        chart.setHorizontalZeroLineVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        Node borderStyle = chart.lookup(".chart-plot-background");
        borderStyle.setStyle("-fx-border-color: black;");
        borderStyle.setStyle("-fx-border-style: solid; -fx-border-width: 3pt;");
        xAxis.setTickLabelFill(Color.BLUE);
        yAxis.setTickLabelFill(Color.BLUE);
        Node axisStyle = xAxis.lookup(".axis-tick-mark");
        axisStyle.setStyle("-fx-stroke: black;" +
                            "-fx-stroke-width: 3");
        axisStyle = yAxis.lookup(".axis-tick-mark");
        axisStyle.setStyle("-fx-stroke: black;" +
                            "-fx-stroke-width: 3;");

        // Check check box for read-only
        CheckBox checkBox = new CheckBox("Read Only");
        checkBox.setSelected(false);

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            textArea.setEditable(!newValue);
            if(newValue) {
                textArea.setOpacity(0.75);
            }
            else {
                textArea.setOpacity(1.0);
            }
        });

        // Create the panes
        VBox leftVBox = new VBox();
        VBox rightVBox = new VBox();
        HBox mainPane = new HBox();

        // Create the title text for left side
        Text leftVBoxTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_VBOX_TITLE.name()));
        String fontName = manager.getPropertyValue(AppPropertyTypes.LEFT_VBOX_TITLE_FONT.name());
        Double fontSize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_VBOX_TITLE_SIZE.name()));
        leftVBoxTitle.setFont(Font.font(fontName, fontSize));

        // Configure leftVBox
        leftVBox.setAlignment(Pos.TOP_CENTER);
        leftVBox.getChildren().addAll(leftVBoxTitle, textArea, displayButton, checkBox);
        leftVBox.setPadding(new Insets(20));
        leftVBox.setSpacing(10);

        // Configure rightVBox
        rightVBox.setAlignment(Pos.CENTER);
        rightVBox.getChildren().addAll(chart);

        // Add all panes to appPane
        mainPane.getChildren().addAll(leftVBox, rightVBox);
        appPane.getChildren().add(mainPane);
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
                String[] numOfLines = newValue.split("\n");
                String[] numOfLinesBefore = oldValue.split("\n");
                if (numOfLinesBefore.length == 10) {
                    if (numOfLinesBefore.length > numOfLines.length && !((AppData) applicationTemplate.getDataComponent()).getExtraLines().empty()) {
                        Stack<String> tempStack = new Stack<>();
                        textArea.setText("");

                        for (int i = 0; i < numOfLines.length; i++) {
                            textArea.appendText(numOfLines[i] + "\n");
                        }
                        while (((AppData) applicationTemplate.getDataComponent()).getExtraLines().size() != 0) {
                            tempStack.add(((AppData) applicationTemplate.getDataComponent()).getExtraLines().pop());
                        }
                        for (int i = 0; i < numOfLinesBefore.length - numOfLines.length; i++) {
                            if (tempStack.size() != 0) {
                                textArea.appendText(tempStack.pop() + "\n");
                            }
                        }
                        while (tempStack.size() != 0) {
                            ((AppData) applicationTemplate.getDataComponent()).getExtraLines().add(tempStack.pop());
                        }
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
                try {   // Clear current data, load new data, display data
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();

                    Stack clonedStack;
                    String extraLinesString = "";

                    if (dataComponent.getExtraLines().clone() instanceof Stack) {
                        clonedStack = (Stack) dataComponent.getExtraLines().clone();
                        while (clonedStack.size() != 0) {
                            extraLinesString += clonedStack.pop() + "\n";
                        }
                    }

                    dataComponent.loadData(textArea.getText() + extraLinesString);
                    dataComponent.displayData();
                    ((AppUI) applicationTemplate.getUIComponent()).setScrnshotButtonDisable(false);
                } catch (Exception e) {
                    e.printStackTrace();    // Prints location where exception happens in the source code
                }
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

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setScrnshotButtonActions();
    }
}
