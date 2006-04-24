package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.text.SimpleDateFormat;

import javax.swing.JPanel;

public class EditableCaseSummaryTab extends JPanel implements EditableCaseSummaryTabView {

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public EditableCaseSummaryTab(Case caseObj, CaseService service, MessagePanel messagePanel,
            ManageChangeables changeablesList) {// TODO
    }

    public void save(Case caseObj) {// TODO
    }

}
