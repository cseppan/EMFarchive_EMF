package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.filechooser.FileSystemView;

public class EmfFileSystemView extends FileSystemView implements Serializable {

    private DataCommonsService service;

    private File[] systemRoots;
    
    private EmfFileInfo[] emfRoots;
    
    public EmfFileSystemView(DataCommonsService service) {
        super();
        this.service = service;
    }

    public EmfFileInfo[] getFiles(EmfFileInfo dir, boolean useFileHiding) {
        try {
            return service.getEmfFileInfos(dir);
        } catch (Exception e) {
            System.out.println("getFiles exception: " + e.getMessage());
            return new EmfFileInfo[0];
        }
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
        try {
            System.out.println("client side getFiles called. Diretory selected: " + dir.getAbsolutePath());
            EmfFileInfo fileInfo = null;

            if (dir != null)
                fileInfo = EmfFileSerializer.convert(dir);

            EmfFileInfo[] fileInfos = service.getEmfFileInfos(fileInfo);

            return getEmfFiles(fileInfos);
        } catch (Exception e) {
            System.out.println("getFiles exception: " + e.getMessage());
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
            System.out.println("createNewFolder exception: " + e.getMessage());
            throw new IOException("cann't create new folder.");
        }
    }

    public EmfFileInfo createNewFolder(EmfFileInfo folder) throws IOException {
        try {
            return service.createNewFolder(folder.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("createNewFolder exception: " + e.getMessage());
            throw new IOException("cann't create new folder.");
        }
    }

    public File getDefaultDirectory() {
        try {
            System.out.println("clien side getDefaultDirectory called.");
            EmfFileInfo defaultDir = service.getDefaultDir();
            System.out.println("default dir returned:" + defaultDir.getAbsolutePath());
            return EmfFileSerializer.convert(defaultDir);
        } catch (EmfException e) {
            System.out.println("getDefaultDirectory exception: " + e.getMessage());
            return null;
        }
    }

    public EmfFileInfo getDefaultDir() {
        try {
            System.out.println("clien side getDefaultDirectory called.");
            EmfFileInfo defaultDir = service.getDefaultDir();
            System.out.println("default dir returned:" + defaultDir.getAbsolutePath());
            return defaultDir;
        } catch (EmfException e) {
            System.out.println("getDefaultDirectory exception: " + e.getMessage());
            return null;
        }
    }

    public File getHomeDirectory() {
        try {
            System.out.println("clien side getHomeDirectory called.");
            EmfFileInfo homeDir = service.getHomeDir();
            System.out.println("hoem dir returned:" + homeDir.getAbsolutePath());
            return EmfFileSerializer.convert(homeDir);
        } catch (EmfException e) {
            System.out.println("getHomeDirectory exception: " + e.getMessage());
            return null;
        }
    }

    public EmfFileInfo getHomeDir() {
        try {
            System.out.println("clien side getHomeDirectory called.");
            EmfFileInfo homeDir = service.getHomeDir();
            System.out.println("hoem dir returned:" + homeDir.getAbsolutePath());
            return homeDir;
        } catch (EmfException e) {
            System.out.println("getHomeDirectory exception: " + e.getMessage());
            return null;
        }
    }

    public File[] getRoots() {
        try {
            //systemRoots = super.getRoots();
             if (systemRoots == null)
                this.systemRoots = getEmfFiles(service.getRoots());
//             for (int i = 0; i < systemRoots.length; i++)
//                 System.out.println("systemRoots[" + i + "]: " + systemRoots[i].getAbsolutePath());
           
//            System.out.println("systemRoots: " + systemRoots.length + " 1: " + systemRoots[0].getAbsolutePath());
            return systemRoots;
        } catch (Exception e) {
            System.out.println("getRoots exception: " + e.getMessage());
            return null;
        }
    }

    public EmfFileInfo[] getEmfRoots() {
        try {
            if (emfRoots == null)
                this.emfRoots = service.getRoots();
            
            return emfRoots;
        } catch (Exception e) {
            System.out.println("getRoots exception: " + e.getMessage());
            return null;
        }
    }

    public boolean isRoot(File file) {
        try {
            boolean retVal;
            if (systemRoots == null)
                retVal= service.isRoot(EmfFileSerializer.convert(file));
             
            retVal= contains(file, systemRoots);
            System.out.println("client side isRoot super="+super.isRoot(file)+",retVal="+retVal+","+
                    file.getAbsolutePath());
            if (file.getAbsolutePath().equals("/")) 
                retVal = true;
            return retVal;
        } catch (Exception e) {
            System.out.println("isRoot exception: " + e.getMessage());
            return false;
        }
    }

    public boolean isRoot(EmfFileInfo file) {
        try {
            boolean retVal;
            if (emfRoots == null)
                retVal= service.isRoot(file);
            
            retVal= contains(file, emfRoots);
            
            if (file.getAbsolutePath().equals("/")) 
                retVal = true;
           
            return retVal;
        } catch (Exception e) {
            System.out.println("isRoot exception: " + e.getMessage());
            return false;
        }
    }

    public boolean isFileSystemRoot(EmfFileInfo file) {
        try {
            boolean retVal;
            retVal= file.getAbsolutePath().equals("/") || 
               ((file.getAbsolutePath().length()==3) && (file.getAbsolutePath().charAt(1)==':'));
            return retVal;
        } catch (Exception e) {
            System.out.println("isFileSystemRoot exception: " + e.getMessage());
            return false;
        }
    }

    public boolean isFileSystemRoot(File file) {
        try {
            boolean retVal;
            retVal= file.getAbsolutePath().equals("/") || 
            ((file.getAbsolutePath().length()==3) && (file.getAbsolutePath().charAt(1)==':'));
            System.out.println(
                    "client side isFileSystemRoot super="+super.isFileSystemRoot(file)+", retVal="+retVal+"," + file.getAbsolutePath() + " length: " + file.getAbsolutePath().length());
            return retVal;
        } catch (Exception e) {
            System.out.println("isFileSystemRoot exception: " + e.getMessage());
            return false;
        }
    }

    public boolean isDrive(File file) {
        System.out.println("client side isDrive called.");
        return isFileSystemRoot(file);
    }

    public boolean isDrive(EmfFileInfo file) {
        System.out.println("client side isDrive called.");
        return isFileSystemRoot(file);
    }

    private boolean contains(File file, File[] files) {
        boolean result = false;
        String filepath = file.getAbsolutePath();

        for (int i = 0; i < files.length; i++)
            if (filepath.equals(files[i].getAbsolutePath()))
                result = true;

        return result;
    }

    private boolean contains(EmfFileInfo file, EmfFileInfo[] files) {
        boolean result = false;
        String filepath = file.getAbsolutePath();
        
        for (int i = 0; i < files.length; i++)
            if (filepath.equals(files[i].getAbsolutePath()))
                result = true;
        
        return result;
    }
    
    public EmfFileInfo getChild(EmfFileInfo file, String child) {
        try {
            System.out.println("client side getChild called. File: " + file.getAbsolutePath() + " child: " + child);
            EmfFileInfo fileInfo = service.getChild(file, child);
            System.out.println("parent file: " + file.getAbsolutePath() + " child returned: " + fileInfo.getAbsolutePath());
            return fileInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public EmfFileInfo[] getSubdirs(EmfFileInfo file) {
        try {
            return service.getSubdirs(file);
        } catch (Exception e) {
            return null;
        }
    }

    public File getChild(File file, String child) {
        try {
            System.out.println("client side getChild called. File: " + file.getAbsolutePath() + " child: " + child);
            EmfFileInfo fileInfo = service.getChild(EmfFileSerializer.convert(file), child);
            System.out.println("parent file: " + file.getAbsolutePath() + " child returned: " + fileInfo.getAbsolutePath());
            return EmfFileSerializer.convert(fileInfo);
        } catch (Exception e) {
            return null;
        }
    }
    
    public File getParentDirectory(File file) {
        try {
            File parent = super.getParentDirectory(file);
            System.out.println("file: " + file.getAbsolutePath() + " super() parent returned: " + parent.getAbsolutePath());
            System.out.println("parent directory returned from server: " + service.getParentDirectory(EmfFileSerializer.convert(file)).getAbsolutePath());
            return parent;
        } catch (Exception e) {
            return null;
        }
    }

    public EmfFileInfo getParentDirectory(EmfFileInfo file) {
        try {
            return service.getParentDirectory(file);
        } catch (Exception e) {
            return null;
        }
    }

}
