package gov.epa.emissions.framework.services;

import java.util.Date;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlMeasureServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlMeasureServicePerformanceTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ControlMeasureService transport = null;

    private ControlMeasureService server;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        server = new ControlMeasureServiceImpl(sessionFactory);
        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        transport = rl.controlMeasureService();
    }

    public void itestShouldGetAllControlMeasures_AtServerSide() throws EmfException {
        dumpMemory();
        ControlMeasure[] all = server.getMeasures();
        dumpMemory();
        assertEquals("0 types", all.length, 1067);
    }
    
    public void testShouldGetAllControlMeasures() throws EmfException {
        dumpMemory();
        ControlMeasure[] all = transport.getMeasures();
        dumpMemory();
        assertEquals("0 types", all.length, 1067);
    }

 

    protected void doTearDown() throws Exception {// no op
    }
    
    protected void dumpMemory() {
        System.out.println("date-"+EmfDateFormat.format_MM_DD_YYYY_HH_mm_ss(new Date())+", "+usedMemory() + " MB");
        
    }

    protected long usedMemory() {
        return (totalMemory() - freeMemory());
    }

    protected long maxMemory() {
        return (Runtime.getRuntime().maxMemory() / megabyte());
    }

    protected long freeMemory() {
        return Runtime.getRuntime().freeMemory() / megabyte();
    }
    

    private int megabyte() {
        return (1024 * 1024);
    }
    

    protected long totalMemory() {
        return Runtime.getRuntime().totalMemory() / megabyte();
    }

}
