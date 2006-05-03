package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RevisionsTab extends JPanel implements RevisionsTabView {

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

    private DesktopManager desktopManager;

    private RevisionsTabPresenter presenter;

    public RevisionsTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("revisionsTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(Revision[] revisions, RevisionsTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(revisions, parentConsole));
    }

    private JPanel createLayout(Revision[] revisions, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSortFilterPane(revisions, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JScrollPane createSortFilterPane(Revision[] revisions, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new RevisionsTableData(revisions));
        selectModel = new SortFilterSelectModel(model);

        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("revisionsTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
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
        List selected = selectModel.selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewRevisionWindow view = new ViewRevisionWindow(desktopManager);
            presenter.doViewRevision((Revision) iter.next(), view);
        }
    }
}
