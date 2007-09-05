package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EMFCmdClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private static CaseService caseService;

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

        if (!options.contains("-k") || !options.contains("-t")) {
            System.out.println("Please specify required options '-k' and '-t'.");
            displayHelp();
            return;
        }

        int keyIndex = options.indexOf("-k");
        int typeIndex = options.indexOf("-t");
        String keyString = options.get(++keyIndex);
        String typeString = options.get(++typeIndex);

        if (keyString.startsWith("-")) {
            System.out.println("Please specify a correct jobkey.");
            return;
        }

        if (!typeString.equalsIgnoreCase("i") && !typeString.equalsIgnoreCase("e") && !typeString.equalsIgnoreCase("w")) {
            System.out.println("Please specify a correct message type - i (info), e (error), w (warning).");
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
        String jobkey = (keyIndex < 0) ? "" : args.get(++keyIndex);
        String execPath = (execIndex < 0) ? "" : args.get(++execIndex);
        String period = (periodIndex < 0) ? "" : args.get(++periodIndex);
        String message = (msgIndex < 0) ? "" : args.get(++msgIndex);
        String status = (statusIndex < 0) ? "" : args.get(++statusIndex);
        String type = (typeIndex < 0) ? "" : args.get(++typeIndex);

        if (execPath.startsWith("-") || period.startsWith("-") || message.startsWith("-") || status.startsWith("-"))
            throw new Exception("Please specify valid values for options.");

        File execFile = new File(execPath);

        JobMessage jobMsg = new JobMessage();
        jobMsg.setExecName(execPath.isEmpty() ? "" : execPath.substring(execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setExecPath(execPath.isEmpty() ? "" : execPath.substring(0, execPath.lastIndexOf(File.separator) + 1));
        jobMsg.setMessage(message);
        jobMsg.setMessageType(type);
        jobMsg.setStatus(status);
        jobMsg.setPeriod(period);
        jobMsg.setRemoteUser(System.getProperty("user.name"));
        jobMsg.setExecModifiedDate(execFile.exists() ? new Date(execFile.lastModified()) : null);

        try {
            String url = DEFAULT_URL;

            if (!args.get(0).startsWith("-"))
                url = args.get(0);

            System.out.println("Starting EMF Command Client");
            ServiceLocator serviceLocator = new RemoteServiceLocator(url);
            caseService = serviceLocator.caseService();
            caseService.recordJobMessage(jobMsg, jobkey);
            System.out.println("Exiting EMF Command Client");
        } catch (Exception exc) {
            System.out.println("Exception starting client: " + exc.getMessage());
            throw exc;
        }
    }

}
