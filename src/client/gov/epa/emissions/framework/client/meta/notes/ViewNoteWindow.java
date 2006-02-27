package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.Note;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewNoteWindow extends DisposableInteralFrame implements NoteView {

    private JPanel layout;

    public ViewNoteWindow(DesktopManager desktopManager) {
        super("View Note", new Dimension(350, 250), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void display(Note note) {
        String name = "View Note: " + note.getName();
        super.setTitle(name);
        super.setName(name);

        doLayout(note);
        super.display();
        System.out.println("displayed view - " + name);
    }

    private void doLayout(Note note) {
        layout.add(inputPanel(note));
        layout.add(buttonsPanel());
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

}
