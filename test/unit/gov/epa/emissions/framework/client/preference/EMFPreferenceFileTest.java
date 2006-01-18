package gov.epa.emissions.framework.client.preference;

import java.util.Properties;

import junit.framework.TestCase;

public class EMFPreferenceFileTest extends TestCase {

    public void testFetchLocalInputFolder() {
        Properties props = new Properties();
        props.put("local.input.drive", "d:\\");
        props.put("default.input.folder", "emf_input");

        UserPreferences test = new UserPreferences(props);

        assertEquals("d:\\emf_input", test.inputFolder());
    }

    public void testFetchLocalOutputFolder() {
        Properties props = new Properties();
        props.put("local.output.drive", "d:\\");
        props.put("default.output.folder", "emf_output");

        UserPreferences test = new UserPreferences(props);

        assertEquals("d:\\emf_output", test.outputFolder());
    }

    public void testMapLocalInputPathToRemote() {
        Properties props = new Properties();
        props.put("local.input.drive", "d:\\");
        props.put("default.input.folder", "emf_input");
        props.put("remote.input.drive", "/data/");

        UserPreferences test = new UserPreferences(props);

        assertEquals("/data/emf_input/orl", test.mapLocalInputPathToRemote("d:\\emf_input\\orl"));
        assertEquals("/data/emf_input/orl/nc/ch", test.mapLocalInputPathToRemote("d:\\emf_input\\orl\\nc\\ch"));
    }
    
    public void testMapLocalOutputPathToRemote() {
        Properties props = new Properties();
        props.put("local.output.drive", "d:\\");
        props.put("default.output.folder", "emf_output");
        props.put("remote.output.drive", "/data/");
        
        UserPreferences test = new UserPreferences(props);
        
        assertEquals("/data/emf_output/orl", test.mapLocalOutputPathToRemote("d:\\emf_output\\orl"));
        assertEquals("/data/emf_output/orl/nc/ch", test.mapLocalOutputPathToRemote("d:\\emf_output\\orl\\nc\\ch"));
    }
}
