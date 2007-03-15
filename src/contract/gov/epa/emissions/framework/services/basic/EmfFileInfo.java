package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;

public class EmfFileInfo implements Serializable {
    
    private String absolutePath; 

    private String canonicalPath; 

    private String path; 
    
    private String name;
    
    private String parent; 
    
    private long freeSpace;
    
    private long totalSpace;
    
    private long usableSpace;

    private long lastModified;
    
    private long length;
    
    private int hashCode;
    
    private boolean isFile;

    private boolean isHidden;

    private boolean isDirectory;

    private boolean isAbsolute;

    private boolean exists;

    private boolean canExecute;
    
    private boolean canRead;

    private boolean canWrite;
    
    private String internalString;
    
    public EmfFileInfo() {
        //
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public void setCanonicalPath(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public void setAbsolute(boolean isAbsolute) {
        this.isAbsolute = isAbsolute;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public int hashCode() {
        return this.hashCode;
    }
    
    public String toString() {
        return this.internalString;
    }

    public boolean canExecute() {
        return canExecute;
    }

    public void setCanExecute(boolean canExecute) {
        this.canExecute = canExecute;
    }

    public boolean canRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getInternalString() {
        return internalString;
    }

    public void setInternalString(String internalString) {
        this.internalString = internalString;
    }

}
