package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RevisionsTab extends JPanel implements RevisionsTabView {

    private EmfConsole parentConsole;

    public RevisionsTab(EmfConsole parentConsole) {
        super.setName("revisionsTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(Revision[] revisions) {
        super.removeAll();
        super.add(createLayout(revisions, parentConsole));
    }

    private JPanel createLayout(Revision[] revisions, EmfConsole parentConsole) {
        JPanel layout = new JPanel();

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(createSortFilterPane(revisions, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(Revision[] revisions, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new RevisionsTableData(revisions));
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("revisionsTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
