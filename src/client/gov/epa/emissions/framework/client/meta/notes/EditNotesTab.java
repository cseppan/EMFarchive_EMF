package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditNotesTab extends JPanel implements EditNotesTabView {

    private EmfConsole parentConsole;

    private NotesTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private ManageChangeables changeables;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;
    
    public EditNotesTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editNotesTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;

        super.setLayout(new BorderLayout());
    }

    public void display(Note[] notes, EditNotesTabPresenter presenter) {
        super.removeAll();
        super.add(createLayout(notes, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(Note[] notes, EditNotesTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(notes, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(Note[] notes, EmfConsole parentConsole) {
        tableData = new NotesTableData(notes);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterTablePanel sortFilterPanel = new SortFilterTablePanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final EditNotesTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewNote(presenter);
            }
        });
        container.add(add);

        Button view = new Button("View", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doViewNote(presenter);
            }
        });
        container.add(view);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doViewNote(EditNotesTabPresenter presenter) {
        List notes = selectModel.selected();
        for(Iterator iter = notes.iterator(); iter.hasNext();) {
            ViewNoteWindow window = new ViewNoteWindow(desktopManager);
            presenter.doViewNote((Note) iter.next(), window); 
        }
    }

    protected void doNewNote(EditNotesTabPresenter presenter) {
        NewNoteDialog view = new NewNoteDialog(parentConsole);
        try {
            presenter.doAddNote(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void addNote(Note note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public Note[] additions() {
        return tableData.additions();
    }

}
