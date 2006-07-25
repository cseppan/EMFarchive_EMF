package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class RetrieveSCCTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // no op
    }

    protected void doTearDown() throws Exception {
        // no op
    }

    // setup the reference schema before running this test
    public void testShouldGetCorrectSCCs() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(12);
        cm.setName("cm test added" + Math.random());
        cm.setAbbreviation("12345678");
        // These scc numbers have to exist in the reference.scc table
        cm.setSccs(new Scc[] { new Scc("10100101", ""), new Scc("10100225", ""), new Scc("10100226", "") });

        ControlMeasuresDAO dao = null;
        Scc[] sccs = null;
        try {
            dao = new ControlMeasuresDAO();
            addMeasure(cm, dao);
            RetrieveSCC retreiveSCC = new RetrieveSCC(cm, dbServer());
            sccs = retreiveSCC.sccs();
        } finally {
            removeMeasure(cm, dao);
        }
        assertEquals(3, sccs.length);
        assertEquals("10100101", sccs[0].getCode());
        assertEquals("10100225", sccs[1].getCode());
        assertEquals("10100226", sccs[2].getCode());
        assertEquals("External Combustion Boilers;Electric Generation;Anthracite Coal;Pulverized Coal", sccs[0]
                .getDescription());
        assertEquals(
                "External Combustion Boilers;Electric Generation;Bituminous/Subbituminous Coal;Traveling Grate (Overfeed) Stoker (Subbituminous Coal)",
                sccs[1].getDescription());
        assertEquals(
                "External Combustion Boilers;Electric Generation;Bituminous/Subbituminous Coal;Pulverized Coal: Dry Bottom Tangential (Subbituminous Coal)",
                sccs[2].getDescription());

    }

    private void addMeasure(ControlMeasure measure, ControlMeasuresDAO dao) throws HibernateException, Exception {
        Session session = sessionFactory().getSession();
        try {
            dao.add(measure, session);
        } finally {
            session.close();
        }
    }

    private void removeMeasure(ControlMeasure measure, ControlMeasuresDAO dao) throws HibernateException, Exception {
        Session session = sessionFactory().getSession();
        try {
            dao.remove(measure, session);
        } finally {
            session.close();
        }
    }

}
