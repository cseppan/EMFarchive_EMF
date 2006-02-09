package gov.epa.emissions.framework.client.preference;

import java.util.Properties;

import junit.framework.TestCase;

public class UserPreferencesTest extends TestCase {

    public void testFetchLocalInputFolder() {
        Properties props = new Properties();
        props.put("local.input.drive", "d:\\");
        props.put("default.input.folder", "emf_input");

        UserPreference pref = new DefaultUserPreferences(props);

        assertEquals("d:\\emf_input", pref.inputFolder());
    }

    public void testFetchLocalOutputFolder() {
        Properties props = new Properties();
        props.put("local.output.drive", "d:\\");
        props.put("default.output.folder", "emf_output");

        UserPreference pref = new DefaultUserPreferences(props);

        assertEquals("d:\\emf_output", pref.outputFolder());
    }

    public void testMapLocalInputPathToRemote() {
        Properties props = new Properties();
        props.put("local.input.drive", "d:\\");
        props.put("default.input.folder", "emf_input");
        props.put("remote.input.drive", "/data/");

        UserPreference pref = new DefaultUserPreferences(props);

        assertEquals("/data/emf_input/orl", pref.mapLocalInputPathToRemote("d:\\emf_input\\orl"));
        assertEquals("/data/emf_input/orl/nc/ch", pref.mapLocalInputPathToRemote("d:\\emf_input\\orl\\nc\\ch"));
    }
    
    public void testMapLocalOutputPathToRemote() {
        Properties props = new Properties();
        props.put("local.output.drive", "d:\\");
        props.put("default.output.folder", "emf_output");
        props.put("remote.output.drive", "/data/");
        
        UserPreference pref = new DefaultUserPreferences(props);
        
        assertEquals("/data/emf_output/orl", pref.mapLocalOutputPathToRemote("d:\\emf_output\\orl"));
        assertEquals("/data/emf_output/orl/nc/ch", pref.mapLocalOutputPathToRemote("d:\\emf_output\\orl\\nc\\ch"));
    }
}
