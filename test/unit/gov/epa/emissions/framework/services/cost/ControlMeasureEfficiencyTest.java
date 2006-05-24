package gov.epa.emissions.framework.services.cost;

import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureEfficiency;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import junit.framework.TestCase;

public class ControlMeasureEfficiencyTest extends TestCase {
    public void testShouldSetAndGetAttributesCorrectly() {
        Date date = new Date();
        User user = new User("test user", "xxx", "xxx", "xxx@xxx.com", "xxx", "xxxxx1234", true, true);
        EfficiencyRecord record1 = new EfficiencyRecord();
        EfficiencyRecord record2 = new EfficiencyRecord();
        EfficiencyRecord record3 = new EfficiencyRecord();
        EfficiencyRecord[] records = new EfficiencyRecord[3];
        records[0] = record1;
        records[1] = record2;
        records[2] = record3;
        
        
        ControlMeasureEfficiency eff = new ControlMeasureEfficiency();
        eff.setName("eff one");
        eff.setId(1);
        eff.setLockDate(date);
        eff.setLockOwner(user.getName());
        eff.setEfficiencyRecords(records);

        assertEquals("eff one", eff.getName());
        assertEquals(new Integer(1), new Integer(eff.getId()));
        assertEquals(date, eff.getLockDate());
        assertEquals(user.getName(), eff.getLockOwner());
        assertEquals(3, eff.getEfficiencyRecords().length);
    }

}
