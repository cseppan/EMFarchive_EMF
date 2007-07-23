package gov.epa.emissions.framework.client.remote;

import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RemoteCopy {

    private String program;

    private String tempDir;

    private String host;

    private String os;

    private int errorLevel;

    private String errorString;
    
    private String userName;

    public RemoteCopy(UserPreference pref) throws EmfException {
        os = System.getProperty("os.name");
        this.program = pref.remoteCopyProgram();
        this.tempDir = pref.localTempDir();
        this.host = System.getProperty("emf.remote.host");
        this.userName = System.getProperty("user.name");
        checkParameters();
    }

    public String getProgram() {
        return this.program;
    }

    public String getTempDir() {
        return this.tempDir;
    }

    private void checkParameters() throws EmfException {
        if (this.host == null || this.host.isEmpty())
            host = "localhost";

        if (this.tempDir == null || this.tempDir.isEmpty())
            tempDir = ".";
        
        if (!tempDir.equals(".") && ! new File(tempDir).exists())
            throw new EmfException("User specified temporary directory is invalid.");

        if (this.program == null || this.program.isEmpty())
            throw new EmfException("A valid remote copy program must be specified in the preference file (EMFPrefs.txt).");
    }

    public String copyToLocal(String remotePath, String localPath) throws EmfException {
        if (remotePath == null || remotePath.isEmpty())
            throw new EmfException("Remote copy: a valid remote path must be specified.");

        String separator = (remotePath.charAt(0) == '/') ? "/" : "\\";
        int lastSeparatorIndex = remotePath.lastIndexOf(separator);
        String remotefile = remotePath.substring(++lastSeparatorIndex);

        localPath = (localPath == null || localPath.isEmpty()) ? (tempDir + separator + remotefile) : localPath;

        if (new File(localPath).exists())
            return localPath;

        String command = this.program + " " + userName + "@" + this.host + ":" + remotePath + " " + localPath;
        execute(command);

        if (errorLevel > 0)
            throw new EmfException(processError());
        
        return localPath;
    }

    public String copyToRemote(String localPath, String remotePath) throws EmfException {
        if (remotePath == null || remotePath.isEmpty())
            throw new EmfException("A valid remote path must be specified in the preference file (EMFPrefs.txt).");

        if (localPath == null || localPath.isEmpty())
            throw new EmfException("Remote copy: a valid local path must be specified.");

        String command = this.program + " " + userName + "@" + localPath + " " + this.host + ":" + remotePath;
        execute(command);

        if (errorLevel > 0)
            throw new EmfException(processError());
        
        return remotePath;
    }

    private void execute(String command) throws EmfException {
        String[] cmds = getCommands(command);
        BufferedReader reader = null;

        try {
            Process p = Runtime.getRuntime().exec(cmds);
            errorLevel = p.waitFor();

            if (errorLevel > 0) {
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                errorString = reader.readLine();
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (reader != null)
                closeReader(reader);
        }
    }

    private String[] getCommands(String command) {
        if (os.equalsIgnoreCase("Linux") || os.equalsIgnoreCase("Unix")) {
            return new String[] { "sh", "-c", command };
        }

        String[] cmd = new String[3];

        if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            cmd[0] = "command.com";
        } else {
            cmd[0] = "cmd.exe";
        }

        cmd[1] = "/C";
        cmd[2] = command;

        return cmd;
    }

    private String processError() {
        if (errorString == null || errorString.isEmpty())
            return "";
        
        errorString = errorString.toLowerCase();
        
        if (errorString.contains("no supported authentication"))
            return "Local key agent not started or local/remote ssh settings not right.";

        if (errorString.contains("cannot create file"))
            return "Please check temporary folder permission/existance specified in the preference file (EMFPrefs.txt).";

        if (errorString.contains("not recognized as an internal or external command"))
            return "Please check the ssh program specified in the preference file (EMFPrefs.txt).";
        
        if (errorString.contains("disconnected") || errorString.contains("connection refused"))
            return "Please check your network connection or ssh connection.";

        if (errorString.contains("no such file or directory"))
            return "Please check if you have exported the result or the export folder was right.";
        
        return errorString + ". Please check your preference settings.";
    }
    
    private void closeReader(BufferedReader reader) throws EmfException {
        try {
            reader.close();
        } catch (IOException e) {
            throw new EmfException(e.getMessage());
        }
    }

}
