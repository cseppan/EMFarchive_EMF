package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddReferencesDialog extends Dialog {

    private ListWidget list;

    protected Note[] selected;

    public AddReferencesDialog(EmfConsole parent) {
        super("Add References", parent);
        super.setSize(new Dimension(250, 275));

        super.center();
    }

    public void display(Note[] all, Note[] selected) {
        this.selected = selected;
        JPanel layout = createLayout(all, selected);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(Note[] all, Note[] selected) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(listPanel(all, selected));
        panel.add(controlPanel());

        return panel;
    }

    private JPanel listPanel(Note[] notes, Note[] selected) {
        JPanel panel = new JPanel(new BorderLayout());

        list = new ListWidget(notes, selected);

        JScrollPane listScroller = new JScrollPane(list);
        panel.add(listScroller, BorderLayout.CENTER);

        return panel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();

        Button okButton = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List selectedValues = Arrays.asList(list.getSelectedValues());
                selected = (Note[]) selectedValues.toArray(new Note[0]);
                close();
            }
        });
        panel.add(okButton);

        Button cancelButton = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancelButton);

        return panel;
    }

    public Note[] selected() {
        return selected;
    }

}
