package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.CostServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.rmi.RemoteException;

public class CostServiceTransportTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private CostService service = null;

    private CostService help;
    
    private UserService userService;
    
    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        help = new CostServiceImpl(sessionFactory);

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
    
    protected void doTearDown() throws Exception {// no op
    }

}
