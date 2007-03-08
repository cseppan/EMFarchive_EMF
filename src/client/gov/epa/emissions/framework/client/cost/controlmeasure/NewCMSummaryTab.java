package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;

public class NewCMSummaryTab extends ControlMeasureSummaryTab {

    public NewCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super(measure, session, messagePanel, changeablesList, null);
        super.setName("summary");
    }

    public void populateValues() {
        super.populateFields();
        deviceCode.setText("");
        equipmentLife.setText("");
        lastModifiedBy.setText("");
    }

    public void save(ControlMeasure measure) throws EmfException {
        super.save(measure);
    }

}
