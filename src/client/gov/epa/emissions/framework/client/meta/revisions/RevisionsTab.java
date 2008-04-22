package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class RevisionsTab extends JPanel implements RevisionsTabView {

    private EmfConsole parentConsole;

    private SelectableSortFilterWrapper table;

    private DesktopManager desktopManager;
    
    private MessagePanel messagePanel;

    private RevisionsTabPresenter presenter;

    public RevisionsTab(EmfConsole parentConsole, DesktopManager desktopManager, MessagePanel messagePanel) {
        super.setName("revisionsTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.messagePanel =messagePanel;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(Revision[] revisions, RevisionsTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(revisions, parentConsole));
    }

    private JPanel createLayout(Revision[] revisions, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout(5, 10));

        layout.add(tablePanel(revisions, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JPanel tablePanel(Revision[] revisions, EmfConsole parentConsole) {
        //EmfTableModel model = new EmfTableModel(new RevisionsTableData(revisions));
        JPanel tablePanel = new JPanel(new BorderLayout());

        table = new SelectableSortFilterWrapper(parentConsole, new RevisionsTableData(revisions), null);

        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewRevisions();
            }
        });
        buttonPanel.add(viewButton);

        panel.add(buttonPanel, BorderLayout.LINE_START);

        return panel;
    }

    private void viewRevisions() {
        List selected = table.selected();
        if (selected == null ||selected.size() == 0) {
            messagePanel.setMessage("Please select a revision.");
            return;
        }
            
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewRevisionWindow view = new ViewRevisionWindow(desktopManager);
            presenter.doViewRevision((Revision) iter.next(), view);
        }
    }
}
