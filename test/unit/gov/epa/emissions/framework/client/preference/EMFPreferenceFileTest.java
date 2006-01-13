package gov.epa.emissions.framework.client.preference;

import gov.epa.emissions.framework.EmfException;
import junit.framework.TestCase;

public class EMFPreferenceFileTest extends TestCase {

    public void testShouldGetCorrectPrefParams() throws EmfException {
        System.setProperty("EMF_PREFERENCE", "test/data/preference/emfpreference.txt");
        UserPreferences up = new UserPreferences();
        assertTrue(up.getProperty("EMFInputDriveLetter").equals("T"));
        assertTrue(up.getProperty("EMFOutputDriveLetter").equals("T"));
        assertTrue(up.getProperty("EMFInputServerDirectory").equals("/data"));
        assertTrue(up.getProperty("EMFOutputServerDirectory").equals("/data"));
        assertTrue(up.getProperty("EMFDefaultInputDirectory").equals("emf_input"));
        assertTrue(up.getProperty("EMFDefaultOutputDirectory").equals("emf_output"));
        assertTrue(up.getInputDir().equals("T:\\emf_input"));
        assertTrue(up.getOutputDir().equals("T:\\emf_output"));
        assertTrue(up.getServerInputDir().equals("/data/emf_input"));
        assertTrue(up.getServerOutputDir().equals("/data/emf_output"));
    }
}
