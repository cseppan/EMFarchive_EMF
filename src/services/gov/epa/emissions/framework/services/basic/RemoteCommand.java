package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RemoteCommand {
    /**
     * Performes a command on a remote machine (or localhost)
     */
    private static Log LOG = LogFactory.getLog(RemoteCommand.class);

    private static String qID;

    private static final String lineSep = System.getProperty("line.separator");

    public static void logStdout(String title, InputStream inStream, boolean localHost) throws EmfException {
        /**
         * log the stdout from a remote command to the log
         */
        BufferedReader reader = null;

        // log the title of this series of message to the LOG
        LOG.warn(title);

        reader = new BufferedReader(new InputStreamReader(inStream));

        if (reader != null) {
            try {
                String message = reader.readLine();
                String lstNonNullMsg = message;
                boolean foundQid = false;

                if (message == null) {
                    message = reader.readLine();
                    lstNonNullMsg = message;
                }
                
                while (message != null) {
                    lstNonNullMsg = message;
                    LOG.warn(message);

                    if (!foundQid)
                        foundQid = extractQId(message);
                    
                    message = reader.readLine();
                }
                
                reader.close();
                
                if (!foundQid && !localHost)
                    throw new EmfException("please check your queue options " + (lstNonNullMsg == null ? "" : "(" + lstNonNullMsg + ")"));
            } catch (Exception e) {
                e.printStackTrace();
                throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
            }
        }
    }
    
    public static void logRemoteStdout(String title, InputStream inStream) throws EmfException {
        /**
         * log the stdout from a remote command to the log
         */
        BufferedReader reader = null;

        // log the title of this series of message to the LOG
        LOG.warn(title);

        reader = new BufferedReader(new InputStreamReader(inStream));

        if (reader != null) {
            try {
                String message = reader.readLine();

                if (message == null)
                    message = reader.readLine();
                
                while (message != null) {
                    LOG.warn(message);
                    message = reader.readLine();
                }
                
                reader.close();
            } catch (Exception e) {
                throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
            }
        }
    }

    private static boolean extractQId(String message) {
        if (message == null || message.trim().isEmpty())
            return false;

        // TBD: need to generalize this to accept other queue ID formats - perhaps with
        //      new property
        Pattern p = Pattern.compile("^[0-9]*\\.(.)*");
        Matcher m = p.matcher(message.trim());

        if (m.find()) {
            qID = message.trim();
            return true;
        }

        return false;
    }

    public static String logStderr(String title, InputStream inStream) throws Exception {
        // log the title of this series of message to the LOG
        LOG.error(title);

        /**
         * log the stderr from a remote command to the log
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        String errorMsg = null;

        if (reader == null)
            return errorMsg;

        try {
            String message = reader.readLine();
            errorMsg = message;

            while (message != null) {
                LOG.error(message);
                message = reader.readLine();
            }
            
            return errorMsg;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    public static InputStream execute(String username, String hostname, String remoteCmd) throws EmfException {
        /**
         * Executes command on a remote machine -- short form Inputs: username - username on remote machine
         * 
         * hostname - hostname of remote machine
         * 
         * remoteCmd = command to execute on remote machine
         * 
         * Outputs - InputStream - the output from the remote command (stdout)
         */
        // some command elements
        String unixShell = "csh";
        String unixOptions = "-c";
        String sshCmd = "ssh";
        String sshOptions = "-o PasswordAuthentication=no";

        try {
            return execute(unixShell, unixOptions, sshCmd, sshOptions, username, hostname, remoteCmd);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public static InputStream executeLocal(String localCmd) throws EmfException {
        /**
         * Executes command on local machine -- short form
         * 
         * Inputs: localCmd - command to execute on this machine
         */

        // some command elements
        String unixShell = "csh";
        String unixOptions = "-c";

        try {
            return executeLocal(unixShell, unixOptions, localCmd);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private static int processSleep(Process p) throws EmfException {
        /**
         * Tests the remote process and waits a predetermined amount of time for a return
         * 
         * time it waits is 1 minute
         */
        int errorLevel = 2;

        try {
            errorLevel = p.exitValue();
        } catch (IllegalThreadStateException e) {
            // process is not finished wait 10 sec
            try {
                Thread.sleep(10000);
            } catch (Exception eT) {
                // do nothing
            }

            try {
                errorLevel = p.exitValue();
            } catch (IllegalThreadStateException e2) {
                // process is not finished wait 50 sec
                try {
                    Thread.sleep(50000);
                } catch (Exception eT) {
                    // do nothing
                }
                try {
                    errorLevel = p.exitValue();
                } catch (IllegalThreadStateException e3) {
                    // process still hasn't finished return error
                    p.destroy();
                    throw new EmfException("Remote command ssh has not responded for 1 minute, killing subprocess");
                }
            }
        }
        return errorLevel;
    }

    public static InputStream execute(String unixShell, String unixOptions, String sshCmd, String sshOptions,
            String username, String hostname, String remoteCmd) throws EmfException {
        /**
         * Executes a command on a remote machine -- long form
         * 
         * Inputs: unixShell - unix shell to operate under
         * 
         * unixOptions - unix options for this shell
         * 
         * sshCmd - ssh or other remote access command
         * 
         * sshOptions - remote access options
         * 
         * username - username on remote machine
         * 
         * hostname - hostname of remote machine
         * 
         * remoteCmd = command to execute on remote machine
         * 
         * Outputs - InputStream - the output from the remote command (stdout)
         */

        int errorLevel = 0;
        String[] cmds = new String[3];
        String executeCmd = null;
        executeCmd = sshCmd + " " + sshOptions + " " + username + "@" + hostname + " " + remoteCmd;
        cmds[0] = unixShell;
        cmds[1] = unixOptions;
        cmds[2] = executeCmd;

        try {
            Process p = Runtime.getRuntime().exec(cmds);

            errorLevel = processSleep(p);

            if (errorLevel > 0) {
                // if have error print remote commands error message to the logs
                // and throw an exception
                String errorTitle = "stderr from (" + hostname + "): " + remoteCmd;
                String errorMsg = logStderr(errorTitle, p.getErrorStream());
                
                if (errorMsg == null)
                    errorMsg = "Error message not readable from remote machine.";

//                throw new EmfException("ERROR executing remote command: " + executeCmd + lineSep + errorMsg);
                throw new EmfException(errorMsg);

            }

            return p.getInputStream();

        } catch (Exception e) {
            LOG.error("ERROR executing remote command: " + executeCmd, e);
            throw new EmfException("ERROR executing remote command: " + executeCmd);
        }
    }

    public static InputStream executeLocal(String unixShell, String unixOptions, String localCmd) throws EmfException {
        /**
         * Executes a command on a local machine -- long form
         * 
         * Inputs: unixShell - unix shell to operate under
         * 
         * unixOptions - unix options for this shell
         * 
         * localCmd - command to execute on this machine
         */

        String[] cmds = new String[3];
        cmds[0] = unixShell;
        cmds[1] = unixOptions;
        cmds[2] = localCmd;
        int errorLevel = -1;

        try {
            Process p = Runtime.getRuntime().exec(cmds);
            try {
                errorLevel = p.exitValue();
            } catch (IllegalThreadStateException e2) {
                // process is not finished wait -- don't wait
            }

            if (DebugLevels.DEBUG_0)
                LOG.warn("Started command on the local EMF machine: " + localCmd);

            if (errorLevel > 0) {
                // error in local command
                String errorMsg = logStderr("Error from localhost", p.getErrorStream());
                
                if (errorMsg == null)
                    errorMsg = "Error message not readable from localhost.";
                
                throw new EmfException("ERROR executing local command: " + localCmd + lineSep + errorMsg);
            }

            return p.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("ERROR in executing local command: " + localCmd);
            throw new EmfException("ERROR executing local command: " + e.getMessage());
        }
    }

    public static String getQueueId() {
        return qID;
    }

}
