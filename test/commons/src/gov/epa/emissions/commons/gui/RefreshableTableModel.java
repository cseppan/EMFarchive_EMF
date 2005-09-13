package gov.epa.emissions.commons.gui;

import javax.swing.table.TableModel;

public interface RefreshableTableModel extends TableModel {
    public void refresh();
}
