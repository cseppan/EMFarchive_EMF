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

    public Object[] finalVersions() {
        List list = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                list.add(new Integer(versions[i].getVersion()));
        }

        return (Integer[]) list.toArray(new Integer[0]);
    }

}
