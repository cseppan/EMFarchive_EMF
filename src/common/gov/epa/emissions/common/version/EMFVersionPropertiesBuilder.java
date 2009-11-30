package gov.epa.emissions.common.version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class EMFVersionPropertiesBuilder {

    public static void main(String[] args) throws IOException {

        Date date = new Date(System.currentTimeMillis());
        File versionGeneratedFile = new File(System.getProperty("user.dir") + "/res/properties/version_generated.properties");
        System.out.println("Building version file:" + versionGeneratedFile.getAbsolutePath() + " with date: " + date.toString()
                + "...");

        PropertiesManager propertiesManager = PropertiesManager.getInstance();
        propertiesManager.initProperties(System.getProperty("user.dir") + "/res/properties/version.properties");
        
        FileWriter fileWriter = new FileWriter(versionGeneratedFile);

        fileWriter.write("#WARNING: This is an automatically generated file. Any manual changes to it will be lost during build.");
        fileWriter.write("\n");
        fileWriter.write("\n");
        fileWriter.write("#build.version.timestamp in readable form: ");
        fileWriter.write(date.toString());
        fileWriter.write("\n");
        fileWriter.write("build.version.timestamp=");
        fileWriter.write(Long.toString(date.getTime()));
        fileWriter.write("\n");

        Set<String> keys = propertiesManager.getKeys();
        for (String key : keys) {
            
            fileWriter.write(key);
            fileWriter.write("=");
            
            int version = propertiesManager.getValueAsInt(key);
            fileWriter.write(Integer.toString(version));
            fileWriter.write("\n");
        }

        fileWriter.flush();
        fileWriter.close();
    }
}
