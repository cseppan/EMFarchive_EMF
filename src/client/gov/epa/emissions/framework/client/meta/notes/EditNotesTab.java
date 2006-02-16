package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SimpleTableModel;
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

    private EmfTableModel model;

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

        layout.add(createSortFilterPane(notes, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(user, dataset, types, versions), BorderLayout.PAGE_END);

        return layout;
    }

    private JScrollPane createSortFilterPane(Note[] notes, EmfConsole parentConsole) {
        tableData = new NotesTableData(notes);
        model = new EmfTableModel(tableData);
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("notesTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

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

        if (dialog.shouldCreate()) {
            tableData.add(dialog.note());
            model.refresh();
        }
    }

    public Note[] additions() {
        return tableData.additions();
    }

}
