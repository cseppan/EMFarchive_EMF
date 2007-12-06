package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditOutputsTab extends JPanel implements EditOutputsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditOutputsTabPresenter presenter;

    private OutputsTableData tableData;

    private ManageChangeables changeables;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;
    
    private EmfSession session; 
    
//    private ComboBox jobCombo;

    public EditOutputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editOutputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.session=session; 

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditOutputsTabPresenter presenter, EmfSession session) {
        super.removeAll();
        CaseOutput[] outputs = new CaseOutput[0];
        try {
            outputs = presenter.getCaseOutputs(caseObj.getId(), 0);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        try {
            super.add(createLayout(outputs, presenter, parentConsole), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        this.presenter = presenter;
    }

    private void doRefresh(CaseOutput[] outputs) {
        super.removeAll();
        try {
            super.add(createLayout(outputs, presenter, parentConsole), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    private JPanel createLayout(CaseOutput[] outputs, EditOutputsTabPresenter presenter, EmfConsole parentConsole) throws EmfException {
        JPanel layout = new JPanel(new BorderLayout());
//        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(outputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

//    private JPanel createTopPanel() throws EmfException {
//        JPanel panel = new JPanel(new SpringLayout());
//        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
//        CaseJob[] caseJobs=presenter.getCaseJobs();
////        CaseJob[] caseJobs=presenter.getCaseOutputs(0, 0);
//        jobCombo=new ComboBox("Select One", caseJobs);  
//        layoutGenerator.addLabelWidgetPair("Jobs: ", jobCombo, panel);
//layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
//        5, 5, // initialX, initialY
//        5, 5);// xPad, yPad
//        return null;
//    }

    private JPanel tablePanel(CaseOutput[] outputs, EmfConsole parentConsole) throws EmfException {
        tableData = new OutputsTableData(outputs, session);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final EditOutputsTabPresenter presenter) {
        JPanel container = new JPanel();
        Insets insets = new Insets(1, 2, 1, 2);

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        add.setMargin(insets);
        add.setEnabled(false);
        container.add(add);

        Button Edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        Edit.setMargin(insets);
        Edit.setEnabled(false);
        container.add(Edit);
        
        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        view.setMargin(insets);
        view.setEnabled(false);
        container.add(view);
        
        Button remove = new RemoveButton("Delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        remove.setMargin(insets);
        remove.setEnabled(false);
        container.add(remove);
        
        Button export = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        export.setMargin(insets);
        export.setEnabled(false);
        container.add(export);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

    public void doRefresh() throws EmfException {
        if (false)
            throw new EmfException("under construction...");
    }

}
