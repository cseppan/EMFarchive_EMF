package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.List;

public class VersionsSet {

    private Version[] versions;

    public VersionsSet(Version[] versions) {
        this.versions = versions;
    }

    public Integer[] versions() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++)
            list.add(new Integer(versions[i].getVersion()));

        return (Integer[]) list.toArray(new Integer[0]);
    }

    public Integer[] finalVersions() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                list.add(new Integer(versions[i].getVersion()));
        }

        return (Integer[]) list.toArray(new Integer[0]);
    }

    public Version[] finalVersionObjects() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                list.add(versions[i]);
        }

        return (Version[]) list.toArray(new Version[0]);
    }
    
    public String[] namesOfFinalVersions() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                list.add(versions[i].getName());
        }

        return (String[]) list.toArray(new String[0]);
    }

    public String[] nameAndNumbersOfFinalVersions() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                list.add(versions[i].getName() + " (" + versions[i].getVersion() + ")");
        }
        
        return (String[]) list.toArray(new String[0]);
    }

    public String[] names() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            list.add(versions[i].getName());
        }

        return (String[]) list.toArray(new String[0]);
    }
    
    public String[] nameAndNumbers() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            list.add(versions[i].getName() + " (" + versions[i].getVersion() + ")");
        }

        return (String[]) list.toArray(new String[0]);
    }

    public String name(int version) {
        String[] names = names();
        Integer[] versions = versions();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].intValue() == version)
                return names[i];
        }

        return null;
    }

    public String getVersionName(int version) {
        Integer[] versions = versions();
        for (int i = 0; i < versions.length; i++) {
            int val = versions[i].intValue();
            if (val == version)
                return name(val);
        }

        return null;
    }

    public Version version(String name) {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].getName().equals(name))
                return versions[i];
        }

        return null;
    }

    public Version getVersionFromNameAndNumber(String nameAndNumber) {
        int ver = getVersionNumber(nameAndNumber);
        
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].getVersion() == ver)
                return versions[i];
        }
        
        return null;
    }

    private int getVersionNumber(String nameAndNumber) {
        int forPerenth = nameAndNumber.indexOf('(');
        int backPerenth = nameAndNumber.indexOf(')');
        
        return Integer.parseInt(nameAndNumber.substring(forPerenth+1, backPerenth));
    }

    public String getDefaultVersionName(EmfDataset dataset) {
        return getVersionName(dataset.getDefaultVersion());
    }

    public boolean contains(String name) {
        return version(name) != null;
    }

    
    public Version[] all() {
        return versions;
    }
    
    public int size() {
        return versions.length;
    }

}
