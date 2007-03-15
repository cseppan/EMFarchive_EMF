package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.filechooser.FileSystemView;

public class EmfFileSystemView extends FileSystemView implements Serializable {
    private DataCommonsService service;

    public EmfFileSystemView(DataCommonsService service) {
        super();
        this.service = service;
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
        try {

            EmfFileInfo fileInfo = null;

            if (dir != null)
                fileInfo = EmfFileSerializer.convert(dir);

            EmfFileInfo[] fileInfos = service.getEmfFileInfos(fileInfo);
            
            return getEmfFiles(fileInfos);
        } catch (Exception e) {
            return new File[0];
        }
    }

    private File[] getEmfFiles(EmfFileInfo[] fileInfos) {
        File[] files = new File[fileInfos.length];

        for (int i = 0; i < files.length; i++)
            files[i] = EmfFileSerializer.convert(fileInfos[i]);
        return files;
    }

    public File createNewFolder(File folder) throws IOException {
        try {
            EmfFileInfo newFolder = service.createNewFolder(folder.getAbsolutePath());
            return EmfFileSerializer.convert(newFolder);
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw new IOException("cann't create new folder.");
        }
    }

    public File getDefaultDirectory() {
        try {
            EmfFileInfo defaultDir = service.getDefaultDir();

            return EmfFileSerializer.convert(defaultDir);
        } catch (EmfException e) {
            return null;
        }
    }

    public File getHomeDirectory() {
        try {
            EmfFileInfo homeDir = service.getHomeDir();
            
            return EmfFileSerializer.convert(homeDir);
        } catch (EmfException e) {
            return null;
        }
    }

}
