package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;

public class InstallPresenter {
    private InstallView view;
    
    private Download model;

//    public InstallPresenter() {
//        //TODO: take in a parameter
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
    }
    
    public void writePreference(String website, String input, String output, String javahome) {
        Tools.writePreference(website, input, output, javahome);
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
    
}
