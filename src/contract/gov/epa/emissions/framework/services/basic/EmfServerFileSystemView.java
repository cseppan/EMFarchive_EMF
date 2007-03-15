package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

public class EmfServerFileSystemView extends FileSystemView {
    
    public EmfServerFileSystemView () {
        super();
    }

    public File createNewFolder(File folder) throws IOException  {
        if (!folder.isDirectory())
            return null;
        
        try {
            File newFolder = new File(folder.getAbsolutePath() + File.separatorChar + "newfolder");
            return newFolder.mkdir() ? newFolder.getCanonicalFile() : null;
        } catch (Exception e) {
            throw new IOException("User is not allowed to create a new folder. " + e.getMessage());
        }
    }

}
