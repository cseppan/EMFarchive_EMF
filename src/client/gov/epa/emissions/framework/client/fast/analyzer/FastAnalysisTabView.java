package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public interface FastAnalysisTabView {

    void save(FastAnalysis analysis) throws EmfException;

    void refresh(FastAnalysis analysis);

    void display();

    void viewOnly();
}