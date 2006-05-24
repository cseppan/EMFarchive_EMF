package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.CostRecord;

import java.util.Date;

import junit.framework.TestCase;

public class ControlMeasureCostTest extends TestCase {
    public void testShouldSetAndGetAttributesCorrectly() {
        Date date = new Date();
        User user = new User("test user", "xxx", "xxx", "xxx@xxx.com", "xxx", "xxxxx1234", true, true);
        CostRecord record1 = new CostRecord();
        CostRecord record2 = new CostRecord();
        CostRecord record3 = new CostRecord();
        CostRecord[] records = new CostRecord[3];
        records[0] = record1;
        records[1] = record2;
        records[2] = record3;
        
        
        ControlMeasureCost cost = new ControlMeasureCost();
        cost.setName("eff one");
        cost.setId(1);
        cost.setLockDate(date);
        cost.setLockOwner(user.getName());
        cost.setCostRecords(records);

        assertEquals("eff one", cost.getName());
        assertEquals(new Integer(1), new Integer(cost.getId()));
        assertEquals(date, cost.getLockDate());
        assertEquals(user.getName(), cost.getLockOwner());
        assertEquals(3, cost.getCostRecords().length);
    }

}
