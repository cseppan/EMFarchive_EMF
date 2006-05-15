package gov.epa.emissions.framework.services.cost;

import java.util.Date;

import gov.epa.emissions.commons.security.User;
import junit.framework.TestCase;

public class ControlMeasureTest extends TestCase {
    public void testShouldSetAndGetAttributesCorrectly() {
        Date date = new Date();
        User user = new User("test user", "xxx", "xxx", "xxx@xxx.com", "xxx", "xxxxx1234", true, true);
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one modified");
        cm.setAnnualizedCost(1000);
        cm.setCreator(user);
        cm.setDescription("description: cm");
        cm.setDeviceCode(111);
        cm.setEquipmentLife(20);
        cm.setId(1);
        cm.setLockDate(date);
        cm.setLockOwner(user.getName());
        cm.setMajorPollutant("CO");
        cm.setRuleEffectiveness(100);
        cm.setRulePenetration(0);

        assertEquals("cm one modified", cm.getName());
        assertEquals(new Float(1000), new Float(cm.getAnnualizedCost()));
        assertEquals(user, cm.getCreator());
        assertEquals("description: cm", cm.getDescription());
        assertEquals(new Integer(111), new Integer(cm.getDeviceCode()));
        assertEquals(new Float(20), new Float(cm.getEquipmentLife()));
        assertEquals(new Integer(1), new Integer(cm.getId()));
        assertEquals(date, cm.getLockDate());
        assertEquals(user.getName(), cm.getLockOwner());
        assertEquals("CO", cm.getMajorPollutant());
        assertEquals(new Float(100), new Float(cm.getRuleEffectiveness()));
        assertEquals(new Float(0), new Float(cm.getRulePenetration()));
    }

}
