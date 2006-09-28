package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class QADAO {

    private HibernateFacade hibernateFacade;

    public QADAO() {
        hibernateFacade = new HibernateFacade();
    }

    public QAStep[] steps(EmfDataset dataset, Session session) {
        Criterion criterion = Restrictions.eq("datasetId", new Integer(dataset.getId()));

        List steps = session.createCriteria(QAStep.class).add(criterion).list();
        return (QAStep[]) steps.toArray(new QAStep[0]);
    }

    public void update(QAStep[] steps, Session session) {
        hibernateFacade.update(steps, session);
    }

    public void add(QAStep[] steps, Session session) {
        hibernateFacade.add(steps, session);
    }

    public QAProgram[] getQAPrograms(Session session) {
        List list = hibernateFacade.getAll(QAProgram.class, session);
        return (QAProgram[]) list.toArray(new QAProgram[0]);
    }

    public QAStepResult qaStepResult(QAStep step, Session session) {
        Criterion[] criterions = qaStepUniqueKeyConstraints(step);
        List list = hibernateFacade.get(QAStepResult.class, criterions, session);
        if (!list.isEmpty())
            return (QAStepResult) list.get(0);
        return null;
    }

    private Criterion[] qaStepUniqueKeyConstraints(QAStep step) {
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("qaStepId", new Integer(step.getId()));
        Criterion[] criterions = { c1, c2, c3 };
        return criterions;
    }

    public void updateQAStepResult(QAStepResult result, Session session) {
        hibernateFacade.update(result, session);
    }

    public boolean exists(QAStep step, Session session) {
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("name", step.getName());
        Criterion[] criterions = { c1, c2, c3 };
        return hibernateFacade.exists(QAStep.class, criterions, session);
    }

}
