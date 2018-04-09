package dataprocessors;

import actions.AppActions;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

import static settings.AppPropertyTypes.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;
    private Stack<String> extraLines = new Stack();

    public Stack<String> getExtraLines() {
        return extraLines;
    }

    public void setExtraLines(Stack<String> extraLines) {
        this.extraLines = extraLines;
    }

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        File file = dataFilePath.toFile();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            ArrayList<String> data = new ArrayList<>();

            String input;
            while ((input = br.readLine()) != null) {
                data.add(input);
            }
            if (data.size() > 10) {
                dataSizeErrorHandler(data);
            }
            int x = parseData(data);
            if (x == 0) {     // parseData returns the line of the first error or 0 if data is valid
                ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
                extraLines.clear();
                for (int y = 10; y < data.size(); y++) {
                    extraLines.add(data.get(y));
                }
                ((AppActions) applicationTemplate.getActionComponent()).setIsDataValid(true);
                applicationTemplate.getUIComponent().clear();
                TextArea textArea = ((AppUI) applicationTemplate.getUIComponent()).getTextArea();
                displayText(textArea, file);
                ((AppUI) applicationTemplate.getUIComponent()).loadDataInformation(data.size(), getLabelNames(data).size(), getLabelNames(data), file.getName());
            } else {
                invalidDataHandler(x);
            }

        } catch (IOException e) {
            // Creates a error dialog for loading
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager = applicationTemplate.manager;
            String errTile = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String errInput = manager.getPropertyValue(SPECIFIED_FILE.name());
            dialog.show(errTile, errMsg + errInput);
        }
    }

    public void loadData(String dataString) {
        try {
            processor.processString(dataString);
        } catch (Exception e) {
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager = applicationTemplate.manager;
            String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            dialog.show(errTitle, errMsg + errInput);
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }

    public int parseData(ArrayList<String> data) {         // Check if the data is valid
        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).equals("")) {
                if (data.get(i).charAt(0) != '@') {         // If the data does not begin with @, it's invalid data
                    return i + 1;                           // Returns the line number of the invalid data
                }
                String[] strings = data.get(i).split("\t");
                if (strings.length != 3) {                  // Valid data should be spaced with 2 tabs and have 3 variables
                    return i + 1;
                } else {                                      // Check if there are duplicate instance names
                    for (int j = 0; j < i; j++) {
                        String[] pastStrings = data.get(j).split("\t");
                        if (pastStrings[0].equals(strings[0])) {
                            return 0 - (i + 1);
                        }
                    }
                }
                String[] dataPoints = strings[2].split(",");
                if (dataPoints.length == 2) {               // Third variable should be two doubles separated by a comma
                    try {
                        Double.parseDouble(dataPoints[0]);  // Check if they are doubles
                        Double.parseDouble(dataPoints[1]);
                    } catch (Exception e) {
                        return i + 1;
                    }
                }
            }
            else {
                return i + 1;
            }
        }
        return 0;
    }

    private void displayText(TextArea textArea, File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String input;
            int i = 0;
            while ((input = br.readLine()) != null && i < 10) {     // Makes it only display ten lines
                textArea.appendText(input);
                textArea.appendText("\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getLabelNames(ArrayList<String> data) {
        ArrayList<String> returnList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String[] strings = data.get(i).split("\t");
            boolean isDuplicate = false;
            for (int j = 0; j < returnList.size(); j++) {
                if (strings[1].equals(returnList.get(j))) {
                    isDuplicate = true;
                }
            }
            if (!isDuplicate) {
                returnList.add(strings[1]);
            }
        }
        return returnList;
    }

    private void dataSizeErrorHandler(ArrayList<String> data) {
        PropertyManager manager = applicationTemplate.manager;
        String confirmationTitle = manager.getPropertyValue(CONFIRMATION.name());
        String confirmationMsgPart1 = manager.getPropertyValue(CONFIRMATION_MSG_PART1.name());
        String confirmationMsgPart2 = manager.getPropertyValue(CONFIRMATION_MSG_PART2.name());
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(confirmationTitle);
        dialog.setContentText(confirmationMsgPart1 + data.size() + confirmationMsgPart2);
        dialog.showAndWait();
    }

    private void invalidDataHandler(int errorNumber) {
        if (errorNumber > 0) {   // If x is a positive number, the error is invalid data
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager = applicationTemplate.manager;
            String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(ERROR_LINE.name());
            dialog.show(errTitle, errMsg + errorNumber);
        } else {  // Else x is a negative number, the error is duplicate names
            ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager = applicationTemplate.manager;
            String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String errMsg = manager.getPropertyValue(DUPLICATE_NAME.name());
            int errLine = errorNumber * -1;
            dialog.show(errTitle, errMsg + errLine);
        }
        ((AppActions) applicationTemplate.getActionComponent()).setIsDataValid(false);
    }
}