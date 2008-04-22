package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

public class QATab extends JPanel implements QATabView {

    private DesktopManager desktopManager;

    private ViewQATabPresenter presenter;

    private SelectableSortFilterWrapper table;

    private EmfConsole parentConsole;

    private MessagePanel messagePanel;

    private EmfSession session;

    public QATab(MessagePanel messagePanel, EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("aqsteps");
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void display(QAStep[] steps, EmfSession session) {
        this.session = session;
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);
    }
    
    public void display(QAStep[] steps) {
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);
    }

    public void observe(ViewQATabPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel tablePanel(QAStep[] steps) {
        JPanel container = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new QAStepsTableData(steps), sortCriteria());
        container.add(table, BorderLayout.CENTER);
        return container;
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Version", "Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] { false, true, true }, new boolean[] { true, true, true });
        //return new SortCriteria(columnNames, new boolean[] { false, false }, new boolean[] { true, true });
    }

//    protected JScrollPane table(QAStep[] steps) {
//        EmfTableModel tableModel = new EmfTableModel(new QAStepsTableData(steps));
//        selectModel = new SortFilterSelectModel(tableModel);
//
//        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
//        panel.setPreferredSize(new Dimension(450, 60));
//
//        return new JScrollPane(panel);
//    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        JButton view = new BorderlessButton("View", new AbstractAction() {
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
        List steps = table.selected();
        for (Iterator iter = steps.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            ViewQAStepWindow view = new ViewQAStepWindow(parentConsole, session, desktopManager);
            try {
                presenter.doView(step, view);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

}
