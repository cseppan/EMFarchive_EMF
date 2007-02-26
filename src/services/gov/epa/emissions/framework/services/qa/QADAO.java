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

    public void updateQAStepsIds(QAStep[] steps, Session session) {
        for (int i = 0; i < steps.length; i++) {
            Criterion[] criterions = qaStepKeyConstraints(steps[i]);
            List list = hibernateFacade.get(QAStep.class, criterions, session);
            if (!list.isEmpty())
                steps[i].setId(((QAStep) list.get(0)).getId());
        }
    }

    public void add(QAStep[] steps, Session session) {
        hibernateFacade.add(steps, session);
    }

    public QAProgram[] getQAPrograms(Session session) {
        List list = hibernateFacade.getAll(QAProgram.class, session);
        return (QAProgram[]) list.toArray(new QAProgram[0]);
    }

    public QAStepResult qaStepResult(QAStep step, Session session) {
        updateQAStepsIds(new QAStep[]{step},session);
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("qaStepId", new Integer(step.getId()));
        Criterion[] criterions1 = { c1, c2, c3 };
        Criterion[] criterions = criterions1;
        List list = hibernateFacade.get(QAStepResult.class, criterions, session);
        if (!list.isEmpty())
            return (QAStepResult) list.get(0);
        return null;
    }

    public void updateQAStepResult(QAStepResult result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public boolean exists(QAStep step, Session session) {
        Criterion[] criterions = qaStepKeyConstraints(step);
        return hibernateFacade.exists(QAStep.class, criterions, session);
    }

    private Criterion[] qaStepKeyConstraints(QAStep step) {
        Criterion c1 = Restrictions.eq("datasetId", new Integer(step.getDatasetId()));
        Criterion c2 = Restrictions.eq("version", new Integer(step.getVersion()));
        Criterion c3 = Restrictions.eq("name", step.getName());
        Criterion[] criterions = { c1, c2, c3 };
        return criterions;
    }

    public QAProgram addQAProgram(QAProgram program, Session session) {
        hibernateFacade.add(program, session);
        
        return (QAProgram)load(QAProgram.class, program.getName(), session);
    }
    
    private Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }

}
