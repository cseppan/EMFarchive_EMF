package gov.epa.emissions.framework.install.installer;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateDownloadFilesList {

    private char delimiter;

    private String dir;

    private PrintWriter printer;

    private int counter = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaaa");

    public CreateDownloadFilesList(String dir, char delimiter) {
        this.dir = dir;
        this.delimiter = delimiter;
    }

    protected void createFilesList() throws Exception {
        if (!isDirectory(dir)) {
            throw new Exception("The '" + dir + "' is not a directory");
        }
        // File homeDirectory = new File(dir);
        // File[] files = homeDirectory.listFiles();
        File[] files = getFiles(dir);
        printer = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.home")
                + File.separatorChar + Generic.FILE_LIST)));
        printHeader();
        createFilesList(files);
        printer.close();
    }

    private File[] getFiles(String dir) throws ImporterException {
        String[] fileNames = new File(dir).list();
        String[] jarFiles = new FilePatternMatcher("*.jar").matchingNames(fileNames);
        File[] jars = new File[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++)
            jars[i] = new File(dir, jarFiles[i]);

        return jars;
    }

    private void createFilesList(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                print(files[i]);
            } else {
                createFilesList(files[i].listFiles());
            }
        }
    }

    private void print(File file) {
        counter++;
        printer.print(counter);
        printer.print(delimiter);

        String relativePath = getRelativePath(file);
        relativePath = relativePath.replace('\\', '/');
        printer.print(relativePath);
        printer.print(delimiter);

        printer.print("all");
        printer.print(delimiter);

        long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        printer.print(dateFormat.format(date));
        printer.print(delimiter);

        printer.print("1.0");
        printer.print(delimiter);

        if (file.length() == 0) {
            printer.print(0);
        } else if (file.length() % 1024 == 0) {
            printer.print((file.length() / 1024));// in KB
        } else {
            printer.print(((file.length() / 1024) + 1));// in KB
        }

        printer.println();
        printer.flush();
    }

    private String getRelativePath(File file) {
        // String absFilePath = file.getAbsolutePath();
        // String fileSeparator = File.separator;
        // String myHomeDir = "/lib/";
        // if(!myHomeDir.endsWith(fileSeparator)){
        // myHomeDir += fileSeparator;
        // }
        // return absFilePath.substring(myHomeDir.length()-1);
        return "/lib/" + file.getName();
    }

    private void printHeader() {
        printer.println("number" + delimiter + "path" + delimiter + "groups" + delimiter + "date" + delimiter
                + "version" + delimiter + "size");
        printer.flush();
    }

    private boolean isDirectory(String dirName) {
        File file = new File(dirName);
        return file.exists() && file.isDirectory();
    }

}
