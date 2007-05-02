package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.SetReferencesDialog;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewNoteDialog extends Dialog implements NewNoteView {

    private TextField name;

    protected boolean shouldCreate;

    private TextArea details;

    private DefaultComboBoxModel versionsModel;

    private VersionsSet versionsSet;

    private DefaultComboBoxModel typesModel;

    private NoteType[] types;

    private User user;

    private EmfDataset dataset;

    private String version;

    private EmfConsole parent;

    private Note[] selectedReferences;

    private TextArea referencesListing;

    public NewNoteDialog(EmfConsole parent) {
        super("Create new Note", parent);
        super.setSize(new Dimension(550, 300));
        super.center();
        
        this.parent = parent;
        selectedReferences = new Note[0];
    }

    public void display(User user, EmfDataset dataset, Version version, Note[] notes, NoteType[] types,
            Version[] versions) {
        versionsSet = new VersionsSet(versions);
        this.version = version.getName();

        doDisplay(user, dataset, notes, types);
    }

    public void display(User user, EmfDataset dataset, Note[] notes, NoteType[] types, Version[] versions) {
        versionsSet = new VersionsSet(versions);
        version = versionsSet.getDefaultVersionName(dataset);

        doDisplay(user, dataset, notes, types);
    }

    private void doDisplay(User user, EmfDataset dataset, Note[] notes, NoteType[] types) {
        this.user = user;
        this.dataset = dataset;
        this.types = types;

        JPanel layout = createLayout(notes, types, versionsSet);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(Note[] notes, NoteType[] types, VersionsSet versionsSet) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel(notes, types, versionsSet));
        panel.add(buttonsPanel(notes));

        return panel;
    }

    private JPanel inputPanel(final Note[] notes, NoteType[] types, VersionsSet versionsSet) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Summary", name, panel);

        typesModel = new DefaultComboBoxModel(typeNames(types));
        JComboBox typesCombo = createCombo(typesModel);
        layoutGenerator.addLabelWidgetPair("Type", typesCombo, panel);

        details = new TextArea("", "", 40, 3);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(details);
        layoutGenerator.addLabelWidgetPair("Details", scrollableDetails, panel);

        layoutReferences(notes, panel, layoutGenerator);

        versionsModel = new DefaultComboBoxModel(versionsSet.nameAndNumbers());
        JComboBox versionsCombo = createCombo(versionsModel);
        versionsCombo.setSelectedItem(version);
        layoutGenerator.addLabelWidgetPair("Version", versionsCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private void layoutReferences(final Note[] notes, JPanel panel, SpringLayoutGenerator layoutGenerator) {
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(scrollableReferences());

        Button button = new Button("Set", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddReferences(notes);
            }
        });
        button.setToolTipText("Set References");
        container.add(button);

        rightPanel.add(container, BorderLayout.LINE_START);

        layoutGenerator.addWidgetPair(new Label("References"), rightPanel, panel);
    }

    private ScrollableComponent scrollableReferences() {
        referencesListing = new TextArea("", "", 20, 2);
        referencesListing.setEditable(false);

        return ScrollableComponent.createWithVerticalScrollBar(referencesListing);
    }

    protected void doAddReferences(Note[] notes) {
        SetReferencesDialog dialog = new SetReferencesDialog(parent);
        dialog.display(notes, selectedReferences);
        selectedReferences = dialog.selected();
        referencesListing.setText(dialog.referencesList());
    }

    private String[] typeNames(NoteType[] types) {
        List names = new ArrayList();
        for (int i = 0; i < types.length; i++)
            names.add(types[i].getType());

        return (String[]) names.toArray(new String[0]);
    }

    private JComboBox createCombo(DefaultComboBoxModel model) {
        JComboBox combo = new JComboBox(model);
        combo.setEditable(false);
        combo.setPreferredSize(new Dimension(300, 20));

        return combo;
    }

    private JPanel buttonsPanel(final Note[] notes) {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew(notes);
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doNew(Note[] notes) {
        if (verifyInput(notes)) {
            shouldCreate = true;
            close();
        }
    }

    protected boolean verifyInput(Note[] notes) {
        String noteName = name.getText().trim();
        if (noteName.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter Name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (duplicate(noteName, notes)) {
            JOptionPane.showMessageDialog(super.getParent(), "Name is duplicate. Please enter a different name.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean duplicate(String noteName, Note[] notes) {
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].getName().equals(noteName))
                return true;
        }

        return false;
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public Note note() {
        Note note = new Note();
        note.setName(name.getText());
        note.setDetails(details.getText());
        note.setReferences(referencesListing.getText());
        note.setVersion(version().getVersion());
        note.setNoteType(type());
        note.setDate(new Date());
        note.setCreator(user);
        note.setDatasetId(dataset.getId());

        return note;
    }

    private NoteType type() {
        String type = (String) typesModel.getSelectedItem();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getType().equals(type))
                return types[i];
        }

        return null;
    }

    private Version version() {
        return versionsSet.getVersionFromNameAndNumber((String) versionsModel.getSelectedItem());
    }

}
