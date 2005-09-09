package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.framework.EmfException;

import java.io.File;

import junit.framework.TestCase;

public class NoOverwriteStategyTest extends TestCase {

    public void testShouldFailToVerifyWritableIfFileExists() throws Exception {
        File file = File.createTempFile("force-overwrite-test", "txt");
        assertTrue(file.exists());

        try {
            new NoOverwriteStrategy().verifyWritable(file);
        } catch (EmfException e) {
            assertEquals("Cannot export as file - " + file.getAbsolutePath() + " exists", e.getMessage());
            return;
        }

        fail("should have raised an exception if file existed");
    }

    public void testShouldIgnoreRequestToVerifyWritableIfFileDoesNotExist() throws Exception {
        File file = new File("force-overwrite-test.txt");
        assertFalse(file.exists());

        new NoOverwriteStrategy().verifyWritable(file);
    }

}
