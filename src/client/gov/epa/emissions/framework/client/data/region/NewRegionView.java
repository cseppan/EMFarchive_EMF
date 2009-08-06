package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.framework.services.EmfException;

public interface NewRegionView {

    void observe(NewRegionPresenterImp presenter);

    void display() throws EmfException;

}