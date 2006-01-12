package gov.epa.emissions.framework.client.preference;

import junit.framework.TestCase;

public class EMFPreferenceFileTest extends TestCase {

    public void testShouldGetCorrectPrefParams() {
        System.setProperty("EMF_PREFERENCE", "test/data/preference/emfpreference.txt");
        UserPreferences up = UserPreferences.getInstance();
        assertTrue(up.getProperty("EMFInputDriveLetter").equals("T"));
        assertTrue(up.getProperty("EMFOutputDriveLetter").equals("T"));
        assertTrue(up.getProperty("EMFInputServerDirectory").equals("/data"));
        assertTrue(up.getProperty("EMFOutputServerDirectory").equals("/data"));
        assertTrue(up.getProperty("EMFDefaultInputDirectory").equals("emf_input"));
        assertTrue(up.getProperty("EMFDefaultOutputDirectory").equals("emf_output"));
    }
}
