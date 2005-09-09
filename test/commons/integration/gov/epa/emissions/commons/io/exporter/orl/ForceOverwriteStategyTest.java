package gov.epa.emissions.commons.io.exporter.orl;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ForceOverwriteStategyTest extends TestCase {

    public void testShouldIgnoreRequestToVerifyWritableEvenIfFileExists() throws IOException {
        File file = File.createTempFile("force-overwrite-test", "txt");
        assertTrue(file.exists());

        new ForceOverwriteStrategy().verifyWritable(file);
    }

    public void testShouldIgnoreRequestToVerifyWritableIfFileDoesNotExist() throws IOException {
        File file = new File("force-overwrite-test.txt");
        assertFalse(file.exists());

        new ForceOverwriteStrategy().verifyWritable(file);
    }

}
