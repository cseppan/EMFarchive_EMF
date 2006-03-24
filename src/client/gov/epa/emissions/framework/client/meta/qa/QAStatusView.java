package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public interface QAStatusView {
    void display();
    
    QAStep qaStepStub();
    
    boolean shouldSetStatus();
}
