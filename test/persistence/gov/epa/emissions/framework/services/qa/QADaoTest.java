package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.util.Date;

public class QADaoTest extends ServiceTestCase {

    private QADAO dao;

    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        dao = new QADAO();
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);

        try {
            QAStep[] steps = dao.steps(dataset, session);

            assertEquals(1, steps.length);
            assertEquals("name", steps[0].getName());
            assertEquals(2, steps[0].getVersion());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldUpdateQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);

        try {
            QAStep[] read = dao.steps(dataset, session);
            assertEquals(1, read.length);

            read[0].setName("updated-name");
            read[0].setProgram("updated-program");

            dao.update(read, session);
            session.clear();// to ensure Hibernate does not return cached objects

            QAStep[] updated = dao.steps(dataset, session);
            assertEquals(1, updated.length);
            assertEquals("updated-name", updated[0].getName());
            assertEquals("updated-program", updated[0].getProgram());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldAddNewStepsOnUpdateQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);

        try {
            dao.update(new QAStep[] { step }, session);
            session.clear();// to ensure Hibernate does not return cached objects

            QAStep[] updated = dao.steps(dataset, session);
            assertEquals(1, updated.length);
            assertEquals("name", updated[0].getName());
            assertEquals(2, updated[0].getVersion());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldSaveNewQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        InternalSource source = new InternalSource();
        source.setTable("table");
        step.setTableSource(source);
        step.setTableCreationDate(new Date());
        step.setTableCreationStatus("Created");
        step.setTableCurrent(true);

        try {
            dao.add(new QAStep[] { step }, session);
            session.clear();

            QAStep[] read = dao.steps(dataset, session);
            assertEquals(1, read.length);
            assertEquals("name", read[0].getName());
            assertEquals(2, read[0].getVersion());
            assertEquals(dataset.getId(), read[0].getDatasetId());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldGetAllDefaultQAPrograms() {
        QAProgram[] programs = dao.getQAPrograms(session);
        assertEquals(3, programs.length);
    }

    private EmfDataset newDataset(String name) {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);
        dataset.setCreator(owner.getUsername());

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

}
