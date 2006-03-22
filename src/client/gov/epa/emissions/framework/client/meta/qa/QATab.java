package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class QATab extends JPanel implements QATabView {

    private QAStepsTableData tableData;

    private DesktopManager desktopManager;

    private QATabPresenter presenter;

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    public QATab(EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("aqsteps");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void display(QAStep[] steps) {
        super.setLayout(new BorderLayout());
        super.add(createQAStepsTableSection(steps), BorderLayout.PAGE_START);
        super.add(createButtonsSection(), BorderLayout.CENTER);
        super.setSize(new Dimension(700, 300));
    }

    public void observe(QATabPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createQAStepsTableSection(QAStep[] steps) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(table(steps), BorderLayout.CENTER);
        return container;
    }

    protected JScrollPane table(QAStep[] steps) {
        tableData = new QAStepsTableData(steps);
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(tableModel);
        
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.setPreferredSize(new Dimension(450, 60));

        return new JScrollPane(panel);
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        JButton view = new JButton("View");
        view.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doView();
            }
        });
        container.add(view);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private void doView() {
        List steps = selectModel.selected();
        for (Iterator iter = steps.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            ViewQAStepWindow view = new ViewQAStepWindow(step.getName(), desktopManager);
            presenter.doView(step, view);
        }
    }

}
