package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class KeywordsTab extends JPanel implements KeywordsTabView {

    public KeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(KeyVal[] values) {
        JPanel layout = new JPanel(new BorderLayout());
        layout.setBorder(BorderFactory.createTitledBorder("Keywords"));

        KeyValueTableData tableData = new KeyValueTableData(values);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(20);

        layout.add(new JScrollPane(table), BorderLayout.CENTER);

        super.add(layout);
    }

}
