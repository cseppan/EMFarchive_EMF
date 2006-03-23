package gov.epa.emissions.framework.client.meta.qa;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.services.data.QAStep;

public class QASteps {

    private QAStep[] sources;

    public QASteps(QAStep[] sources) {
        this.sources = sources;
    }

    QAStep[] filterDuplicates(QAStep[] steps) {
        List newSteps = new ArrayList();

        for (int i = 0; i < steps.length; i++) {
            if (!contains(sources, steps[i]))
                newSteps.add(steps[i]);
        }

        return (QAStep[]) newSteps.toArray(new QAStep[0]);
    }

    boolean contains(QAStep[] sources, QAStep step) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getName().equals(step.getName()) && sources[i].getVersion() == step.getVersion()) {
                return true;
            }
        }

        return false;
    }
}
