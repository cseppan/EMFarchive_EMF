package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.ServiceTestCase;

public class CostServiceTest extends ServiceTestCase {

    private CostService service;

    public void doSetUp() throws Exception {
        service = new CostServiceImpl(sessionFactory(configFile()));
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlMeasures() throws Exception {

        ControlMeasure cm = new ControlMeasure("cm test one");
        cm.setEquipmentLife(12);
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

        ControlMeasure cm = new ControlMeasure("cm test added");
        cm.setEquipmentLife(12);
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

    public void testShouldRemoveOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure("cm test added");
        cm.setEquipmentLife(12);
        service.addMeasure(cm);

        ControlMeasure[] cms = service.getMeasures();

        assertEquals(1, cms.length);
        assertEquals("cm test added", cms[0].getName());
        assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        
        service.removeMeasure(cm);
        assertEquals(0, service.getMeasures().length);
    }
}
