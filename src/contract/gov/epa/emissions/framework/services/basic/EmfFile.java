package gov.epa.emissions.framework.services.basic;

import java.io.File;

public class EmfFile extends File {
    @SuppressWarnings("hiding")
    public static String pathSeparator = File.pathSeparator;
   
    @SuppressWarnings("hiding")
    public static char pathSeparatorChar = File.pathSeparatorChar;

    @SuppressWarnings("hiding")
    public static String separator = File.separator;

    @SuppressWarnings("hiding")
    public static char separatorChar = File.separatorChar; 

    private EmfFileInfo fileInfo;

    public EmfFile(EmfFileInfo fileInfo) {
        super(fileInfo.getAbsolutePath());
        this.fileInfo = fileInfo;
        this.pathSeparator = fileInfo.getPathSeparator();
        this.pathSeparatorChar = (char)fileInfo.getPathSeparatorChar();
        this.separator = fileInfo.getSeparator();
        this.separatorChar = (char)fileInfo.getSeparatorChar();
    }
    
    public String getName() {
        return fileInfo.getName();
    }
    
    public String getParent() {
        return fileInfo.getParent();
    }
    
    public String getPath() {
        return fileInfo.getPath();
    }
    
    public String getAbsolutePath() {
        return fileInfo.getAbsolutePath();
    }
    
    public File getAbsoluteFile() {
        return new File(fileInfo.getAbsolutePath());
    }
    
    public String getCanonicalPath() {
        return fileInfo.getCanonicalPath();
    }
    
    public File getCanonicalFile() {
        return new File(fileInfo.getCanonicalPath());
    }
    
    public long getFreeSpace() {
        return fileInfo.getFreeSpace();
    }
    
    public long getTotalSpace() {
        return fileInfo.getTotalSpace();
    }
    
    public long lastModified() {
        return fileInfo.getLastModified();
    }
    
    public long length() {
        return fileInfo.getLength();
    }
    
    public long getUsableSpace() {
        return fileInfo.getUsableSpace();
    }
    
    public boolean isDirectory() {
        return fileInfo.isDirectory();
    }
    
    public boolean  isFile() {
        return fileInfo.isFile();
    }
    public boolean exists() {
        return fileInfo.isExists();
    }
    
    public boolean isAbsolute() {
        return fileInfo.isAbsolute();
    }
    
    public boolean isHidden() {
        return fileInfo.isHidden();
    }
    
    public boolean canExecute() {
        return fileInfo.canExecute();
    }
    
    public boolean canRead() {
        return fileInfo.canRead();
    }
    
    public boolean canWrite() {
        return fileInfo.canWrite();
    }
    
    public int hashCode() {
        return fileInfo.getHashCode();
    }
    
    public String toString() {
        return fileInfo.toString();
    }

}
