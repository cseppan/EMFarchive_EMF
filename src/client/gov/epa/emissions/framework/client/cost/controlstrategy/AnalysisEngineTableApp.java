package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.FileImportGUI;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.TablePanel;
import gov.epa.mims.analysisengine.table.TextDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTabbedPane;

public class AnalysisEngineTableApp extends DisposableInteralFrame {

    private JTabbedPane mainTabbedPane;

    private EmfConsole parentConsole;

    private static int counter=0;

    public AnalysisEngineTableApp(String controlStrategyName, DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Analyze Control Strategy: "+controlStrategyName+counter++, desktopManager);
        this.parentConsole = parentConsole;
    }

    public void display(String[] fileNames) {
        setLayout(fileNames);
        super.display();
    }

    private void setLayout(String[] fileNames) {
        mainTabbedPane = new JTabbedPane();
        String[] tabNames = createTabNames(1, 40, fileNames);
        importFiles(fileNames, tabNames, FileImportGUI.GENERIC_FILE, ",", 1);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainTabbedPane);
        pack();

    }

    private String[] createTabNames(int startIndex, int endIndex, String[] fileNames) {
        String[] tabNames = null;
        if (fileNames != null) {
            // switch the indices
            if (startIndex > endIndex) {
                int tempStartIndex = startIndex;
                startIndex = endIndex;
                endIndex = tempStartIndex;
            }// if

            startIndex--; // deducting 1 since user values starts from 1
            // but index starts from 0;
            tabNames = new String[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                File file = new File(fileNames[i]);
                String fileNameWithExt = file.getName();
                int wholeLength = fileNameWithExt.length();
                String fileNameWOExt = fileNameWithExt.substring(0, wholeLength - 4);
                int correctedEndIndex = -1;

                if (startIndex > fileNameWOExt.length()) {
                    startIndex = FileImportGUI.START_TAB_NAME_INDEX - 1; // set to 0
                }// if

                if (endIndex > fileNameWOExt.length()) {
                    correctedEndIndex = fileNameWOExt.length();
                } // if
                else {
                    correctedEndIndex = endIndex;
                }// else
                String tabName = fileNameWOExt.substring(startIndex, correctedEndIndex);
                tabNames[i] = tabName;
            }// for(i)
        }// if(selectedFiles != null)
        return tabNames;
    }// createTabNames()

    private void importFiles(String[] fileNames, String[] tabNames, String fileType, String delimiter,
            int noOfColumnNameRows) {
        boolean allSuccessful = true; // a flag to check whether all the file imported sucessfuly
        ArrayList importFileStatus = new ArrayList();
        ArrayList warningWindow = new ArrayList();

        for (int i = 0; i < fileNames.length; i++) {
            SpecialTableModel model = null;
            try {
                model = FileImportGUI.createAModel(fileNames[i], fileType, delimiter, noOfColumnNameRows);
                if (FileImportGUI.getLogMessages() != null) {
                    warningWindow.add(FileImportGUI.getLogMessages());
                }
                insertIntoTabbedPane(model, fileNames[i], tabNames[i], fileType);
                importFileStatus.add("Success: " + fileNames[i]);
            } // try
            catch (Exception e) {
                allSuccessful = false;
                if (e.getMessage() == null) {
                    importFileStatus.add("FAILURE: " + fileNames[i] + "\n");
                    importFileStatus.add(get50Lines(fileNames[i]));
                } else {
                    importFileStatus.add("FAILURE: " + fileNames[i] + ":\n" + e.getMessage());
                    importFileStatus.add(get50Lines(fileNames[i]));
                }
                System.out.println("Error reading the file " + e.getMessage());
            }// catch
        }
        importStatus(allSuccessful, importFileStatus, warningWindow);
    }

    private void importStatus(boolean allSuccessful, ArrayList importFileStatus, ArrayList warningWindow) {
        try {
            if (warningWindow.size() != 0) {
                TextDialog dialog = new TextDialog(this, "WARNING", "", false);
                dialog.setTextFromList("Warnings from the Table Loader", warningWindow);
                dialog.setModal(true);
                dialog.setVisible(true);
                warningWindow.clear();
            }
            if (!allSuccessful) // ie don't show the dialog if all the files are imported sucessfuly.
            {
                TextDialog dialog = new TextDialog(this, "Import File Status", "", false);
                dialog.setTextFromList("The status of the import process is as follows:", importFileStatus);

                dialog.setSize(800, 400);

                dialog.setModal(true);
                dialog.setVisible(true);
            }// if(!allSuccessful)
        } // try
        catch (Exception e) {
            DefaultUserInteractor.get().notifyOfException(this, "Failed to show import status dialog", e,
                    UserInteractor.ERROR);
        }// catch
    }

    public void insertIntoTabbedPane(SpecialTableModel model, String fileName, String tabName, String fileType) {
        if (model == null)
            return;

        if (fileName == null)
            fileName = tabName;

        TablePanel panel = new TablePanel(parentConsole, model, fileName, tabName, fileType, mainTabbedPane);

        mainTabbedPane.addTab(tabName, null, panel, fileName);
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
    }

    public static String get50Lines(String fileName) {
        StringBuffer lines = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str = new String("The first 50 Lines of the file " + fileName);
            lines.append(str);
            lines.append("\n");
            for (int i = 0; i < str.length(); i++)
                lines.append("=");
            lines.append("\n\n");
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 50) {
                lines.append(line);
                lines.append("\n");
                count++;
            }
            reader.close();
        } catch (IOException ie) {
            lines.append("Error reading file " + ie.getMessage());
        }
        return lines.toString();
    }

}
