package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.*;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.SEPARATOR;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active */
    Path dataFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void clearDataFilePath() { dataFilePath = null; }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                if (!isUnsaved.get()) {
                    applicationTemplate.getDataComponent().clear();
                    applicationTemplate.getUIComponent().clear();
                    isUnsaved.set(false);
                    dataFilePath = null;
                    ((AppUI) applicationTemplate.getUIComponent()).setScrnshotButtonDisable(true);
                }
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        if (isUnsaved.get() && dataFilePath != null) { //
            save();
            ((AppUI) applicationTemplate.getUIComponent()).saveSetDisable(true);
        }
        else {
            try {
                saveNewFile();
                ((AppUI) applicationTemplate.getUIComponent()).saveSetDisable(true);
            } catch (IOException e) {
                errorHandlingHelper();
            }
        }
    }

    @Override
    public void handleLoadRequest() {
        load();
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) /* If isUnsaved is true, condition depends on promptToSave() */
                if (!isUnsaved.get())
                    System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
        saveAsPng();

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
        // Creates a confirmation dialog
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()),
                manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false;   // If user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {     // If the users selects YES
            ArrayList<String> data = new ArrayList<>();
            String[] textArea =((AppUI) applicationTemplate.getUIComponent()).getCurrentText().split("\n");
            for (String line : textArea) {
                data.add(line);
            }
            int x = ((AppData) applicationTemplate.getDataComponent()).parseData(data);

            if (x == 0) {
                if (dataFilePath == null) {     // If there no dataFilePath it means that this file is not stored anywhere so we have to find a place to store it
                    FileChooser fileChooser = new FileChooser();
                    String dataDirPath = SEPARATOR + manager.getPropertyValue(DATA_RESOURCE_PATH.name());     // Gets name of the folder (/data)
                    URL dataDirURL = getClass().getResource(dataDirPath);      // Gets the absolute path to /data folder

                    if (dataDirURL == null)     // If there is no absolute path
                        throw new FileNotFoundException(manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));

                    fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                    fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                    // Creates a filter to determine what types of files are allowed
                    String description = manager.getPropertyValue(DATA_FILE_EXT_DESC.name());
                    String extension = manager.getPropertyValue(DATA_FILE_EXT.name());
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(String.format("%s", description), String.format("*.%s", extension));
                    // Applies the filter so the file can only be saved as the types specified above
                    fileChooser.getExtensionFilters().add(extFilter);
                    File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    if (selected != null) {
                        dataFilePath = selected.toPath();
                        save();
                    } else {
                        saveErrorHandlingHelper(x);
                        return false;    // If user presses escape after initially selecting 'yes'
                    }
                }
            } else
                save();
        }
        else if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
            setIsUnsavedProperty(false);
            Stack<String> empty = new Stack<>();
            ((AppData) applicationTemplate.getDataComponent()).setExtraLines(empty);
            return true;
        }
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);    // If CANCEL is selected, return false
    }

    private void save() {
        ArrayList<String> data = new ArrayList<>();
        String[] textArea = ((AppUI) applicationTemplate.getUIComponent()).getCurrentText().split("\n");
        for (String line : textArea) {
            data.add(line);
        }
        int x = ((AppData) applicationTemplate.getDataComponent()).parseData(data);
        if (x == 0) {
            applicationTemplate.getDataComponent().saveData(dataFilePath);
        }
        else {
            saveErrorHandlingHelper(x);
        }
    }

    public SimpleBooleanProperty getIsUnsaved() {
        return isUnsaved;
    }

    private void saveNewFile() throws IOException {
        PropertyManager manager = PropertyManager.getManager();
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = SEPARATOR + manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);
        //fileChooser.setInitialDirectory(new File(dataDirURL.getPath()));
       // fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

        if (dataDirURL == null)
            throw new FileNotFoundException(manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));

        ArrayList<String> data = new ArrayList<>();
        String[] textArea =((AppUI) applicationTemplate.getUIComponent()).getCurrentText().split("\n");
        for (String line : textArea) {
            data.add(line);
        }
        int x = ((AppData) applicationTemplate.getDataComponent()).parseData(data);

        if (x == 0) {
            fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
            fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
            String description = manager.getPropertyValue(DATA_FILE_EXT_DESC.name());
            String extension = manager.getPropertyValue(DATA_FILE_EXT.name());
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(String.format("%s", description), String.format("*.%s", extension));
            fileChooser.getExtensionFilters().add(extFilter);
            File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (selected != null) {
                dataFilePath = selected.toPath();
                applicationTemplate.getDataComponent().saveData(dataFilePath);
            }
        }
        else {
            saveErrorHandlingHelper(x);
        }
    }

    private void load() {
        Stack<String> empty = new Stack<>();
        ((AppData) applicationTemplate.getDataComponent()).setExtraLines(empty);
        PropertyManager manager = PropertyManager.getManager();
        FileChooser fileChooser = new FileChooser();

        String dataDirPath = SEPARATOR + manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);
        fileChooser.setInitialDirectory(new File(dataDirURL.getPath()));

        String description = manager.getPropertyValue(DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(DATA_FILE_EXT.name());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(String.format("%s", description), String.format("*.%s", extension));
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selected != null) {
            dataFilePath = selected.toPath();
            applicationTemplate.getDataComponent().loadData(dataFilePath);
        }
    }

    public void saveAsPng() throws IOException {
        WritableImage image = ((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);

        PropertyManager manager = PropertyManager.getManager();
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = SEPARATOR + manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);

        if (dataDirURL == null)
            throw new FileNotFoundException(manager.getPropertyValue(RESOURCE_SUBDIR_NOT_FOUND.name()));

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(manager.getPropertyValue(SAVE_IMAGE_TITLE.name()));
        String description = manager.getPropertyValue(SAVE_FILE_EXT_DESC.name());
        String extension = manager.getPropertyValue(SAVE_IMAGE_FILE_EXT.name());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(String.format("%s", description), String.format("*.%s", extension));
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selected != null) {
            dataFilePath = selected.toPath();
            File file = dataFilePath.toFile();
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void errorHandlingHelper() {
        // Creates a error dialog for saving
        ErrorDialog     dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle  = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager. getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }

    public void saveErrorHandlingHelper(int x) {
        PropertyManager manager = applicationTemplate.manager;
        if (x > 0) {   // If x is a positive number, the error is invalid data
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(AppPropertyTypes.ERROR_LINE.name());
            int errLine = x;
            dialog.show(errTitle, errMsg + errLine);
            ((AppActions) applicationTemplate.getActionComponent()).clearDataFilePath();
            ((AppActions) applicationTemplate.getActionComponent()).getIsUnsaved().set(true);
        }
        if (x < 0) {   // Else x is a negative number, the error is duplicate names
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(AppPropertyTypes.DUPLICATE_NAME.name());
            int errLine = x * -1;
            dialog.show(errTitle, errMsg + errLine);
            ((AppActions) applicationTemplate.getActionComponent()).clearDataFilePath();
            ((AppActions) applicationTemplate.getActionComponent()).getIsUnsaved().set(true);
        }
    }
}

