package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewVersionDialog extends Dialog {

    private TextField name;

    private DefaultComboBoxModel versionsModel;

    protected boolean shouldCreate;

    private VersionsSet versionsSet;

    public NewVersionDialog(EmfDataset dataset, Version[] versions, EmfConsole parent) {
        super("Create a new Version of " + dataset.getName(), parent);
        versionsSet = new VersionsSet(versions);
        super.setSize(new Dimension(450, 150));

        super.getContentPane().add(createLayout(versions));
        super.center();
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

        name = new TextField("", 30);
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
        VersionsSet set = new VersionsSet(versions);
        return set.nameAndNumbersOfFinalVersions();
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
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (verifyInput()) {
                    shouldCreate = true;
                    close();
                }
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

    protected boolean verifyInput() {
        String newName = name().trim();
        if (newName.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (newName.contains("(") || newName.contains(")"))
        {
            JOptionPane.showMessageDialog(super.getParent(), 
                   "Please enter a name that does not contain parentheses", 
                  "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (isDuplicate(newName)) {
            JOptionPane.showMessageDialog(super.getParent(), 
                    "Please enter a unique 'name'", "Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean isDuplicate(String value) {
        return versionsSet.contains(value);
    }

    protected void close() {
        super.dispose();
    }

    public void run() {
        super.display();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public Version version() {
        return versionsSet.getVersionFromNameAndNumber((String) versionsModel.getSelectedItem());
    }

    public String name() {
        return name.getText();
    }

}
