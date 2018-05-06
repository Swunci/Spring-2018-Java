package dataprocessors;


import javafx.geometry.Point2D;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AppDataTest {

    // Parsing a single valid line of data in the TSD format to create an instance object
    @Test
    public void parseDataTest1() throws Exception {
        String tsdString = "@instance1\tlabel1\t1,2";
        ArrayList<String> lines = new ArrayList<>();
        lines.add(tsdString);
        AppData appData = new AppData();
        int returnNumber = appData.parseData(lines);
        // The number returned from parseData(ArrayList<String> data) should be 0 if the lines of data are in TSD format and contain only a name, label, and data points.
        // Any other number means that at least one line of data is invalid.
        Assert.assertEquals(0, returnNumber);
        //If the data is valid, processString() should create new instance object.
        TSDProcessor tsdProcessor = new TSDProcessor();
        for (String line : lines) {
            tsdProcessor.processString(line);
        }
        // If the instance object was created, we should be able to use the instance name as a key to get the data label and the data point
        String dataLabel = tsdProcessor.getDataLabels().get("@instance1");
        Point2D dataPoint = tsdProcessor.getDataPoints().get("@instance1");
        Assert.assertEquals("label1", dataLabel);
        Assert.assertEquals(new Point2D(1,2), dataPoint);
    }

    // Parsing a single line of invalid data
    // This is a boundary value test because each line should have only 3 values, this is testing the method with less than 3 values in a line
    @Test(expected = AssertionError.class)
    public void parseDataTest2() {
        String tsdString = "@instance1\tlabel1";
        ArrayList<String> lines = new ArrayList<>();
        lines.add(tsdString);
        AppData appData = new AppData();
        int returnNumber = appData.parseData(lines);
        // The number returned from parseData(ArrayList<String> data) should be 0 if the lines of data are in TSD format and contain only a name, label, and data points.
        // Any other number means that at least one line of data is invalid.
        Assert.assertEquals(0, returnNumber);
    }

    // Parsing a single line of invalid data
    // This is a boundary value test because each line should have only 3 values, this is testing the method with more than 3 values in a line
    @Test(expected = AssertionError.class)
    public void parseDataTest3() {
        String tsdString = "@instance1\tlabel1\t1,2\tuselessValue";
        ArrayList<String> lines = new ArrayList<>();
        lines.add(tsdString);
        AppData appData = new AppData();
        int returnNumber = appData.parseData(lines);
        // The number returned from parseData(ArrayList<String> data) should be 0 if the lines of data are in TSD format and contain only a name, label, and data points.
        // Any other number means that at least one line of data is invalid.
        Assert.assertEquals(0, returnNumber);
    }

    // Saving data from the text-area in the UI to a .tsd file
    @Test
    public void saveDataTest1() throws IOException {
        // Let text be the data from the text-area in the UI
        String text = "@instance1\tlabel1\t1,2\n@instance2\tlabel2\t2,3";
        AppData appData = new AppData();
        Path path = Paths.get("./testfile.tsd");
        // This method is supposed to save the data to a .tsd file named testfile
        appData.saveData(path, text);
        // Read the contents of the file and check if they are equal
        File file = new File(String.valueOf(path));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String fileContent = "";
        String input;
        int lineNum = 1;
        while ((input = br.readLine()) != null) {
            if (lineNum == 1) {
                fileContent += input;
            }
            else {
                fileContent += "\n" + input;
            }
            lineNum = 0;
        }
        // Contents should be identical
        Assert.assertEquals(text, fileContent);
    }

    // Saving data from the text-area in the UI to a nonexistent path
    @Test
    public void saveDataTest2() throws IOException {
        // Let text be the data from the text-area in the UI
        String text = "@instance1\tlabel1\t1,2\n@instance2\tlabel2\t2,3";
        AppData appData = new AppData();
        Path path = Paths.get("C:", "nonexistentpath", "testfile.tsd");
        // This method is supposed to save the data to a .tsd file named testfile
        appData.saveData(path, text);
        // If the data is saved to the file, the file should exist
        File file = new File(String.valueOf(path));
        // File should not exist
        Assert.assertFalse(file.exists());
    }


}