package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;

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

    public String getDefaultVersionName(int defaultVersion) {
        Integer[] versions = versions();
        for (int i = 0; i < versions.length; i++) {
            int val = versions[i].intValue();
            if (val == defaultVersion)
                return name(val);
        }

        return null;
    }

}
