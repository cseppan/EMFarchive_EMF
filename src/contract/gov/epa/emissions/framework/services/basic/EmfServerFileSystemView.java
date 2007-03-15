package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

public class EmfServerFileSystemView extends FileSystemView {
    
    public EmfServerFileSystemView () {
        super();
    }

    public File createNewFolder(File folder) throws IOException  {
        return folder.mkdirs() ? folder.getCanonicalFile() : null;
    }

}
