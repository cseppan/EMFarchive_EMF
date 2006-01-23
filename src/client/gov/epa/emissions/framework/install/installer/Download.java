package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download extends Thread{
	private String urlbase;
	private String installhome;
    private String filelist;
    private InstallPresenter presenter;
    private volatile Thread  blinker;
    private int numFiles2Download;

	
	public Download(String url, String filelist, String installhome){
        this.urlbase = url;
        this.installhome = installhome;
        this.filelist = filelist;
        this.blinker = new Thread(this);
    }	
    
    public void start() {
        blinker.start();
    }
    
    public void stopDownload() {
        presenter.setCursor(Cursor.getDefaultCursor());
        blinker = null;
    }

	public void run(){
        Thread thisThread = Thread.currentThread();
        presenter.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            downloadFileList();
            File2Download[] todownload = getFiles2Download();
            
            for(int x=0; x<numFiles2Download; x++){
                if(blinker == thisThread) {
                    String temp = todownload[x].getPath();
                    File file2save = getSingleDownloadFile(temp);
                    HttpURLConnection conn = getConnection(temp);
                    writeStatus(x, temp);
                    saveFile(file2save, conn);
                }
            }
            
            if(blinker == thisThread)
                presenter.setFinish();
        } catch (IOException e){
            presenter.displayErr("Downloading files failed.");
        } finally {
            stopDownload();
        }
	}
	
	public void createShortcut(){
        File bat = new File(installhome, "shortcut.bat");
        File inf = new File(installhome, "shortcut.inf");
        createShortcutBatchFile(bat, inf);
		
		try {

            String[] cmd = getCommands();
            
            /*
             * Only creates a shortcut on Windows start menu.
             */
			if(System.getProperty("os.name").indexOf("Windows") >= 0){
				Process p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
			}
			
			bat.delete();
			inf.delete();
		} catch (IOException e) {
			presenter.displayErr("Creating shortcut failed.");
		} catch (InterruptedException e) {
		    presenter.displayErr("Windows runtime error while creating EMF client shortcut.");
        }
	}
    
    private void createShortcutBatchFile(File bat, File inf){
        String separator = Generic.SEPARATOR;
        
        String battext = "\n@echo off & setlocal" + separator +
                         "\nset inf=rundll32 setupapi,InstallHinfSection DefaultInstall" + separator +
                         "\nstart/w %inf% 132 " + installhome.replace('\\', '/') + "/shortcut.inf" + separator +
                         "\nendlocal" + separator;
                         
        String inftext = "[version]" + separator +
                         "signature=$chicago$" + separator +
                         "[DefaultInstall]" + separator +
                         "UpdateInis=Addlink" + separator +
                         "[Addlink]" + separator +
                         "setup.ini, progman.groups,, \"\"group200=\"EMF\"\"\"" + separator +
                         "setup.ini, group200,, \"\"\"EMF Client\"\",\"\"\"\"\"\"" + installhome.replace('\\', '/') + 
                         "/" + Generic.EMF_BATCH_FILE + "\"\"\"\"\"\"" + separator;
        
        try{
            FileWriter fw1 = new FileWriter(bat);
            FileWriter fw2 = new FileWriter(inf);
            fw1.write(battext);
            fw2.write(inftext);
            fw1.close();
            fw2.close();
        }catch(IOException e){
            presenter.displayErr("Creating shortcut files failed.");
        }
    }
    
    private String[] getCommands() {
        String[] cmd = new String[3];
        String os = System.getProperty("os.name");
        
        if(os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")){
            cmd[0] = "command.com" ;
        } else {
            cmd[0] = "cmd.exe" ;
        }
        
        cmd[1] = "/C" ;
        cmd[2] = installhome.replace('\\', '/') + "/shortcut.bat";
        
        return cmd;
    }
    
    public void downloadFileList() throws IOException {
        File dir = new File(installhome);
        dir.mkdirs();
        File download = new File(dir, filelist);
        
        HttpURLConnection conn = getConnection(filelist);
        String s = "";
        String out = "";
        
        InputStream is = conn.getInputStream();
        BufferedReader content = new  BufferedReader(new InputStreamReader(is));
        FileWriter fw = new FileWriter(download);
        while((s = content.readLine()) != null){
            out += s + Generic.SEPARATOR;   
        }
        is.close();
        fw.write(out);
        fw.close();
    }
    
    private File2Download[] getFiles2Download() {
        File list = new File(installhome, Generic.FILE_LIST);
        if(list.exists()){
            TextParser parser = new TextParser(list, ";");
            parser.parse();
            numFiles2Download = parser.getNumDownloadFiles();
            return parser.getDownloadFiles();
        }
        
        return null;
    }
    
    private File getSingleDownloadFile(String name) {
        if(name.endsWith("/")){
            File f = new File(installhome, name);
            f.mkdirs();
        }else{
            int index = name.lastIndexOf("/");

            //Get the subdir
            String sub = name.substring(0, index);
            File subdir = new File(installhome, sub);
            subdir.mkdirs();
        }
            
        return new File(installhome, name);
    }
    
    private HttpURLConnection getConnection(String name) throws IOException {
        URL url = new URL(urlbase + "/" + name);        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.connect();
        
        return conn;
    }
    
    private void writeStatus(int n, String name) {
        String status = "Status: Downloading " + (n+1) + " out of " + numFiles2Download
            + " files:   " + name;
        presenter.setStatus(status);
    }
    
    private void saveFile(File file, HttpURLConnection conn) throws IOException {
        if(!file.isDirectory()){
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);
            
            int res = 0;    
            while ((res = bis.read()) != -1) {
                fos.write(res);
            }
            
            is.close(); 
            fos.close();
        }
    }
    
    public void addObserver(InstallPresenter presenter) {
        this.presenter = presenter;
    }
    
}
