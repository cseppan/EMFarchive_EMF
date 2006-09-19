package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class SQLQAProgramRunnerTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldRunSQLQAStep() throws Exception {
        EmfDataset dataset = null;
        try {
            dataset = newDataset();
            QAStep step = new QAStep();
            step.setName("QA1");
            step.setProgramArguments("SELECT * FROM reference.pollutants");
            step.setDatasetId(dataset.getId());
            add(step);

            SQLQAProgramRunner runner = new SQLQAProgramRunner(dbServer(), step);
            runner.run();
        } finally {
            if (dataset != null)
                dropTable("QA1" + "_" + dataset.getId(), dbServer().getEmissionsDatasource());
            dropAll(QAStep.class);
            dropAll(EmfDataset.class);
        }

    }

    private EmfDataset newDataset() {
        User owner = new UserDAO().get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
        dataset.setCreator(owner.getUsername());
        add(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

}
