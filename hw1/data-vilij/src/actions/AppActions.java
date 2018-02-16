package actions;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import vilij.components.ActionComponent;
import vilij.components.UIComponent;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static settings.AppPropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;

    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;
    private FileChooser fileChooser = new FileChooser();

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        // TODO for homework 1
        try {
            if (promptToSave())
                applicationTemplate.getUIComponent().clear();
        } catch (IOException e) {
        }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        // TODO for homework 1
        try {
            if (promptToExit()) {
                System.exit(0);
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        // TODO for homework 1
        // TODO remove the placeholder line below after you have implemented this method
        PropertyManager manager = applicationTemplate.manager;

        // Create confirmation window for saving
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
        alert.setHeaderText(null);
        alert.setContentText(manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        Optional<ButtonType> action = alert.showAndWait();
        if (action.orElse(null) == ButtonType.YES) {
            FileChooser.ExtensionFilter exitFilter = new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name()));
            fileChooser.getExtensionFilters().add(exitFilter);
            fileChooser.setInitialFileName(manager.getPropertyValue(INITIAL_SAVE.name()));
            fileChooser.setInitialDirectory(new File(manager.getPropertyValue(DATA_RESOURCE_PATH.name())));
            try {
                dataFilePath = (fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow())).toPath();
            } catch (NullPointerException e) {
                return false;
            }

            return true;
        }
        else if (action.orElse(null) == ButtonType.NO) { return true; }
        else if (action.orElse(null) == ButtonType.CANCEL) { return false; }

        return false;
    }

    private boolean promptToExit() throws IOException {
        PropertyManager manager = applicationTemplate.manager;

        // Create confirmation window for exiting
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
        alert.setHeaderText(null);
        alert.setContentText(manager.getPropertyValue(EXIT_WHILE_RUNNING_WARNING.name()));
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> action = alert.showAndWait();
        if (action.orElse(null) == ButtonType.YES) {
            return true;
        } else if (action.orElse(null) == ButtonType.NO) {
            return false;
        }
        return false;
    }
}

