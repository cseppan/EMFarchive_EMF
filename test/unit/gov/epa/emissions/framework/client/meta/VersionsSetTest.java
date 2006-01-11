package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import junit.framework.TestCase;

public class VersionsSetTest extends TestCase {
    public void testVersionNumsMatchVersionNames() {
        Version[] versions = new Version[11];
        for(int i = 0; i < versions.length; i++) {
            versions[i] = new Version();
            versions[i].setVersion(i);
            versions[i].setName("" + i);
        }
        
        VersionsSet vset = new VersionsSet(versions);
        Integer[] vnums = vset.versions();
        String[] vnames = vset.names();
        for(int j = 0; j < vnums.length; j++)
            assertTrue(vnums[j].intValue() == Integer.parseInt(vnames[j]));
    }
}
