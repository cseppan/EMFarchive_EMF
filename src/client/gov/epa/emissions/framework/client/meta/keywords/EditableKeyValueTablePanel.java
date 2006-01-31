package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.framework.client.data.EditableTablePanel;
import gov.epa.emissions.framework.ui.TableData;

import javax.swing.JScrollPane;

public class EditableKeyValueTablePanel extends EditableTablePanel {

    public EditableKeyValueTablePanel(String label, EditableKeyValueTableData tableData) {
        super(label, tableData);
    }
    
    protected JScrollPane table(TableData tableData) {
        tableModel = new KeywordTableModel((EditableKeyValueTableData) tableData);
        table = new EditableTable(tableModel);

        return new JScrollPane(table);
    }


}
