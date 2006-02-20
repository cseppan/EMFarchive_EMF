package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class NotesTab extends JPanel implements NotesTabView {

    private EmfConsole parentConsole;

    private NotesTabPresenter presenter;

    public NotesTab(EmfConsole parentConsole) {
        super.setName("notesTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(Note[] notes, NotesTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(notes, parentConsole));
    }

    private JPanel createLayout(Note[] notes, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSortFilterPane(notes, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Button view = new Button("View", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doView();
            }
        });
        panel.add(view, BorderLayout.LINE_START);

        return panel;
    }

    protected void doView() {// TODO- show NoteView
        presenter.doViewNote(null, new ViewNoteDialog(parentConsole));
    }

    private JScrollPane createSortFilterPane(Note[] notes, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new NotesTableData(notes));
        SortFilterSelectModel selectModel = new SortFilterSelectModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, selectModel);
        panel.getTable().setName("notesTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
