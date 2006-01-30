package gov.epa.emissions.framework.install.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class CheckUpdatedFiles {
        private File2Download[] oldFiles, newFiles;
        
        private File storedInfoFile;
        
        private InstallPresenter presenter;
        
        private Download delegate;
        
        public void initialize(String installhome, Download delegate) {
            this.delegate = delegate;
            storedInfoFile = new File(installhome, Constants.UPDATE_FILE);
            getOldFilesInfo();
            getFile2Download();
        }
        
        public void download() {
            delegate.setFile2Download(newFiles);
            delegate.start();
        }
        
        public String[] getNewFilesName() {
            List list = new ArrayList();
            for(int i = 0; i < newFiles.length; i++)
                list.add(newFiles[i].getPath());
            
            return (String[])list.toArray(new String[0]);
        }
        
        private void getFile2Download() {
            File2Download[] curFiles = delegate.getFiles2Download();
            List list = new ArrayList();
            
            for(int i=0; i<curFiles.length; i++){
                innerloop:
                for(int j=0; j<oldFiles.length; j++){
                    if(curFiles[i].getPath().equalsIgnoreCase(oldFiles[j].getPath())){
                        if(compareTimeStamps(curFiles[i].getDate(), oldFiles[j].getDate())){
                            list.add(curFiles[i]);
                        }
                        break innerloop;    
                    }
                }
            }
                
            newFiles = (File2Download[])list.toArray(new File2Download[0]);
        }
        
        private void getOldFilesInfo() {
            try{
                FileInputStream fis = new FileInputStream(storedInfoFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                oldFiles = (File2Download[])ois.readObject();
                ois.close();
            }catch(Exception e){
                setErrMsg("Fetching stored files info failed.");
            }
        }
        
        private boolean compareTimeStamps(String date1, String date2){
            boolean ret = false;
            StringTokenizer st1 = new StringTokenizer(date1, " ");
            StringTokenizer st2 = new StringTokenizer(date2, " ");
            String time11 = st1.nextToken().trim();
            String time12 = st1.nextToken().trim();
            String time21 = st2.nextToken().trim();
            String time22 = st2.nextToken().trim();
            
            Date dt1 = new Date();
            Date dt2 = new Date();
            SimpleDateFormat df = new SimpleDateFormat(Constants.TIME_FORMAT);
            try{
                dt1 = df.parse(time11 + " " + time12);
                dt2 = df.parse(time21 + " " + time22);
            }catch(ParseException e){
                e.printStackTrace();
            }
            
            if(dt1.after(dt2)){
                ret = true;
            }
            
            return ret; 
        }
        
        private void setErrMsg(String msg) {
            presenter.displayErr(msg);
        }
        
        public void addObserver(InstallPresenter presenter) {
            this.presenter = presenter;
        }

}
