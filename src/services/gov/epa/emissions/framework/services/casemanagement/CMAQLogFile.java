package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.framework.services.EmfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMAQLogFile implements EMFCaseFile {

    private String path;

    private File file;

    private BufferedReader fileReader;

    private CustomCharSetInputStreamReader inputStreamReader;

    private List<String> uniqAttributes = new ArrayList<String>();

    private Map<String, List<String>> data = new HashMap<String, List<String>>();

    public CMAQLogFile(String path) {
        this.path = path;
        this.file = new File(path);
    }

    private void open() throws UnsupportedEncodingException, FileNotFoundException {
        inputStreamReader = new CustomCharSetInputStreamReader(new FileInputStream(file));
        fileReader = new BufferedReader(inputStreamReader);
    }

    private void close() throws IOException {
        fileReader.close();
    }

    public String[] getAttributeValues(String attribute) {
        return data.get(attribute).toArray(new String[0]);
    }

    public String[] getAttributes() {
        return uniqAttributes.toArray(new String[0]);
    }

    /***
     * Read all the attributes and their values and put them in a hash map
     */
    public void readAll() throws EmfException {
        String tempAttr = "";
        List<String> tempValues = new ArrayList<String>();

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
                    tempValues.add(value);
                    continue;
                }

                addAttribValue(tempAttr, tempValues, data);
                
                if (tempAttr.isEmpty())
                    uniqAttributes.add(tempAttr);
                
                tempAttr = attrib;
                tempValues = new ArrayList<String>();
                tempValues.add(value);
                
                
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
    public void read(List<String> parameters) throws EmfException {
        String tempAttr = "";
        List<String> tempValues = new ArrayList<String>();

        try {
            open();
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                int eqIndex = line.indexOf("=");

                if (eqIndex < 0)
                    continue;

                String attrib = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();
                
                if (!parameters.contains(attrib))
                    continue;

                if (attrib.equals(tempAttr)) {
                    tempValues.add(value);
                    continue;
                }

                addAttribValue(tempAttr, tempValues, data);
                
                if (tempAttr.isEmpty())
                    uniqAttributes.add(tempAttr);
                
                tempAttr = attrib;
                tempValues = new ArrayList<String>();
                tempValues.add(value);
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
    
    private void addAttribValue(String key, List<String> value, Map<String, List<String>> map) {
        if (key == null || key.isEmpty())
            return;

        if (map.containsKey(key)) {
            List<String> values = map.get(key);
            map.remove(key);
            values.addAll(value);
            map.put(key, values);
            return;
        }

        map.put(key, value);
    }

}
