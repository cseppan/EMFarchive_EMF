package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

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
        super.add(new JLabel("Default Version"));

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
        String defaultValue = getDefaultVersion(versionsSet);

        ComboBox combo = new ComboBox(defaultValue, labels(versionsSet));
        combo.setName("defaultVersions");
        combo.setPreferredSize(new Dimension(175, 20));

        combo.setSelectedItem(defaultValue);
        changeables.addChangeable(combo);

        return combo;
    }

    private String[] labels(VersionsSet versionsSet) {
        String[] names = versionsSet.names();
        Integer[] numbers = versionsSet.versions();

        List labels = new ArrayList();
        for (int i = 0; i < names.length; i++) {
            String version = displayableVersion(names[i], numbers[i].intValue());
            labels.add(version);
        }

        return (String[]) labels.toArray(new String[0]);
    }

    private String getDefaultVersion(VersionsSet versionsSet) {
        String name = versionsSet.getVersionName(dataset.getDefaultVersion());
        return displayableVersion(name, dataset.getDefaultVersion());
    }

    private String displayableVersion(String name, int version) {
        return version + " - " + name;
    }

    public void updateDataset() {
        int version = Integer.parseInt(selected.split("-")[0].trim());
        dataset.setDefaultVersion(version);
    }

}
