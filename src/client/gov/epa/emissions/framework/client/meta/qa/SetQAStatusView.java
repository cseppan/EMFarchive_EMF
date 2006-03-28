package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public interface SetQAStatusView {
    void display(QAStep[] steps);

    boolean shouldSetStatus();
}
