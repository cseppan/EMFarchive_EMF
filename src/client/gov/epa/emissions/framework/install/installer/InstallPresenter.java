package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;

public class InstallPresenter {
    private InstallView view;
    
    private Download model;

//    public InstallPresenter(Download model) {
//        this.model = model;
//    }

    public void doCancel() {
        view.close();
    }

    public void display(InstallView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }
    
    public void startDownload(String url, String filelist, String installhome) {
        model = new Download(url, filelist, installhome);
        model.addObserver(this);
        model.start();
    }
    
    public void stopDownload() {
        model.stopDownload();
        //model.stop();
    }
    
    public void writePreference(String website, String input, 
            String output, String javahome, String emfhome) {
        Tools.writePreference(website, input, output, javahome, emfhome);
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
            String javahome) {
        try {
            new ClientBatchFile(filename).create(preference, javahome);
        } catch (Exception e) {
            view.displayErr("Creating EMF client batch file failed.");
        }
    }
    
    public void createShortcut() {
        model.createShortcut();
    }
    
}
