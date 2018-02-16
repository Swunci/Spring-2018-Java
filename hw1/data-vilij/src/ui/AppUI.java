package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.util.List;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;
import static vilij.settings.PropertyTypes.EXIT_TOOLTIP;

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
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private static final String SEPARATOR = "/";
    private String scrnshoticonPath;

    public ScatterChart<Number, Number> getChart() { return chart; }

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
        // TODO for homework 1
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        printButton = setToolbarButton(printiconPath, manager.getPropertyValue(PRINT_TOOLTIP.name()), true);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);

        // Create the screenshot button and add to toolbar
        String scrnshotPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(SEPARATOR, scrnshotPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar = new ToolBar(newButton, saveButton, loadButton, printButton, scrnshotButton, exitButton);
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
    public void clearButtons()
    {
        newButton.setDisable(false);
        saveButton.setDisable(false);
        hasNewText = true;
    }

    public void setButtons()
    {
        newButton.setDisable(true);
        saveButton.setDisable(true);
        hasNewText = false;
    }

    @Override
    public void clear() {
        // TODO for homework 1
        if(hasNewText)
        {
            textArea.clear();
        }
        chart.getData().clear();
        newButton.setDisable(true);
        saveButton.setDisable(true);
        hasNewText = false;
    }

    private void layout() {
        // TODO for homework 1
        // Create chart, text area, and display button
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(CHART_TITLE.name()));
        textArea = new TextArea();
        displayButton = new Button(manager.getPropertyValue(DISPLAY_TEXT.name()));

        // Set textArea and chart size
        textArea.setPrefSize(250, 150);
        chart.setPrefSize(700, 550);

        // Create title for textArea and add listener
        Label textAreaTitle = new Label(manager.getPropertyValue(TEXT_AREA.name()));
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.equals(manager.getPropertyValue(EMPTY.name())))
                {
                    setButtons();
                }
                else
                {
                    clearButtons();
                }
            }
        });

        VBox leftVBox = new VBox();
        VBox rightVBox = new VBox();
        HBox mainPane = new HBox();

        leftVBox.setAlignment(Pos.TOP_CENTER);
        leftVBox.getChildren().addAll(textAreaTitle, textArea, displayButton);
        leftVBox.setPadding(new Insets(20));
        leftVBox.setSpacing(10);

        rightVBox.setAlignment(Pos.CENTER);
        rightVBox.getChildren().addAll(chart);
        mainPane.getChildren().addAll(leftVBox, rightVBox);

        appPane.getChildren().add(mainPane);
    }

    private void setWorkspaceActions() {
        // TODO for homework 1
        displayButton.setOnAction(action -> ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText()));
    }
}
