package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
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

    private static boolean DEBUG = false;

    public synchronized static void main(String[] args) throws Exception {
        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList(args));

        if (options.contains("-d")) {
            DEBUG = true;
        }

        if (DEBUG)
            System.out.println("EMF command line client initialized at: " + new Date());

        if (args.length <= 1) {
            displayHelp();
            return;
        }

        if (args.length == 2 && (args[1].equalsIgnoreCase("-h") || args[1].equalsIgnoreCase("--help"))) {
            displayHelp();
            return;
        }

        if (options.contains("-f")) {
            runFromFile(options);
            return;
        }

        if (!options.contains("-k")) {
            System.out.println("Please specify required options '-k'.");
            displayHelp();
            return;
        }

        if ((options.contains("-F") || options.contains("-P")) && !options.contains("-T")) {
            System.out.println("Please specify required dataset type. Outputs not registered.");
            displayHelp();
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

    private synchronized static void displayHelp() {
        System.out.println("Usage:\njava " + EMFClient.class.getName() + " [url] [options]\n"
                + "\n\turl: location of EMF Services. Defaults to " + DEFAULT_URL + "\n" + "\n\toptions:\n"
                + "\n\t-h --help\tShow this help message and exit" + "\n\t-k --jobkey\tJob key code"
                + "\n\t-x --execPath\tFull path to script or executable" + "\n\t-p --period\tEMF period"
                + "\n\t-m --message\tText of message"
                + "\n\t-s --status\tStatus, acceptable values: 'Submitted', 'Running', 'Failed', or 'Completed'"
                + "\n\t-t --msg_type\tMessage type: i (info), e (error), w (warning)"
                + "\n\t-F single file to register as a dataset"
                + "\n\t-D path to the directory where files to register as datasets"
                + "\n\t-P pattern of the files to register as datasets" 
                + "\n\t-T dataset type" 
                + "\n\t-N dataset name"
                + "\n\t-O output name (optional, default to dataset name)");

        System.out.println("\nOr:\toptions:\n" + "\n\t-f --file\tSend job messages directly from this file\n");
    }

    private synchronized static void run(List<String> args) throws Exception {
        int keyIndex = args.indexOf("-k");
        int execIndex = args.indexOf("-x");
        int periodIndex = args.indexOf("-p");
        int msgIndex = args.indexOf("-m");
        int statusIndex = args.indexOf("-s");
        int typeIndex = args.indexOf("-t");
        int logIntervalIndex = args.indexOf("-l");
        int resendTimesIndex = args.indexOf("-r");
        int outputFileIndex = args.indexOf("-F");
        int outputFolderIndex = args.indexOf("-D");
        int outputPatternIndex = args.indexOf("-P");
        int outputTypeIndex = args.indexOf("-T");
        int outputDatasetNameIndex = args.indexOf("-N");
        int outputNameIndex = args.indexOf("-O");

        String jobkey = (keyIndex < 0) ? "" : args.get(++keyIndex);
        String execPath = (execIndex < 0) ? "" : args.get(++execIndex);
        String period = (periodIndex < 0) ? "" : args.get(++periodIndex);
        String message = (msgIndex < 0) ? "" : args.get(++msgIndex);
        String status = (statusIndex < 0) ? "" : args.get(++statusIndex);
        String type = (typeIndex < 0) ? "" : args.get(++typeIndex);
        int logInterval = (logIntervalIndex < 0) ? 0 : Integer.parseInt(args.get(++logIntervalIndex));
        int resendTimes = (resendTimesIndex < 0) ? 1 : Integer.parseInt(args.get(++resendTimesIndex));
        String outputFile = (outputFileIndex < 0) ? "" : args.get(++outputFileIndex);
        String outputFolder = (outputFolderIndex < 0) ? "" : args.get(++outputFolderIndex);
        String outputPattern = (outputPatternIndex < 0) ? "" : args.get(++outputPatternIndex);
        String outputType = (outputTypeIndex < 0) ? "" : args.get(++outputTypeIndex);
        String outputDatasetName = (outputDatasetNameIndex < 0) ? "" : args.get(++outputDatasetNameIndex);
        String outputName = (outputNameIndex < 0) ? "" : args.get(++outputNameIndex);

        String loggerDir = System.getenv("EMF_LOGGERDIR");
        String jobName = createSafeName(System.getenv("EMF_JOBNAME"));
        String caseName = createSafeName(System.getenv("CASE"));
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
        jobMsg.setReceivedTime(new Date());
        
        if (status.isEmpty() && message.isEmpty())
            jobMsg.setEmpty(true);

        CaseOutput output = new CaseOutput(outputName);
        output.setDatasetFile(outputFile);
        output.setDatasetName(outputDatasetName);
        output.setPath(outputFolder);
        output.setDatasetType(outputType);
        output.setPattern(outputPattern);
        output = setOutputEmptyProp(output);

        if (loggerDir == null || loggerDir.isEmpty())
            sendMessage(args, jobkey, jobMsg, output);
        else
            writeToLogger(args, logFile, logInterval, resendTimes, jobkey, jobMsg, output);
    }
    
    private synchronized static CaseOutput setOutputEmptyProp(CaseOutput output) {
        boolean empty = true;
        
        String[] fields = new String[] {
                output.getDatasetType(),
                output.getDatasetName(),
                output.getPath(),
                output.getDatasetFile(),
                output.getPattern(),
                output.getName()
        };
        
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                empty = false;
                break;
            }
        }
        
        output.setEmpty(empty);
        
        return output;
    }

    private synchronized static void runFromFile(List<String> args) throws Exception {
        int resendTimesIndex = args.indexOf("-r");
        int msgFileIndex = args.indexOf("-f");
        int resendTimes = (resendTimesIndex < 0) ? 1 : Integer.parseInt(args.get(++resendTimesIndex));
        String msgFile = (msgFileIndex < 0) ? "" : args.get(++msgFileIndex).trim();

        if (!msgFile.isEmpty()) {
            sendLogsFromFile(args, resendTimes, msgFile);
        }
    }

    private synchronized static void sendMessage(List<String> args, String jobkey, JobMessage jobMsg, CaseOutput output)
            throws Exception {
        int exitValue = 0;

        try {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey }, new CaseOutput[] { output });
        } catch (Exception exc) {
            String errorString = exc.getMessage();

            if (errorString != null && errorString.contains("Error in registering output"))
                exitValue = 2;

            if (errorString != null && errorString.contains("Error in recording job messages"))
                exitValue = 1;

            if (DEBUG)
                System.out.println("Exception starting client: " + exc.getMessage());

            throw exc;
        } finally {
            System.exit(exitValue);
        }
    }

    private synchronized static void sendLogs(List<String> args, int logInterval, int resendTimes, String logfile,
            String logAsistFile, String jobkey, JobMessage jobMsg, CaseOutput output, boolean now) throws Exception {
        List<JobMessage> msgs = new ArrayList<JobMessage>();
        List<String> keys = new ArrayList<String>();
        List<CaseOutput> outputs = new ArrayList<CaseOutput>();
        long start = 0;
        long end = 0;

        File logFile = new File(logfile);

        if (!logFile.exists()) {
            send(args, new JobMessage[] { jobMsg }, new String[] { jobkey }, new CaseOutput[] { output });
            return;
        }

        BufferedReader asistFileReader = new BufferedReader(new FileReader(logAsistFile));
        long sentLines = Long.parseLong(asistFileReader.readLine().trim());
        asistFileReader.close();

        BufferedReader br = new BufferedReader(new FileReader(logfile));
        String line = br.readLine();
        long lineCount = 1;

        while ((line = br.readLine()) != null) {
            ++lineCount;

            if (lineCount >= sentLines) {
                msgs.add(extractJobMsg(line));
                keys.add(extractJobkey(line));
                outputs.add(extractOutput(line));

                if (start == 0)
                    start = getTime(line);

                end = getTime(line);
            }
        }

        br.close();

        String errorString = null;

        if (now || ((end - start) / 1000 > logInterval)) {
            errorString = resend(args, resendTimes, msgs, keys, outputs);
            rewriteSentLinesNumber(++lineCount, logAsistFile);
        }

        if (DEBUG)
            System.out.println("EMF command line client exited.");

        if (errorString != null && errorString.contains("Error in registering output"))
            System.exit(2);

        if (errorString != null)
            System.exit(1);

        System.exit(0);
    }

    private synchronized static void sendLogsFromFile(List<String> args, int resendTimes, String logfile)
            throws Exception {
        List<JobMessage> msgs = new ArrayList<JobMessage>();
        List<String> keys = new ArrayList<String>();
        List<CaseOutput> outputs = new ArrayList<CaseOutput>();
        File logFile = new File(logfile);
        String logAsistFile = logfile + ".ast";
        File logAsistant = new File(logAsistFile);
        writeInitialAsistFile(logAsistant);

        if (!logFile.exists()) {
            System.out.println("Specified log file: " + logfile + " doesn't exist.");
            return;
        }

        if (!logAsistant.exists()) {
            System.out.println("Log asistant file: " + logAsistFile + " doesn't exist.");
            return;
        }

        BufferedReader asistFileReader = new BufferedReader(new FileReader(logAsistant));
        long sentLines = Long.parseLong(asistFileReader.readLine().trim());
        asistFileReader.close();

        BufferedReader br = new BufferedReader(new FileReader(logfile));
        String line = br.readLine();
        long lineCount = 1;

        while ((line = br.readLine()) != null) {
            ++lineCount;

            if (lineCount >= sentLines) {
                msgs.add(extractJobMsg(line));
                keys.add(extractJobkey(line));
                outputs.add(extractOutput(line));
            }
        }

        br.close();

        String errorString = null;

        if (msgs.size() > 0) {
            errorString = resend(args, resendTimes, msgs, keys, outputs);
            rewriteSentLinesNumber(++lineCount, logAsistFile);
        }

        if (errorString != null && errorString.contains("Error in registering output"))
            System.exit(2);

        if (errorString != null)
            System.exit(1);

        System.exit(0);
    }

    private synchronized static void rewriteSentLinesNumber(long lineCount, String logAsistFile) throws Exception {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logAsistFile)));
        writer.println(lineCount);
        writer.close();
    }

    private synchronized static String resend(List<String> args, int resendTimes, List<JobMessage> msgs,
            List<String> keys, List<CaseOutput> outputs) {
        String errorString = null;

        while (resendTimes > 0) {
            try {
                send(args, msgs.toArray(new JobMessage[0]), keys.toArray(new String[0]), outputs
                        .toArray(new CaseOutput[0]));
                resendTimes = 0;
            } catch (Exception e) {
                --resendTimes;
                errorString += e.getMessage();
            }
        }

        return errorString;
    }

    private synchronized static void send(List<String> args, JobMessage[] msgs, String[] keys, CaseOutput[] outputs)
            throws Exception {
        try {
            if (DEBUG)
                System.out.println("EMF Command Client starts sending messages to server at: " + new Date());

            CaseService service = getService(args);
            service.recordJobMessage(getNonEmptyMsgs(msgs), keys);
            System.out.println("EMF command client sent " + msgs.length + " messages successfully.");

            CaseOutput[] nonEmptyOutputs = getNonEmptyOutputs(outputs);

            if (nonEmptyOutputs.length > 1) {
                service.registerOutputs(nonEmptyOutputs, keys);
                System.out.println("EMF command client registered " + outputs.length + " outputs successfully.");
            }

            if (nonEmptyOutputs.length == 1) {
                service.registerOutput(nonEmptyOutputs[0], keys[0]);
                System.out.println("EMF command client registered one output successfully.");
            }

            if (DEBUG)
                System.out.println("EMF Command Client exits successfully at: " + new Date());
        } catch (Exception e) {
            System.out.println("EMF Command Client encounters problem at: " + new Date() + "\nThe error was: "
                    + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private synchronized static JobMessage[] getNonEmptyMsgs(JobMessage[] msgs) {
        List<JobMessage> all = new ArrayList<JobMessage>();

        for (JobMessage msg : msgs)
            if (!msg.isEmpty())
                all.add(msg);

        return all.toArray(new JobMessage[0]);
    }

    private synchronized static CaseOutput[] getNonEmptyOutputs(CaseOutput[] outputs) {
        List<CaseOutput> all = new ArrayList<CaseOutput>();

        for (CaseOutput output : outputs)
            if (!output.isEmpty())
                all.add(output);

        return all.toArray(new CaseOutput[0]);
    }

    private synchronized static void writeToLogger(List<String> args, String logFile, int logInterval, int resendTimes,
            String jobkey, JobMessage jobMsg, CaseOutput output) throws Exception {
        boolean logFileExisted = new File(logFile).exists();
        String logAsistFile = logFile + ".ast";

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
        writeLogs(writer, logAsistFile, jobMsg, jobkey, output, logFileExisted);
        writer.close();

        if (jobMsg.getStatus() == null || jobMsg.getStatus().isEmpty()) {
            sendLogs(args, logInterval, resendTimes, logFile, logAsistFile, jobkey, jobMsg, output, false);
        } else
            sendLogs(args, logInterval, resendTimes, logFile, logAsistFile, jobkey, jobMsg, output, true);
    }

    private synchronized static void writeLogs(PrintWriter writer, String logAsistFile, JobMessage jobMsg,
            String jobkey, CaseOutput output, boolean logFileExisted) throws Exception {
        String record = jobkey;
        record += "," + jobMsg.getExecName();
        record += "," + jobMsg.getExecPath();
        record += "," + jobMsg.getMessage();
        record += "," + jobMsg.getMessageType();
        record += "," + jobMsg.getStatus();
        record += "," + jobMsg.getPeriod();
        record += "," + jobMsg.getRemoteUser();
        record += "," + EmfDateFormat.format_MM_DD_YYYY_HH_mm(jobMsg.getExecModifiedDate());
        record += "," + new Date().getTime();
        record += "," + output.getDatasetFile();
        record += "," + output.getPath();
        record += "," + output.getPattern();
        record += "," + output.getDatasetType();
        record += "," + output.getDatasetName();
        record += "," + output.getName();

        if (!logFileExisted) {
            writeInitialAsistFile(new File(logAsistFile));

            writer.println("job key,exec name,exec path,message,"
                    + "message type,status,period,user,last mod date,log time(ms),"
                    + "output file,output dir,pattern,dataset type,dataset name,output name");
        }

        writer.println(record);
    }

    private synchronized static void writeInitialAsistFile(File file) throws Exception {
        if (!file.exists()) {
            PrintWriter asistWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            asistWriter.println("2"); // line number to start extract job messages and send
            asistWriter.close();
        }

        return;
    }

    private synchronized static CaseService getService(List<String> args) throws Exception {
        String url = DEFAULT_URL;

        if (!args.get(0).startsWith("-"))
            url = args.get(0);

        if (serviceLocator == null)
            serviceLocator = new RemoteServiceLocator(url);

        return serviceLocator.caseService();
    }

    private synchronized static long getTime(String line) {
        int timeIndex = line.lastIndexOf(",");
        String time = line.substring(timeIndex + 1);

        if (time != null || !time.trim().isEmpty())
            return Long.parseLong(time.trim());

        return 0;
    }

    private synchronized static String extractJobkey(String line) {
        return line.substring(0, line.indexOf(","));
    }

    private synchronized static JobMessage extractJobMsg(String line) throws Exception {
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
        
        if (msg.getStatus().isEmpty() && msg.getMessage().isEmpty())
            msg.setEmpty(true);

        try {
            msg.setExecModifiedDate((fields[8] == null || fields[8].trim().isEmpty()) ? null : EmfDateFormat
                    .parse_MM_DD_YYYY_HH_mm(fields[8]));
            msg.setReceivedTime(new Date(Long.parseLong(fields[9])));
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }

        return msg;
    }

    private synchronized static CaseOutput extractOutput(String line) throws Exception {
        CommaDelimitedTokenizer tokenizer = new CommaDelimitedTokenizer();
        String[] fields = tokenizer.tokens(line);
        boolean empty = true;
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                empty = false;
                break;
            }
        }

        CaseOutput output = new CaseOutput();
        output.setDatasetFile(fields[10]);
        output.setPath(fields[11]);
        output.setPattern(fields[12]);
        output.setDatasetType(fields[13]);
        output.setDatasetName(fields[14]);
        output.setName(fields[15]);
        output.setEmpty(empty);

        return output;
    }

    private synchronized static String createSafeName(String name) {
        if (name == null)
            return name;

        String safeName = name.trim();

        for (int i = 0; i < safeName.length(); i++) {
            if (!Character.isLetterOrDigit(safeName.charAt(i))) {
                safeName = safeName.replace(safeName.charAt(i), '_');
            }
        }

        return safeName;
    }

}
