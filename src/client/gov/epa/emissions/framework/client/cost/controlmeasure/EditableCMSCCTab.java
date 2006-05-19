package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.EditableTablePanel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

public class EditableCMSCCTab extends JPanel implements EditableCMTabView {

    private EditableTablePanel panel;

    private SCCTableData tableData;

    public EditableCMSCCTab(ControlMeasure measure, EmfSession session, ManageChangeables changeables,
            MessagePanel messagePanel) {
        doLayout(measure, changeables);
    }

    private void doLayout(ControlMeasure measure, ManageChangeables changeables) {
        String[] sccs = measure.getSccs();
        tableData = new SCCTableData(sccs);
        panel = new EditableTablePanel("", tableData, changeables, null);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }

    public void save(ControlMeasure measure) throws EmfException {
        panel.commit();
        List sccs = tableData.rows();
        String[] newSccs = new String[sccs.size()];
        for (int i = 0; i < sccs.size(); i++) {
            EditableRow row = (EditableRow) sccs.get(i);
            row.validate(i);
            newSccs[i] = (String) row.source();
        }
        measure.setSccs(newSccs);
    }

}
