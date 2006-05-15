package gov.epa.emissions.framework.services;

import java.rmi.RemoteException;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.CostServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class CostServiceTransportTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private CostService service = null;

    private CostService help;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        help = new CostServiceImpl(sessionFactory);

        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        service = rl.costService();
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

    protected void doTearDown() throws Exception {// no op
    }

}
