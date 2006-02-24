package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfDataset;

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

    public String[] names() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            list.add(versions[i].getName());
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

    public String getDefaultVersionName(EmfDataset dataset) {
        return getVersionName(dataset.getDefaultVersion());
    }

}
