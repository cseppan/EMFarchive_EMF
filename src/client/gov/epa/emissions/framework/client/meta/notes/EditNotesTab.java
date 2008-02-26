package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

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

    public void display(DatasetNote[] datasetNotes, EditNotesTabPresenter presenter) {
        super.removeAll();
        super.add(createLayout(datasetNotes, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(DatasetNote[] datasetNotes, EditNotesTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(datasetNotes, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(DatasetNote[] datasetNotes, EmfConsole parentConsole) {
        tableData = new NotesTableData(datasetNotes);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final EditNotesTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewNote(presenter);
            }
        });
        add.setToolTipText("add a new note");
        container.add(add);
        
        Button addExisting = new AddButton("Add Existing", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addExistingNotes(presenter);
            }
        });
        addExisting.setToolTipText("add an existing note");
        container.add(addExisting);

        Button view = new ViewButton(new AbstractAction() {
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
        for (Iterator iter = notes.iterator(); iter.hasNext();) {
            ViewNoteWindow window = new ViewNoteWindow(desktopManager);
            presenter.doViewNote((DatasetNote) iter.next(), window);
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
    
    protected void addExistingNotes(EditNotesTabPresenter presenter) {
        AddExistingNotesDialog view = new AddExistingNotesDialog(parentConsole);
        try {
            presenter.addExistingNotes(view);
            DatasetNote[] selectedNotes=presenter.getSelectedNotes(view);
            
//            System.out.println("length of selected notes " + selectedNotes.length );
            if (selectedNotes.length>0){
                tableData.add( selectedNotes);
            }
            refresh();
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError("Could not add existing notes" +e.getMessage());
        }
    }

    public void addNote(DatasetNote note) {
        tableData.add(note);
        refresh();
    }
    
    private void refresh(){
        selectModel.refresh();
        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public DatasetNote[] additions() {
        return tableData.additions();
    }

}
