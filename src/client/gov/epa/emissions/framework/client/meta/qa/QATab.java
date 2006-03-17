package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class QATab extends JPanel implements QATabView {

    private EmfConsole parentConsole;

    public QATab(EmfConsole parentConsole) {
        super.setName("qaTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(QAStep[] steps) {
        super.removeAll();
        super.add(createLayout(steps, parentConsole));
    }

    private JPanel createLayout(QAStep[] steps, EmfConsole parentConsole) {
        JPanel layout = new JPanel();
        layout.setBorder(new Border("QA Steps"));

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(createSortFilterPane(steps, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(QAStep[] steps, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new QAStepsTableData(steps));
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("qaStepsTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
