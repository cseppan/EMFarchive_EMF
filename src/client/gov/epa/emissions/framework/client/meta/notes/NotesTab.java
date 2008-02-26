package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.DatasetNote;
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

public class NotesTab extends JPanel implements NotesTabView {

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

    private NotesTabPresenter presenter;

    private DesktopManager desktopManager;

    public NotesTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("notesTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(DatasetNote[] notes, NotesTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(notes, parentConsole));
    }

    private JPanel createLayout(DatasetNote[] notes, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSortFilterPane(notes, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JScrollPane createSortFilterPane(DatasetNote[] notes, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new NotesTableData(notes));
        selectModel = new SortFilterSelectModel(model);

        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("notesTable");
        panel.setPreferredSize(new Dimension(450, 60));

        return new JScrollPane(panel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewNotes();
            }
        });
        buttonPanel.add(viewButton);

        panel.add(buttonPanel, BorderLayout.LINE_START);

        return panel;
    }

    private void viewNotes() {
        List selected = selectModel.selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewNoteWindow view = new ViewNoteWindow(desktopManager);
            presenter.doViewNote((DatasetNote) iter.next(), view);    
        }
    }
}
