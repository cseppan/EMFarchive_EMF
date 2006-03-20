package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditableQATab extends JPanel implements EditableQATabView {

    private EditableQAStepsPresenter presenter;
    
    private QAService service;
    
    private EmfDataset dataset;
    
    private MessagePanel messagePanel;
    
    private EditableQAStepTableData tableData;
    
    private EditableEmfTableModel tableModel;

    private EditableTable table;
    
    private EmfConsole parent;
    
    private Version[] versions;
    
    ManageChangeables changeablesList;
    
    public EditableQATab(EmfDataset dataset, Version[] versions, QAService service,
            MessagePanel messagePanel, ManageChangeables changeablesList, EmfConsole parent) {
        super.setName("aqsteps");
        this.dataset = dataset;
        this.service = service;
        this.messagePanel = messagePanel;
        this.parent = parent;
        this.versions = versions;
        this.changeablesList = changeablesList;
        
        super.setLayout(new BorderLayout());
        super.add(createQAStepsTableSection(), BorderLayout.PAGE_START);
        super.add(createButtonsSection(), BorderLayout.CENTER);
        super.setSize(new Dimension(700, 300));
    }

    private JPanel createQAStepsTableSection() {
        JPanel container = new JPanel(new BorderLayout());
        container.add(table(), BorderLayout.CENTER);
        return container;
    }
    
    protected JScrollPane table() {
        try {
            tableData = new EditableQAStepTableData(service.getQASteps(dataset));
            tableModel = new EditableEmfTableModel(tableData);
            table = new EditableTable(tableModel);
            table.setRowHeight(16);
            changeablesList.addChangeable(table);
        } catch (EmfException e) {
            messagePanel.setError("Failed to create QAStep table data.");
        }

        return new JScrollPane(table);
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        Button add = new BorderlessButton("Add Existing", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //FIXME: this is only a place holder, remove when ready
                addExisting();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add New", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO
            }
        });
        container.add(remove);

        Button update = new BorderlessButton("Perform", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO
            }
        });
        container.add(update);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void observe(EditableQAStepsPresenter presenter) {
        this.presenter = presenter;
    }

    public void save() {
        // NOTE Auto-generated method stub
        
    }
    
    private void addExisting() {
        presenter.doAdd(new NewQAStepDialog(parent, versions), dataset);
    }

    public void add(QAStep[] steps) {
        for(int i = 0; i < steps.length; i++)
            tableData.add(steps[i]);
        
        refresh();
    }
    
    public void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    public void display(QAStep[] steps) {
        // NOTE Auto-generated method stub
        
    }
    
}
