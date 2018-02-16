package dataprocessors;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import java.util.Optional;

import static settings.AppPropertyTypes.INVALID_DATA;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
    }

    public void loadData(String dataString) {
        // TODO for homework 1
        try
        {
            processor.processString(dataString);
        }
        catch (Exception e) {
            PropertyManager manager = applicationTemplate.manager;
            manager.getPropertyValue(INVALID_DATA.name());
            Alert error = new Alert(Alert.AlertType.ERROR, manager.getPropertyValue(INVALID_DATA.name()), ButtonType.CLOSE);
            Optional<ButtonType> action = error.showAndWait();
            if (action.orElse(null) == ButtonType.CLOSE) {
                error.close();
            }
            return;
        }
        ((AppUI) applicationTemplate.getUIComponent()).clearButtons();
        displayData();
        processor.clear();
    }

    @Override
    public void saveData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
