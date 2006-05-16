package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class CostServiceTest extends ServiceTestCase {

    private CostService service;
    
    private UserServiceImpl userService;

    public void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        userService = new UserServiceImpl(sessionFactory);
        service = new CostServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlMeasures() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test one");
        add(cm);

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals("cm test one", cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }

    public void testShouldAddOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added");
        service.addMeasure(cm);

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals("cm test added", cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasure() throws Exception {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added");
        service.addMeasure(cm);
        
        ControlMeasure cmModified = service.obtainLockedMeasure(owner, cm);
        cmModified.setEquipmentLife(120);
        cmModified.setName("cm updated");
        ControlMeasure cm2 = service.updateMeasure(cmModified);
        
        try {
            assertEquals("cm updated", cm2.getName());
            assertEquals(new Float(120), new Float(cm2.getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }

    public void testShouldRemoveOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added");
        service.addMeasure(cm);

        ControlMeasure[] cms = service.getMeasures();

        assertEquals(1, cms.length);
        assertEquals("cm test added", cms[0].getName());
        assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        
        service.removeMeasure(cm);
        assertEquals(0, service.getMeasures().length);
    }
}
