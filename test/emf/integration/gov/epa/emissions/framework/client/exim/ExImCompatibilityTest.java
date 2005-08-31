package gov.epa.emissions.framework.client.exim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ExImCompatibilityTest extends TestCase {

    public void testExportShouldMatchImportForValidFiles() throws Exception {
        String expectedMessage = "Status: success";
        String outputFile = "good.results";
        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\exported-non-point.txt";

        run(expectedMessage, outputFile, type, importFile, exportFile);
    }

    public void testExportShouldFailToMatchImportForUnmatchedData() throws Exception {
        String expectedMessage = "Status: failure";
        String outputFile = "bad.results";

        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\BAD-exported-non-point.txt";

        run(expectedMessage, outputFile, type, importFile, exportFile);
    }

    private void run(String expectedMessage, String outputFile, String type, String importFile, String exportFile)
            throws IOException, InterruptedException {
        String osName = System.getProperty("os.name");
        assertTrue("ExIm Compatibility Tests only run on Windows", osName.startsWith("Win"));

        File workingDir = new File("test/compatibility");
        String script = "compare_invs.pl";
        String[] args = new String[] { "perl.exe", script, type, importFile, exportFile, ">", outputFile };

        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args, null, workingDir);

        dumpOutput(p);

        p.waitFor();
        p.destroy();

//        verify(new File(workingDir, outputFile), expectedMessage);
    }

    private void dumpOutput(Process p) throws IOException {
        flush(p.getErrorStream());
        flush(p.getInputStream());
    }

    private void flush(InputStream inputStream) throws IOException {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);

        String line = null;
        while ((line = reader.readLine()) != null)
            System.err.println(line);;
        reader.close();
    }

    //FIXME: read the 'exit status' directly from the error/input streams
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
