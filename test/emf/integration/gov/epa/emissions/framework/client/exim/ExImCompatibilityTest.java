package gov.epa.emissions.framework.client.exim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ExImCompatibilityTest extends TestCase {

    public void testExportShouldMatchImportForValidFiles() throws Exception {
        String execFilename = "good.bat";
        String expectedMessage = "Status: success";
        String outputFile = "good.results";

        run(execFilename, expectedMessage, outputFile);
    }

    public void testExportShouldFailToMatchImportForUnmatchedData() throws Exception {
        String execFilename = "bad.bat";
        String expectedMessage = "Status: failure";
        String outputFile = "bad.results";

        run(execFilename, expectedMessage, outputFile);
    }

    private void run(String execFilename, String expectedMessage, String outputFile) throws IOException,
            InterruptedException {
        String osName = System.getProperty("os.name" );
        assertTrue("Test only supported on Windows", osName.startsWith("Win"));
        
        File workingDir = new File("test/compatibility");

        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(new String[] { "cmd.exe", "/C", "start", execFilename }, null, workingDir);

        int exitValue = p.waitFor();
        assertEquals(exitValue, p.exitValue());
        assertEquals(0, p.exitValue());
        p.destroy();

        verify(new File(workingDir, outputFile), expectedMessage);
    }

    private void verify(File file, String expectedMessage) throws IOException {
        List lines = new ArrayList();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String line = null;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } finally {
            if (reader != null)
                reader.close();
        }

        assertTrue("No result generated in file - " + file, lines.size() >= 1);
        assertEquals(expectedMessage, lines.get(lines.size() - 1));
    }

}
