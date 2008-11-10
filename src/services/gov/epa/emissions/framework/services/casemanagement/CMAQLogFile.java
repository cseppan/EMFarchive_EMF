package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.framework.services.EmfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMAQLogFile implements EMFCaseFile {

    private String path;

    private File file;

    private BufferedReader fileReader;

    private CustomCharSetInputStreamReader inputStreamReader;

    private Map<String, String> data = new HashMap<String, String>();
    
    private StringBuffer sb = null;
    
    private static final String lineSep = System.getProperty("line.separator");

    public CMAQLogFile(String path) {
        this.path = path;
        this.file = new File(path);
    }
    
    public CMAQLogFile(File file) {
        this.path = file.getAbsolutePath();
        this.file = file;
    }

    private void open() throws UnsupportedEncodingException, FileNotFoundException {
        inputStreamReader = new CustomCharSetInputStreamReader(new FileInputStream(file));
        fileReader = new BufferedReader(inputStreamReader);
    }

    private void close() throws IOException {
        fileReader.close();
    }

    public String getAttributeValue(String attribute) {
        return data.get(attribute);
    }

    /***
     * Read all the attributes and their values and put them in a hash map
     */
    public void readAll() throws EmfException {
        String tempAttr = "";
        sb = new StringBuffer();
        
        if (!data.isEmpty())
            data.clear();
        
        try {
            open();
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                int eqIndex = line.indexOf("=");

                if (eqIndex < 0)
                    continue;

                String attrib = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();

                if (attrib.equals(tempAttr)) {
                    sb.append("WARNING: " + tempAttr + "--duplicate value: " + value + lineSep);
                    continue;
                }

                addAttribValue(attrib, value, data);
                tempAttr = attrib;
            }

            close();
        } catch (UnsupportedEncodingException e) {
            throw new EmfException("File " + path + " is not consistent with character encoding: "
                    + inputStreamReader.getEncoding() + ".");
        } catch (FileNotFoundException e) {
            throw new EmfException("File " + path + " doesn't exist.");
        } catch (IOException e) {
            throw new EmfException("Cannot read file " + path + ".");
        }
    }

    /***
     * Read specified attributes (parameters) only
     */
    public void read(List<String> attributes) throws EmfException {
        if (attributes == null || attributes.size() == 0)
            throw new EmfException("No attributes specified to read from log file.");
        
        String tempAttr = "";
        sb = new StringBuffer();
        
        if (!data.isEmpty())
            data.clear();
        
        try {
            open();
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                int eqIndex = line.indexOf("=");

                if (eqIndex < 0)
                    continue;

                String attrib = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();
                
                if (!attributes.contains(attrib))
                    continue;

                if (attrib.equals(tempAttr)) {
                    sb.append("WARNING: " + tempAttr + "--duplicate value: " + value + lineSep);
                    continue;
                }

                addAttribValue(attrib, value, data);
                tempAttr = attrib;
            }

            close();
        } catch (UnsupportedEncodingException e) {
            throw new EmfException("File " + path + " is not consistent with character encoding: "
                    + inputStreamReader.getEncoding() + ".");
        } catch (FileNotFoundException e) {
            throw new EmfException("File " + path + " doesn't exist.");
        } catch (IOException e) {
            throw new EmfException("Cannot read file " + path + ".");
        }
    }
    
    private void addAttribValue(String key, String value, Map<String, String> map) {
        if (key == null || key.isEmpty())
            return;

        map.put(key, value);
    }

    public String getMessages() {
        return (sb != null) ? sb.toString() : null;
    }

}
