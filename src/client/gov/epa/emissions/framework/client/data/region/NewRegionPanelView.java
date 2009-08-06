package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;

import javax.swing.JComponent;

public interface NewRegionPanelView {
    void display(JComponent container) throws EmfException;
    GeoRegion setFields() throws EmfException;
    void validateFields() throws EmfException;
    
}
