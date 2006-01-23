package gov.epa.emissions.framework.install.installer;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ClientBatchFile {
    
    private PrintWriter writer;
    
    private final String sep = Generic.SEPARATOR;
    
    private File batchFile;

    public ClientBatchFile(String fileName) throws Exception{
        this.batchFile = new File(fileName);
        writer = new PrintWriter(new BufferedWriter( new FileWriter(fileName)));
    }
    
    public void create(String preference, String javahome, String server) throws Exception{
        writer.println("@echo off" + sep);
        writer.println("::  Batch file to start the EMF Client" + sep  + sep);
        writer.println("set EMF_HOME=\"" + batchFile.getParent() + "\""+ sep);
        writer.println("set R_HOME=C:\\Program Files\\R\\rw2000\\bin" + sep);
        writer.println("set JAVA_EXE=\"" + javahome + "\\bin\\java\"" + sep);
        writer.println("::  add bin directory to search path" + sep); 
        writer.println("set PATH=%PATH%;%R_HOME%" + sep);
        writer.println(":: set needed jar files in CLASSPATH" + sep);
        classPath();
        writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\emf-client.jar");
        writer.println(sep + sep + "@echo on" + sep + sep);
        writer.println("%JAVA_EXE% -Xmx400M -DEMF_PREFERENCE=" + 
                "\"" + batchFile.getParent() + "\\" + preference + "\" " +
                "-classpath %CLASSPATH% gov.epa.emissions.framework.client.Launcher " +
                server + sep);
        writer.close();
         
    }

    private void classPath() throws Exception {
        String [] jarFiles = getJarFiles();
        writer.println();
        writer.println("set CLASSPATH=%EMF_HOME%\\lib\\"+jarFiles[0]);
        for (int i = 1; i < jarFiles.length; i++) {
            writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\lib\\"+jarFiles[i]);
        }
    }

    private String[] getJarFiles() throws Exception {
        String[] fileNames = new File(batchFile.getParent() + "\\lib").list();
        return new FilePatternMatcher("*.jar").matchingNames(fileNames);
    }
    
//    public static void main(String[] args) {
//        try {
//            String fileName = System.getProperty("user.home") + "/EMFClient.bat";
//            String preference="EMFPrefs.txt";
//            String javahome = System.getProperty("java.home");
//            new ClientBatchFile(fileName).create(preference, javahome);
//            CreateDownloadFilesList filelist = new CreateDownloadFilesList("lib", ';');
//            filelist.createFilesList();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
