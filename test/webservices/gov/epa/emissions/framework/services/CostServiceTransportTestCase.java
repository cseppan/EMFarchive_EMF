package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlMeasureServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class CostServiceTransportTestCase extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ControlMeasureService service = null;
    
    private ControlMeasureService help;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        help = new ControlMeasureServiceImpl(sessionFactory);
        
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

    protected void doTearDown() throws Exception {// no op
    }

}
