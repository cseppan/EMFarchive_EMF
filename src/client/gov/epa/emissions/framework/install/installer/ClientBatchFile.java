package gov.epa.emissions.framework.install.installer;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientBatchFile {
    
    private PrintWriter writer;
    
    private final String sep = Generic.SEPARATOR;

    public ClientBatchFile(String fileName) throws IOException{
        writer = new PrintWriter(new BufferedWriter( new FileWriter(fileName)));
    }
    
    public void create(String preferenceFile, String emfhome, String javahome) throws ImporterException{
        writer.println("@echo off" + sep);
        writer.println("::  Batch file to start the EMF Client" + sep  + sep);
        writer.println("set EMF_HOME=" + emfhome + sep);
        writer.println("set R_HOME=C:\\Program Files\\R\\rw2000\\bin" + sep);
        writer.println("set JAVA_EXE=\"" + javahome.replace('/', '\\') + "\\bin\\java\"" + sep);
        writer.println("::  add bin directory to search path" + sep); 
        writer.println("set PATH=%PATH%;%R_HOME%" + sep);
        writer.println(":: set needed jar files in CLASSPATH" + sep);
        classPath();
        writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\emf-client.jar");
        writer.println(sep + sep + "@echo on" + sep + sep);
        writer.println("%JAVA_EXE% -Xmx400M -DEMF_PREFERENCE=" + "\""+preferenceFile+"\""+
                "  -classpath %CLASSPATH% gov.epa.emissions.framework.client.Launcher");
        writer.close();
         
    }

    private void classPath() throws ImporterException {
        String [] jarFiles = getJarFiles();
        writer.println();
        writer.println("set CLASSPATH=%EMF_HOME%\\lib\\"+jarFiles[0]);
        for (int i = 1; i < jarFiles.length; i++) {
            writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\lib\\"+jarFiles[i]);
        }
    }

    private String[] getJarFiles() throws ImporterException {
        String[] fileNames = new File("lib").list();
        return new FilePatternMatcher("*.jar").matchingNames(fileNames);
    }
    
    public static void main(String[] args) {
        try {
            String fileName = System.getProperty("user.home") + "/EMFClient.bat";
            String preferenceFile=System.getProperty("user.home") + "/EMFPrefs.txt";
            String emfhome = "C:\\";
            String javahome = System.getProperty("java.home");
            new ClientBatchFile(fileName).create(preferenceFile, emfhome, javahome);
            CreateDownloadFilesList filelist = new CreateDownloadFilesList("lib", ';');
            filelist.createFilesList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
