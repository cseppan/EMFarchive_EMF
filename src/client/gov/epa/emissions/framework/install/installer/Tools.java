package gov.epa.emissions.framework.install.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Tools {

    public static String resetWinSeprator(String s) {
        int i = s.length();
        char[] array = s.toCharArray();
        for (int j = 0; j < i; j++) {
            if (array[j] == '/') {
                array[j] = '\\';
            }
        }
        return String.valueOf(array);
    }

    public static boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = removeDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String getCurrentDate(String format) {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String datenewformat = formatter.format(today);
        return datenewformat;
    }

    public static void updateFileModTime(String installhome, File2Download[] files) throws Exception {
        for (int i = 0; i < files.length; i++) {
            String fullpath = installhome + "/" + files[i].getPath();
            File file = new File(fullpath);

            StringTokenizer st = new StringTokenizer(files[i].getDate(), " ");
            String time1 = st.nextToken().trim();
            String time2 = st.nextToken().trim();

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT);
            Date date = sdf.parse(time1 + " " + time2);
            file.setLastModified(date.getTime());
        }
    }

    public static void writePreference(String website, String input, String output, String javahome, String emfhome,
            String server) throws Exception {
        String separator = Constants.SEPARATOR;
        
        String analysisEnginePrefString = separator + separator 
                + "#Analysis Engine Preferences" + separator 
                + "format.double.decimal_places=2"
                + separator + "format.double.option=Standard_Notation" + separator
                + "#legal options: Standard_Notation,Scientific_Notation, Dollars, Percentage, Custom" + separator
                + "format.double.significant_digits=4";

        String emfPrefString = "local.input.drive=" + input.charAt(0) + ":/" + separator + "local.output.drive="
                + output.charAt(0) + ":/" + separator + "remote.input.drive=/data/" + separator
                + "remote.output.drive=/data/" + separator + "default.input.folder="
                + input.substring(3).replace('\\', '/') + separator + "default.output.folder="
                + output.substring(3).replace('\\', '/') + separator + "server.address=" + server + separator;

        String towrite = "#EMF Client Installer - Preferences" + separator + "#comments '#'" + separator
                + "#preference specified by key,value pair separted by '='" + separator + "#case sensitive" + separator
                + "#white spaces and line terminators can be escaped by '\'" + separator
                + "#If the value aren't specified then default value will be empty string" + separator
                + "#Use '/' for path separator for file names" + separator + separator + "web.site=" + website
                + separator + "emf.install.folder=" + emfhome.replace('\\', '/') + separator + emfPrefString
                + "java.home=" + javahome.replace('\\', '/') + separator;

        PrintWriter userPrefWriter = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.home")
                + "\\" + Constants.INSTALLER_PREFERENCES_FILE)));
        PrintWriter emfPrefWriter = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.home")
                + "\\" + Constants.EMF_CLIENT_PREFERENCES_FILE)));
        userPrefWriter.write(towrite);
        emfPrefWriter.write(emfPrefString + analysisEnginePrefString);
        userPrefWriter.close();
        emfPrefWriter.close();
    }

}
