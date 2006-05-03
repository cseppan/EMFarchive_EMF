package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class DefaultVersionPanel extends JPanel {

    private EmfDataset dataset;

    private Version[] versions;

    protected String selected;

    public DefaultVersionPanel(EmfDataset dataset, Version[] versions, ManageChangeables changeables) {
        this.dataset = dataset;
        this.versions = versions;

        createLayout(changeables);
    }

    private void createLayout(ManageChangeables changeables) {
        super.add(new JLabel("Default Version:"));

        ComboBox combo = comboBox(changeables);
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                selected = e.getItem().toString();
            }
        });

        super.add(combo);
    }

    private ComboBox comboBox(ManageChangeables changeables) {
        VersionsSet versionsSet = new VersionsSet(versions);
        selected = getDefaultVersion(versionsSet);

        ComboBox combo = new ComboBox(selected, labels(versionsSet));
        combo.setName("defaultVersions");
        combo.setPreferredSize(new Dimension(175, 20));

        combo.setSelectedItem(selected);
        changeables.addChangeable(combo);

        return combo;
    }

    private String[] labels(VersionsSet versionsSet) {
        return versionsSet.nameAndNumbers();
    }

    private String getDefaultVersion(VersionsSet versionsSet) {
        int ver = dataset.getDefaultVersion();
        return versionsSet.getVersionName(ver) + " (" + ver + ")";
    }

    public void updateDataset() {
        int forPerenth = selected.indexOf('(');
        int backPerenth = selected.indexOf(')');
        
        int version = Integer.parseInt(selected.substring(forPerenth+1, backPerenth));
        dataset.setDefaultVersion(version);
    }

}
