package gov.epa.emissions.framework.install;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientBatchFile {
    
    private PrintWriter writer;

    public ClientBatchFile(String fileName) throws IOException{
        writer = new PrintWriter(new BufferedWriter( new FileWriter(fileName)));
    }
    
    public void create(String preferenceFile) throws ImporterException{
        writer.println("@echo off");
        writer.println("\n::  Batch file to start the EMF Client");
        writer.println("\n\nset EMF_HOME=");
        writer.println("\nset R_HOME=C:\\Program Files\\R\\rw2000\\bin");
        writer.println("\nset JAVA_EXE=\"C:\\j2sdk1.4.2_01\\bin\\java\"");
        writer.println("\n::  add bin directory to search path"); 
        writer.println("\nset PATH=%PATH%;%R_HOME%");
        writer.println("\nset needed jar files in CLASSPATH");
        classPath();
        writer.println("\n\n@echo on\n\n");
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
            String preferenceFile="";
            new ClientBatchFile(fileName).create(preferenceFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
