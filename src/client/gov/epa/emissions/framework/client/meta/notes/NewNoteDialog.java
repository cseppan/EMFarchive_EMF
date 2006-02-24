package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.ui.Dialog;

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

    private TextField references;

    private DefaultComboBoxModel versionsModel;

    private VersionsSet versionsSet;

    private DefaultComboBoxModel typesModel;

    private NoteType[] types;

    private User user;

    private EmfDataset dataset;

    private String version;

    public NewNoteDialog(EmfConsole parent) {
        super("Create new Note", parent);
        super.setSize(new Dimension(550, 250));

        super.center();
    }

    public void display(User user, EmfDataset dataset, Version version, NoteType[] types, Version[] versions) {
        versionsSet = new VersionsSet(versions);
        this.version = version.getName();

        doDisplay(user, dataset, types);
    }

    public void display(User user, EmfDataset dataset, NoteType[] types, Version[] versions) {
        versionsSet = new VersionsSet(versions);
        version = versionsSet.getDefaultVersionName(dataset);

        doDisplay(user, dataset, types);
    }

    private void doDisplay(User user, EmfDataset dataset, NoteType[] types) {
        this.user = user;
        this.dataset = dataset;
        this.types = types;

        super.getContentPane().add(createLayout(types, versionsSet));
        super.show();
    }

    private JPanel createLayout(NoteType[] types, VersionsSet versionsSet) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel(types, versionsSet));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(NoteType[] types, VersionsSet versionsSet) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Summary", name, panel);

        typesModel = new DefaultComboBoxModel(typeNames(types));
        JComboBox typesCombo = createCombo(typesModel);
        layoutGenerator.addLabelWidgetPair("Type", typesCombo, panel);

        details = new TextArea("", "", 40, 3);
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(details);
        layoutGenerator.addLabelWidgetPair("Details", scrollableDetails, panel);

        references = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("References", references, panel);

        versionsModel = new DefaultComboBoxModel(versionsSet.names());
        JComboBox versionsCombo = createCombo(versionsModel);
        versionsCombo.setSelectedItem(version);
        layoutGenerator.addLabelWidgetPair("Version", versionsCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
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

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (verifyInput()) {
                    shouldCreate = true;
                    close();
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected boolean verifyInput() {
        if (name.getText().trim().length() != 0)
            return true;

        JOptionPane.showMessageDialog(super.getParent(), "Please enter Name", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    protected void close() {
        super.dispose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public Note note() {
        Note note = new Note();
        note.setName(name.getText());
        note.setDetails(details.getText());
        note.setReferences(references.getText());
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
        return versionsSet.version((String) versionsModel.getSelectedItem());
    }

}
