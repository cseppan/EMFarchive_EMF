package gov.epa.emissions.framework.client.preference;

public interface UserPreference {

    boolean checkFile(String fileName);

    String inputFolder();

    String outputFolder();

    String mapLocalInputPathToRemote(String localPath);

    String mapLocalOutputPathToRemote(String localPath);
    
    String mapRemoteOutputPathToLocal(String remotePath);
    
    String userName();
    
    String userPassword();
    
    String remoteCopyProgram();
    
    String localTempDir();
    
    String remoteHost();
    
    String sortFilterPageSize();
}