package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.filechooser.FileSystemView;

public class EmfFileSystemView extends FileSystemView implements Serializable {
    private DataCommonsService service;
    
    public EmfFileSystemView (DataCommonsService service) {
        super();
        this.service = service;
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
        try {
            String[] files = service.getFiles(dir.getAbsolutePath());
            File[] files2Return = new File[files.length];
            
            for (int i = 0; i < files.length; i++)
                files2Return[i] = new File(files[i]);
            
            return files2Return;
        } catch (EmfException e) {
            return new File[0];
        }
    }
    
    public File createNewFolder(File folder) throws IOException {
        String newFolder = service.createNewFolder(folder.getAbsolutePath());
        return new File(newFolder);
    }

}
