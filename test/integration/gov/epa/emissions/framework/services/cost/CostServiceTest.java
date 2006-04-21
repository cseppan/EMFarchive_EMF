package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.ServiceTestCase;

public class CostServiceTest extends ServiceTestCase {

    private CostService service;

    public void doSetUp() throws Exception {
        service = new CostServiceImpl(sessionFactory());
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlMeasures() throws Exception {

        ControlMeasure cm = new ControlMeasure("cm one");
        cm.setEquipmentLife(12);
        add(cm);

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals("cm one", cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }
}
