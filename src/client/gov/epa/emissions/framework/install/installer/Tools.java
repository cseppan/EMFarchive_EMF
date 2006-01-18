package gov.epa.emissions.framework.install.installer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Tools {
	
	public static String resetWinSeprator(String s){
		int i = s.length();
		char[] array = s.toCharArray();
		for(int j = 0; j < i; j++){
			if(array[j] == '/'){
				array[j] = '\\';
			}
		}	
		return String.valueOf(array);
	}
	
	public static boolean removeDir(File dir){
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = removeDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	public static String getCurrentDate (String format) {
	    Date today = new Date();
	    SimpleDateFormat formatter = new SimpleDateFormat(format);
	    String datenewformat = formatter.format(today);
	    return  datenewformat;
	}
	
	public static void updateFileModTime(String trimhome, File2Download[] files){
		for(int i = 0; i < files.length; i++){
			String fullpath = trimhome + "/" + files[i].getPath();
			File file = new File(fullpath);
			
			StringTokenizer st = new StringTokenizer(files[i].getDate(), " ");
			String time1 = st.nextToken().trim();
			String time2 = st.nextToken().trim();
			
			SimpleDateFormat sdf = new SimpleDateFormat(Generic.TIME_FORMAT);
			try{
				Date date = sdf.parse(time1 + " " + time2);
				file.setLastModified(date.getTime());
			}catch(ParseException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
    public static void writePreference(String website, String input, String output, String javahome){
        File prefdir = new File(System.getProperty("user.home"));
        File pref = new File(prefdir, Generic.USER_PARAMETER);
        String separator = Generic.SEPARATOR;
        
        String towrite = "#EMF Client Installer - Preferences" + separator
                         + "#comments '#'" + separator 
                         + "#preference specified by key,value pair separted by '='" + separator 
                         + "#case sensitive" + separator 
                         + "#white spaces and line terminators can be escaped by '\'" + separator 
                         + "#If the value aren't specified then default value will be empty string" + separator 
                         + "#Use '/' for path separator for file names"+ separator + separator
                         + "web.site=" + website + separator 
                         + "EMFInputDriveLetter=" + input.charAt(0) + separator
                         + "EMFOutputDriveLetter=" + output.charAt(0) + separator
                         + "EMFInputServerDirectory=/data" + separator
                         + "EMFOutputServerDirectory=/data" + separator
                         + "EMFDefaultInputDirectory=emf_input" + separator
                         + "EMFDefaultOutputDirectory=emf_output" + separator
                         + "java.home=" + javahome.replace('\\', '/') + separator;
        
        try{
            FileWriter fw = new FileWriter(pref);
            fw.write(towrite);
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
        }   
    }

}
