package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;

import java.io.File;

public class CMImportPresenter {

    private CMImportView view;

    private EmfSession session;

    private CMImportInputRules importRules;

    public CMImportPresenter(EmfSession session) {
        this.session = session;
        this.importRules = new CMImportInputRules();
    }

    public void doImport(int [] sectorIDs, String directory, String[] files) throws EmfException {
        
        if ( sectorIDs != null) { // need to purge control measure by sectors
            
            // TODO: JIZHEN do validation here
            
            if ( sectorIDs.length == 0) { // TODO: JIZHEN purge all
                System.out.println("Purge all");
                
            } else { // TODO: JIZHEN purge by sectors
                System.out.println("Purge by sectors: ");
                for ( int i=0; i<sectorIDs.length; i++) {
                    System.out.print(sectorIDs[i] + " ");
                }
                System.out.println("");
            }
        }
        
        //return;
        
        importControlMeasures(directory, files);
    }

    void importControlMeasures(String directory, String[] files) throws EmfException {
        
        //Maybe validate if user is a CoST SU, if truncate is true
        importRules.validate(directory, files);
        
        
        startImportMessage(view);
//        importing = true;
//        session.controlMeasureImportService().importControlMeasures(mapToRemote(directory), files, session.user());
        session.controlMeasureImportService().importControlMeasures(directory, files, session.user());
    }

    private void startImportMessage(CMImportView view) {
        String message = "Started Import. Please use 'Status Window' and 'Import Status' button in the import window to track";
        view.setMessage(message);
    }

    public void doDone() {
//        if (importing) {
//            String message = "Control measures are being imported, you will lose status messages by closing this window." + System.getProperty("line.separator")
//            + " Click the Import Status button to ge the status of the import porcess.  Click no, if you to don't care to see the messages for the import process.";
//            String title = "Ignore import status messages?";
//            YesNoDialog dialog = new YesNoDialog(view, title, message);
//            if (dialog.confirm()) {
//                view.disposeView();
//            }
//        }
        view.disposeView();
    }

    public void display(CMImportView view) {
        try {
            removeImportStatuses();
        } catch (EmfException e) {
            //
        }
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
    }

    private String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();
        if (folder == null) folder = "";
        else if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    // TODO: move the getFileNamesFromPattern () to a common service
    public String[] getFilesFromPatten(String folder, String pattern) throws EmfException {
        return session.eximService().getFilenamesFromPattern(folder, pattern);
    }

    public Status[] getImportStatus() throws EmfException {
        return session.controlMeasureImportService().getImportStatus(session.user());
    }

    public void removeImportStatuses() throws EmfException {
        session.controlMeasureImportService().removeImportStatuses(session.user());
    }
    
    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        return session.controlMeasureService().getDistinctControlMeasureSectors();
    }

    public String getCoSTSUs() throws EmfException {
        return session.controlStrategyService().getCoSTSUs();
    }
}
