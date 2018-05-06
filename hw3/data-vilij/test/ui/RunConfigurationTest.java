package ui;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RunConfigurationTest {

    // Testing that the run configuration value for maxIterations is always valid no matter what the user inputs
    // This is a boundary value test because we are testing for user input of any number less than 1 which would make the maxIteration value invalid
    @Test
    public void RunConfigurationIterationsTest1() {
        RunConfiguration runConfiguration = new RunConfiguration("test");
        // Testing to see if putting in 1 (a valid input) changes the value
        runConfiguration.setIterations(1);
        Assert.assertTrue(runConfiguration.getInterations() == 1);
        // User types 0 for maxIterations
        runConfiguration.setIterations(0);
        // Since 0 is not a valid value for maxIterations, the program should not change the value
        Assert.assertTrue (runConfiguration.getInterations() != 0);
    }

    // Testing that the run configuration value for updateInterval is always valid no matter what the user inputs
    // This is a boundary value test because we are testing for user input of any number less than 1 which would make the updateInterval value invalid
    @Test
    public void RunConfigurationUpdateIntervalTest1() {
        RunConfiguration runConfiguration = new RunConfiguration("test");
        // Testing to see if putting in 1 (a valid input) changes the value
        runConfiguration.setUpdateInterval(1);
        Assert.assertTrue(runConfiguration.getUpdateInterval() == 1);
        // User types 0 for updateInterval
        runConfiguration.setUpdateInterval(0);
        // Since 0 is not a valid value for updateInterval, the program should not change the value
        Assert.assertTrue (runConfiguration.getUpdateInterval() != 0);
    }

    // Testing that the run configuration value for number of labels is always valid no matter what the user inputs
    // This is a boundary value test because we are testing for user input of any number less than 2 which would make the number of labels value invalid
    @Test
    public void RunConfigurationNumberOfLabelsTest1() {
        RunConfiguration runConfiguration = new RunConfiguration("test");
        // Testing to see if putting in 2 (a valid input) changes the value
        runConfiguration.setlabels(2);
        Assert.assertTrue(runConfiguration.getNumOfLabels() == 2);
        // User types 1 into number of labels
        runConfiguration.setlabels(1);
        // Since 1 is not a valid value for number of labels, the program should not change the value
        Assert.assertTrue (runConfiguration.getNumOfLabels() != 1);
    }

    // Testing that the run configuration value for number of labels is always valid no matter what the user inputs
    // This is a boundary value test because we are testing for user input of any number greater than 4 which would make the number of labels value invalid
    @Test
    public void RunConfigurationNumberOfLabelsTest2() {
        RunConfiguration runConfiguration = new RunConfiguration("test");
        // Testing to see if putting in 4 (a valid input) changes the value
        runConfiguration.setlabels(4);
        Assert.assertTrue(runConfiguration.getNumOfLabels() == 4);
        // User types 5 into number of labels
        runConfiguration.setlabels(5);
        // Since 5 is not a valid value for number of labels, the program should not change the value
        Assert.assertTrue (runConfiguration.getNumOfLabels() != 5);
    }


}