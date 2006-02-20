package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewNoteDialog extends Dialog implements NoteView {

    public ViewNoteDialog(EmfConsole parent) {
        super("Note", parent);
        super.setSize(new Dimension(550, 250));

        super.center();
    }

    public void display(Note note) {
        super.setTitle("Note: " + note.getName());
        super.getContentPane().add(createLayout(note));
        super.show();
    }

    private JPanel createLayout(Note note) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel(note));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(Note note) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name", new Label(note.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Type", new Label(note.getNoteType().getType()), panel);

        TextArea details = new TextArea("", note.getDetails(), 40, 3);
        details.setEditable(false);
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(details);
        layoutGenerator.addLabelWidgetPair("Details", scrollableDetails, panel);

        layoutGenerator.addLabelWidgetPair("References", new Label(note.getReferences()), panel);
        layoutGenerator.addLabelWidgetPair("Version", new Label(note.getVersion() + ""), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        return panel;
    }

    protected void close() {
        super.dispose();
    }

}
