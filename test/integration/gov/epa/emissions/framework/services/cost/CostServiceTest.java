package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CostServiceTest extends ServiceTestCase {

    private ControlMeasureService service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;

    public void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        userService = new UserServiceImpl(sessionFactory);
        service = new ControlMeasureServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlMeasures() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(12);
        cm.setName(name);
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals(name, cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }

    public void testShouldAddOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(12);
        cm.setName(name);
        cm.setAbbreviation("12345679");
        Scc scc1 = new Scc("1023232", "");
        service.addMeasure(cm, new Scc[] { scc1 });

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals(name, cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasure() throws Exception {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(12);
        cm.setName(name);
        cm.setAbbreviation("12345688");
        Scc scc1= new Scc("123232","");
        service.addMeasure(cm, new Scc[]{scc1});

        ControlMeasure cmModified = service.obtainLockedMeasure(owner, cm);
        cmModified.setEquipmentLife(120);
        cmModified.setName("cm updated");
        ControlMeasure cm2 = service.updateMeasure(cmModified, new Scc[]{});

        try {
            assertEquals("cm updated", cm2.getName());
            assertEquals(new Float(120), new Float(cm2.getEquipmentLife()));
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldLockControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx" + Math.random());
        cm.setAbbreviation("yyyyyyyy");
        Scc scc1= new Scc("123232","");
        service.addMeasure(cm, new Scc[]{scc1});

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, cm);
            assertTrue("Should have released lock", locked.isLocked());

            ControlMeasure loadedFromDb = load(cm);
            assertTrue("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldReleaseLockedControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx" + Math.random());
        cm.setAbbreviation("yyyyyyyy");
        Scc scc1= new Scc("123232","");
        service.addMeasure(cm, new Scc[]{scc1});

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, cm);
            ControlMeasure released = service.releaseLockedControlMeasure(locked);
            assertFalse("Should have released lock", released.isLocked());

            ControlMeasure loadedFromDb = load(cm);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void FIXME_testShouldRemoveOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(12);
        cm.setName(name);
        cm.setAbbreviation("12345678");
        Scc scc1= new Scc("123232","");
        service.addMeasure(cm, new Scc[]{scc1});

        ControlMeasure[] cms = service.getMeasures();

        assertEquals(1, cms.length);
        assertEquals(name, cms[0].getName());
        assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));

        service.removeMeasure(cm);//FIXME: remove measure 
        assertEquals(0, service.getMeasures().length);
    }

    private ControlMeasure load(ControlMeasure cm) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlMeasure.class).add(Restrictions.eq("name", cm.getName()));
            tx.commit();

            return (ControlMeasure) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
