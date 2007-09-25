package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.data.EmfDateFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EMFCmdClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private static ServiceLocator serviceLocator;

    public static void main(String[] args) throws Exception {
        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList(args));

        if (args.length <= 1) {
            displayHelp();
            return;
        }

        if (args.length == 2 && (args[1].equalsIgnoreCase("-h") || args[1].equalsIgnoreCase("--help"))) {
            displayHelp();
            return;
        }

        if (!options.contains("-k")) {
            System.out.println("Please specify required options '-k'.");
            displayHelp();
            return;
        }

        int keyIndex = options.indexOf("-k");
        int typeIndex = options.indexOf("-t");
        String keyString = options.get(++keyIndex);
        String typeString = (typeIndex < 0) ? "" : options.get(++typeIndex);

        if (keyString.startsWith("-")) {
            System.out.println("Please specify a correct jobkey.");
            return;
        }

        String msgTypeError = "Please specify a correct message type - i (info), e (error), w (warning).";

        if (typeString != null && !typeString.isEmpty() && typeString.startsWith("-")) {
            System.out.println(msgTypeError);
            return;
        }

        if (typeString != null && !typeString.isEmpty() && !typeString.equalsIgnoreCase("i")
                && !typeString.equalsIgnoreCase("e") && !typeString.equalsIgnoreCase("w")) {
            System.out.println(msgTypeError);
            return;
        }

        run(options);
    }

    private static void displayHelp() {
        System.out.println("Usage\njava " + EMFClient.class.getName() + " [url] [options]\n"
                + "\n\turl: location of EMF Services. Defaults to " + DEFAULT_URL + "\n" + "\n\toptions:\n"
                + "\n\t-h --help\tShow this help message and exit" + "\n\t-k --jobkey\tJob key code"
                + "\n\t-x --execPath\tFull path to script or executable" + "\n\t-p --period\tEMF period"
                + "\n\t-m --message\tText of message"
                + "\n\t-s --status\tStatus, acceptable values: 'Submitted', 'Running', 'Failed', or 'Completed'"
                + "\n\t-t --msg_type\tMessage type: i (info), e (error), w (warning)");
    }

    /**
     * 
     */
    private static void run(List<String> args) throws Exception {
        int keyIndex = args.indexOf("-k");
        int execIndex = args.indexOf("-x");
        int periodIndex = args.indexOf("-p");
        int msgIndex = args.indexOf("-m");
        int statusIndex = args.indexOf("-s");
        int typeIndex = args.indexOf("-t");
        int logIntervalIndex = args.indexOf("-l");
        int resendTimesIndex = args.indexOf("-r");
        String jobkey = (keyIndex < 0) ? "" : args.get(++keyIndex);
        String execPath = (execIndex < 0) ? "" : args.get(++execIndex);
        String period = (periodIndex < 0) ? "" : args.get(++periodIndex);
        String message = (msgIndex < 0) ? "" : args.get(++msgIndex);
        String status = (statusIndex < 0) ? "" : args.get(++statusIndex);
        String type = (typeIndex < 0) ? "" : args.get(++typeIndex);
        int logInterval = (logIntervalIndex < 0) ? 0 : Integer.parseInt(args.get(++logIntervalIndex));
        int resendTimes = (resendTimesIndex < 0) ? 1 : Integer.parseInt(args.get(++resendTimesIndex));
        String loggerDir = System.getenv("EMF_LOGGERDIR");
        String jobName = System.getenv("EMF_JOBNAME");
        String caseName = System.getenv("CASE");
        String logFile = loggerDir + File.separator + jobName + "_" + caseName + "_" + jobkey + ".csv";

        if (execPath.startsWith("-") || period.startsWith("-") || message.startsWith("-") || status.startsWith("-"))
            throw new Exception("Please specify valid values for options.");

        File execFile = new File(execPath);

        JobMessage jobMsg = new JobMessage();
        jobMsg.setExecName(execPath.isEmpty() ? "" : execPath.substring(execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setExecPath(execPath.isEmpty() ? "" : execPath.substring(0, execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setMessage(message);
        jobMsg.setMessageType((type.isEmpty()) ? "i" : type);
        jobMsg.setStatus(status);
        jobMsg.setPeriod(period);
        jobMsg.setRemoteUser(System.getProperty("user.name"));
        jobMsg.setExecModifiedDate(execFile.exists() ? new Date(execFile.lastModified()) : null);

        if (loggerDir == null || loggerDir.isEmpty())
            sendMessage(args, jobkey, jobMsg);
        else
            writeToLogger(args, logFile, logInterval, resendTimes, jobkey, jobMsg);
    }

    private static void sendMessage(List<String> args, String jobkey, JobMessage jobMsg) throws Exception {
        try {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey });
        } catch (Exception exc) {
            System.out.println("Exception starting client: " + exc.getMessage());
            throw exc;
        }
    }

    private static void sendLogs(List<String> args, int logInterval, int resendTimes, String logfile, String jobkey,
            JobMessage jobMsg, boolean now) throws Exception {
        List<JobMessage> msgs = new ArrayList<JobMessage>();
        List<String> keys = new ArrayList<String>();
        long start = 0;
        long end = 0;

        File logFile = new File(logfile);

        if (!logFile.exists()) {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey });
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String line = br.readLine().trim();
        long sentLines = Long.parseLong(line.substring(1));
        long lineCount = 1;

        while ((line = br.readLine()) != null) {
            ++lineCount;

            if (lineCount >= sentLines) {
                msgs.add(extractJobMsg(line));
                keys.add(extractJobkey(line));

                if (start == 0)
                    start = getTime(line);

                end = getTime(line);
            }
        }

        br.close();

        if (now || ((end - start) / 1000 > logInterval)) {
            resend(args, resendTimes, msgs, keys);
            try {
                rewriteSentLinesNumber(++lineCount, logfile, logfile + ".tmp");
            } finally {
                new File(logfile + ".tmp").delete();
            }
        }
    }

    private static void rewriteSentLinesNumber(long lineCount, String logfile, String tmpfile) throws Exception {
        String[] cmd = null;
        String os = System.getProperty("os.name").toUpperCase();

        if (os.startsWith("WINDOWS")) {
            String cmd1 = "ECHO #" + lineCount + " > \"" + tmpfile + "\"";
            String cmd2 = "FIND /V \"#\" < \"" + logfile + "\" >> \"" + tmpfile + "\"";
            String cmd3 = "TYPE \"" + tmpfile + "\" > \"" + logfile + "\"";
            cmd = new String[] { "cmd.exe", "/C", cmd1 + " & " + cmd2 + " & " + cmd3 };
        } else {
            String cmd1 = "echo \\#" + lineCount + " > \"" + tmpfile + "\"";
            String cmd2 = "grep -v \"^#\" \"" + logfile + "\" >> \"" + tmpfile + "\"";
            String cmd3 = "cat \"" + tmpfile + "\" > \"" + logfile + "\"";
            cmd = new String[] { "sh", "-c", cmd1 + ";" + cmd2 + ";" + cmd3 };
        }

        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }

    private static void resend(List<String> args, int resendTimes, List<JobMessage> msgs, List<String> keys) {
        while (resendTimes > 0) {
            try {
                send(args, msgs.toArray(new JobMessage[0]), keys.toArray(new String[0]));
                resendTimes = 0;
            } catch (Exception e) {
                --resendTimes;
            }
        }
    }

    private static void send(List<String> args, JobMessage[] msgs, String[] keys) throws Exception {
        System.out.println("Starting EMF Command Client");
        getService(args).recordJobMessage(msgs, keys);
        System.out.println("Exiting EMF Command Client");
    }

    private static void writeToLogger(List<String> args, String logFile, int logInterval, int resendTimes,
            String jobkey, JobMessage jobMsg) throws Exception {
        boolean logFileExisted = new File(logFile).exists();

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
        writeLogs(writer, jobMsg, jobkey, logFileExisted);
        writer.close();

        if (jobMsg.getStatus() == null || jobMsg.getStatus().isEmpty())
            sendLogs(args, logInterval, resendTimes, logFile, jobkey, jobMsg, false);
        else
            sendLogs(args, logInterval, resendTimes, logFile, jobkey, jobMsg, true);
    }

    private static void writeLogs(PrintWriter writer, JobMessage jobMsg, String jobkey, boolean logFileExisted) {
        String msg = jobkey;
        msg += "," + jobMsg.getExecName();
        msg += "," + jobMsg.getExecPath();
        msg += "," + jobMsg.getMessage();
        msg += "," + jobMsg.getMessageType();
        msg += "," + jobMsg.getStatus();
        msg += "," + jobMsg.getPeriod();
        msg += "," + jobMsg.getRemoteUser();
        msg += "," + EmfDateFormat.format_MM_DD_YYYY_HH_mm(jobMsg.getExecModifiedDate());
        msg += "," + new Date().getTime();

        if (!logFileExisted) {
            writer.println("#3"); // line number to start extract job messages and send
            writer.println("job key, exec name,exec path,message,"
                    + "message type,status,period,user,last mod date,log time(ms)");
        }

        writer.println(msg);
    }

    private static CaseService getService(List<String> args) throws Exception {
        String url = DEFAULT_URL;

        if (!args.get(0).startsWith("-"))
            url = args.get(0);

        if (serviceLocator == null)
            serviceLocator = new RemoteServiceLocator(url);

        return serviceLocator.caseService();
    }

    private static long getTime(String line) {
        int timeIndex = line.lastIndexOf(",");
        String time = line.substring(timeIndex + 1);

        if (time != null || !time.trim().isEmpty())
            return Long.parseLong(time.trim());

        return 0;
    }

    private static String extractJobkey(String line) {
        return line.substring(0, line.indexOf(","));
    }

    private static JobMessage extractJobMsg(String line) throws Exception {
        CommaDelimitedTokenizer tokenizer = new CommaDelimitedTokenizer();
        String[] fields = tokenizer.tokens(line);

        JobMessage msg = new JobMessage();
        msg.setExecName(fields[1]);
        msg.setExecPath(fields[2]);
        msg.setMessage(fields[3]);
        msg.setMessageType(fields[4]);
        msg.setStatus(fields[5]);
        msg.setPeriod(fields[6]);
        msg.setRemoteUser(fields[7]);

        try {
            msg.setExecModifiedDate((fields[8] == null || fields[8].trim().isEmpty()) ? null : EmfDateFormat
                    .parse_MM_DD_YYYY_HH_mm(fields[8]));
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }

        return msg;
    }

}
