package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;

public class InstallPresenter {
    private InstallView view;
    
    private Download model;
    
    private CheckUpdatedFiles checkUpdates;

    public InstallPresenter() {
        this.model = new Download();
        model.addObserver(this);
        this.checkUpdates = new CheckUpdatedFiles();
        checkUpdates.addObserver(this);
    }

    public void doCancel() {
        view.close();
    }

    public void display(InstallView view) {
        this.view = view;
        this.view.observe(this);

        this.view.display();
    }
    
    public void startDownload() {
        model.start();
    }
    
    public void initModels(String url, String filelist, String installhome) {
        model.initialize(url, filelist, installhome);
        checkUpdates.initialize(installhome, model);
    }
    
    public String[] checkUpdates() {
        return checkUpdates.getNewFilesName();
    }
    
    public void downloadUpdates() {
        checkUpdates.download();
    }
    
    public void stopDownload() {
        model.stopDownload();
    }
    
    public void writePreference(String website, String input, 
            String output, String javahome, String emfhome, String server) {
        try {
            Tools.writePreference(website, input, output, javahome, emfhome, server);
        } catch (Exception e) {
            view.displayErr("Creating EMF client preference file failed.");
        }
    }
    
    public void setStatus(String status) {
        view.setStatus(status);
    }
    
    public void setCursor(Cursor cursor) {
        view.setCursor(cursor);
    }
    
    public void displayErr(String err) {
        view.displayErr(err);
    }
    
    public void setFinish() {
        view.setFinish();
    }
    
    public void createBatchFile(String filename, String preference, 
            String javahome, String server) {
        try {
            new ClientBatchFile(filename).create(preference, javahome, server);
        } catch (Exception e) {
            view.displayErr("Creating EMF client batch file failed.");
        }
    }
    
    public void createShortcut() {
        model.createShortcut();
    }
    
}
