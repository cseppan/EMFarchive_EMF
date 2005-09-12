package gov.epa.emissions.framework.client.exim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ExImCompatibilityTest extends TestCase {

    public void testExportShouldMatchImportForValidFiles() throws Exception {
        String expectedMessage = "Status: success";
        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\exported-non-point.txt";

        run(expectedMessage, type, importFile, exportFile);
    }

    public void FIXME_testExportShouldFailToMatchImportForUnmatchedData() throws Exception {
        String expectedMessage = "Status: failure";

        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\BAD-exported-non-point.txt";

        run(expectedMessage, type, importFile, exportFile);
    }

    private void run(String expectedMessage, String type, String importFile, String exportFile) throws IOException,
            InterruptedException {
        String osName = System.getProperty("os.name");
        assertTrue("ExIm Compatibility Tests only run on Windows", osName.startsWith("Win"));

        File workingDir = new File("test/compatibility");
        String script = "compare_invs.pl";
        String[] args = new String[] { "perl.exe", script, type, importFile, exportFile };

        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args, null, workingDir);

        String status = captureStatus(p);

        p.waitFor();
        p.destroy();

        assertEquals(expectedMessage, status);
    }

    private String captureStatus(Process p) throws IOException {
        InputStreamReader isReader = new InputStreamReader(p.getInputStream());
        BufferedReader reader = new BufferedReader(isReader, 2048);

        List lines = new ArrayList();
        String line = null;
        do {
            line = reader.readLine();
            if (line != null)
                lines.add(line);
        } while (line != null);

        reader.close();

        assertTrue("Should have atleast the status message streamed by the compatiblity script", lines.size() >= 1);

        return (String) lines.get(lines.size() - 1);
    }

}
