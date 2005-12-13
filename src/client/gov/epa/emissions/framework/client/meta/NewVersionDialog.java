package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SpringLayout;

import abbot.editor.widgets.TextField;

public class NewVersionDialog extends JDialog {

    private TextField name;

    private DefaultComboBoxModel versionsModel;

    protected boolean shouldCreate;

    private Version[] versions;

    public NewVersionDialog(EmfDataset dataset, Version[] versions, EmfConsole parent) {
        super(parent);
        this.versions = versions;
        super.setTitle("Create new Version for Dataset: " + dataset.getName());
        super.setModal(true);
        super.setSize(new Dimension(500, 150));
        super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        super.getContentPane().add(createLayout(versions));
    }

    private JPanel createLayout(Version[] versions) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        panel.add(inputPanel(versions));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(Version[] versions) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        versionsModel = new DefaultComboBoxModel(versionNames(versions));
        JComboBox versionsCombo = createVersionsCombo(versionsModel);
        layoutGenerator.addLabelWidgetPair("Base Version", versionsCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String[] versionNames(Version[] versions) {
        List names = new ArrayList();
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                names.add(versions[i].getName());
        }
        return (String[]) names.toArray(new String[0]);
    }

    private JComboBox createVersionsCombo(DefaultComboBoxModel model) {
        JComboBox combo = new JComboBox(model);
        combo.setName("Versions");
        combo.setEditable(false);
        combo.setPreferredSize(new Dimension(300, 20));
        
        return combo;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("Ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = true;
                close();
            }
        });
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

    protected void close() {
        super.dispose();
    }

    public void run() {
        show();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public Version version() {
        return lookupVersion((String) versionsModel.getSelectedItem());
    }

    private Version lookupVersion(String name) {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].getName().equals(name))
                return versions[i];
        }

        return null;
    }

    public String name() {
        return name.getText();
    }

}
