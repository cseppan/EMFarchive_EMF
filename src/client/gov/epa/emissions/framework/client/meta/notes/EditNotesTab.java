package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditNotesTab extends JPanel implements EditNotesTabView {

    private EmfConsole parentConsole;

    private NotesTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    public EditNotesTab(EmfConsole parentConsole) {
        super.setName("editNotesTab");
        this.parentConsole = parentConsole;
        super.setLayout(new BorderLayout());
    }

    public void display(User user, EmfDataset dataset, Note[] notes, NoteType[] types, Version[] versions) {
        super.removeAll();
        super.add(createLayout(user, dataset, notes, types, versions, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(User user, EmfDataset dataset, Note[] notes, NoteType[] types, Version[] versions,
            EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(notes, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(user, dataset, types, versions), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(Note[] notes, EmfConsole parentConsole) {
        tableData = new NotesTableData(notes);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole));

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterTablePanel sortFilterPanel = new SortFilterTablePanel(parentConsole, selectModel);
        sortFilterPanel.getTable().setName("notesTable");

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final User user, final EmfDataset dataset, final NoteType[] types,
            final Version[] versions) {
        JPanel panel = new JPanel(new BorderLayout());

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewNote(user, dataset, types, versions);
            }
        });
        panel.add(add, BorderLayout.LINE_START);

        return panel;
    }

    protected void doNewNote(User user, EmfDataset dataset, NoteType[] types, Version[] versions) {
        NewNoteDialog dialog = new NewNoteDialog(parentConsole);
        dialog.display(user, dataset, types, versions);

        if (dialog.shouldCreate())
            addNote(dialog.note());
    }

    private void addNote(Note note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public Note[] additions() {
        return tableData.additions();
    }

}
