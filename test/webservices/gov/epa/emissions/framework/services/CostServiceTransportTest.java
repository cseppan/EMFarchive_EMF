package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlMeasureServiceImpl;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.rmi.RemoteException;

public class CostServiceTransportTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ControlMeasureService service = null;

    private ControlMeasureService help;
    
    private UserService userService;
    
    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        help = new ControlMeasureServiceImpl(sessionFactory);

        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        service = rl.costService();
        userService = rl.userService();
    }

    public void testServiceActive() throws EmfException {
        assertEquals("Service works", help.getMeasures().length, 0);
    }

    public void testShouldGetAllControlMeasures() throws EmfException {
        ControlMeasure[] all = service.getMeasures();
        assertEquals("0 types", all.length, 0);
    }

    public void testShouldAddOneControlMeasure() throws RemoteException {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm test added");
        cm.setEquipmentLife(12);
        cm.setAbbreviation("12345678");
        service.addMeasure(cm);

        ControlMeasure[] all = service.getMeasures();
        assertEquals(all.length, 1);
        assertEquals("cm test added", all[0].getName());
        
        service.removeMeasure(all[0]);
        assertEquals(all.length - 1, service.getMeasures().length);
    }

    public void testShouldUpdateControlMeasure() throws RemoteException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added");
        cm.setAbbreviation("12345678");
        service.addMeasure(cm);
        
        ControlMeasure cmModified = service.obtainLockedMeasure(owner, service.getMeasures()[0]);
        cmModified.setEquipmentLife(120);
        cmModified.setName("cm updated");
        ControlMeasure cm2 = service.updateMeasure(cmModified);
        
        try {
            assertEquals("cm updated", cm2.getName());
            assertEquals(new Float(120), new Float(cm2.getEquipmentLife()));
        } finally {
            service.removeMeasure(cmModified);
        }
    }
    
    public void testShouldLockUnlockControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx");
        cm.setAbbreviation("yyyyyyyy");
        service.addMeasure(cm);
        
        ControlMeasure released = null;

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, service.getMeasures()[0]);
            assertTrue("Should have released lock", locked.isLocked());

            released = service.releaseLockedControlMeasure(locked);
            assertFalse("Should have released lock", released.isLocked());

        } finally {
            service.removeMeasure(released);
        }
    }

    public void testShouldGetCorrectSCCs() throws EmfException {
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added" + Math.random());
        cm.setAbbreviation("12345678");
        
        //These scc numbers have to exist in the reference.scc table
        cm.setSccs(new Scc[] {new Scc("10100224", ""), new Scc("10100225", ""), new Scc("10100226", "")} ); 
        service.addMeasure(cm);
        int measuresAfterAddOne = service.getMeasures().length;
        
        Scc[] sccs = service.getSccs(cm);
        service.removeMeasure(cm);
        

        assertEquals(3, sccs.length);
        assertEquals("10100224", sccs[0].getCode());
        assertEquals("10100225", sccs[1].getCode());
        assertEquals("10100226", sccs[2].getCode());
        
        assertEquals(measuresAfterAddOne, service.getMeasures().length + 1);
    }

    protected void doTearDown() throws Exception {// no op
    }

}
