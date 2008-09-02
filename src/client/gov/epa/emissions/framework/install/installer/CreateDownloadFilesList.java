package gov.epa.emissions.framework.install.installer;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CreateDownloadFilesList {

    private char delimiter;

    private String dir;

    private PrintWriter printer;

    private int counter = 0;
    
    private File clientJarFile;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaaa");

    public CreateDownloadFilesList(String dir, char delimiter) {
        this.dir = dir;
        this.delimiter = delimiter;
        this.clientJarFile = new File(Constants.CLIENT_JAR_FILE);
    }

    protected void createFilesList() throws Exception {
        if (!isDirectory(dir)) {
            throw new Exception("The '" + dir + "' is not a directory");
        }

        List<File> jarRefFiles = new ArrayList<File>();
        File[] jarFiles = getFiles(dir);
        File[] refFiles = getRefFiles(Constants.REFERENCE_PATH);
        File[] prefFiles = getRefFiles(Constants.PREFERENCE_PATH);
        jarRefFiles.addAll(Arrays.asList(jarFiles));
        jarRefFiles.addAll(Arrays.asList(refFiles));
        jarRefFiles.addAll(Arrays.asList(prefFiles));
        
        printer = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.dir") + File.separatorChar
                + "deploy" + File.separatorChar + "client" + File.separatorChar + Constants.FILE_LIST)));
        printHeader();
        createFilesList(jarRefFiles.toArray(new File[0]));
        print(clientJarFile);
        printer.close();
    }

    private File[] getFiles(String dir) {
        File path = new File(dir);
        String[] fileNames = path.list();
        try {
            String[] jarFiles = new FilePatternMatcher(path, "*.jar").matchingNames(fileNames);
            File[] jars = new File[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++)
                jars[i] = new File(dir, jarFiles[i]);

            return jars;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private File[] getRefFiles(String dir) {
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File path = new File(dir);
        
        return path.listFiles(fileFilter);
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
        String absFilePath = file.getAbsolutePath();
        String relativePath = "/lib/" + file.getName();
        String parentPath = file.getParent();
        System.out.println("Parent path: " + parentPath);
        System.out.println(Constants.REFERENCE_PATH);
        
        if (absFilePath.indexOf("epa-commons") >= 0)
            relativePath = "/lib/epa-commons.jar";

        if (absFilePath.indexOf("analysis-engine") >= 0)
            relativePath = "/lib/analysis-engine.jar";
        
        if(parentPath.equalsIgnoreCase(Constants.REFERENCE_PATH))
            relativePath = "/config/ref/delimited/" + file.getName();
        
        if(parentPath.equalsIgnoreCase(Constants.PREFERENCE_PATH))
            relativePath = "/config/preferences/" + file.getName();
        
        if(absFilePath.equalsIgnoreCase(Constants.CLIENT_JAR_FILE))
            relativePath = File.separatorChar + file.getName();
        
        return relativePath;
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
    
    public static void main(String[] args) {
        try {
            CreateDownloadFilesList filelist = new CreateDownloadFilesList(System.getProperty("user.dir") + "\\lib", ';');
            filelist.createFilesList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
