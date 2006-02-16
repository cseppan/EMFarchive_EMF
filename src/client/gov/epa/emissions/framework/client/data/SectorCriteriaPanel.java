package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.commons.gui.Editor;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class SectorCriteriaPanel extends JPanel implements  Editor,Changeable {
    private ChangeablesList listOfChangeables;
    
    private boolean changed = false;
    
    private EditableTablePanel editableTablePanel;

    public SectorCriteriaPanel(String label, SectorCriteriaTableData tableData) {
        editableTablePanel = new EditableTablePanel(label, tableData);
        editableTablePanel.setColumnEditor(typesCellEditor(), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

    private TableCellEditor typesCellEditor() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("SCC");
        comboBox.addItem("NAICS");
        comboBox.addItem("SIC");
        comboBox.addItem("IPM Flag");// True/False values
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                notifyChanges();
            }
        });

        return new DefaultCellEditor(comboBox);
    }

    public void commit() {
        editableTablePanel.commit();
    }

    public void clear() {
        this.changed = false;
    }
    
    private void notifyChanges() {
        changed = true;
        this.listOfChangeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(ChangeablesList list) {
        this.listOfChangeables = list;
    }
}
