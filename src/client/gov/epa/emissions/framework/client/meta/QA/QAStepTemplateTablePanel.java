package gov.epa.emissions.framework.client.meta.QA;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.EditableTablePanel;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

public class QAStepTemplateTablePanel extends EditableTablePanel {
    public QAStepTemplateTablePanel(String label, EditableQAStepTemplateTableData tableData, ManageChangeables changeablesList) {
        super(label, tableData, changeablesList);
    }
    
    public JPanel doLayout(String label, InlineEditableTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(labelPanel(label), BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);
        container.add(buttonsPanel(tableData), BorderLayout.PAGE_END);

        return container;
    }
    
    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }
    
    private JPanel buttonsPanel(final InlineEditableTableData tableData) {
        JPanel container = new JPanel();

        JButton add = new JButton("Add");
        add.setMargin(new Insets(2, 2, 2, 2));
        add.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.addBlankRow();
                refresh();
            }
        });
        container.add(add);

        JButton remove = new JButton("Remove");
        remove.setMargin(new Insets(2, 2, 2, 2));
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.removeSelected();
                refresh();
            }
        });
        container.add(remove);

        JButton update = new JButton("Update");
        remove.setMargin(new Insets(2, 2, 2, 2));
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.removeSelected();
                refresh();
            }
        });
        container.add(update);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }
    
    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

}
