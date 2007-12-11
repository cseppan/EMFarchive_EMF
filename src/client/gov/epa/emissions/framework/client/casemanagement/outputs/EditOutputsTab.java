package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditOutputsTab extends JPanel implements EditOutputsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditOutputsTabPresenter presenter;
    
    private MessagePanel messagePanel;

    private OutputsTableData tableData;

    private ManageChangeables changeables;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;
    
    private Case caseObj;
    
    private EmfSession session; 
    
    private ComboBox jobCombo;
    
    private List<CaseJob> caseJobs; 
    
    private CaseJob selectedJob=null;
    
    private DesktopManager desktopManager;


    public EditOutputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editOutputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.session=session; 
        this.desktopManager=desktopManager;
        this.messagePanel=messagePanel;
 
        super.setLayout(new BorderLayout());
    }

//    public void observe(EditOutputsTabPresenter presenter) {
//        this.presenter = presenter;
//    }
    
    public void display() {
        super.removeAll();
        CaseOutput[] outputs = new CaseOutput[0];
        try {
            getAllJobs();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        super.add(createLayout(outputs), BorderLayout.CENTER);
    }

    private void doRefresh(CaseOutput[] outputs){
        messagePanel.clear();
        selectedJob=(CaseJob) jobCombo.getSelectedItem();
        super.removeAll();
        super.add(createLayout(outputs), BorderLayout.CENTER);
        super.revalidate();
    }

    private JPanel createLayout(CaseOutput[] outputs){
        JPanel layout = new JPanel(new BorderLayout());
        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(outputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);
        return layout;
    }
    
    private void getAllJobs() throws EmfException {
        this.caseJobs = new ArrayList<CaseJob>();
        caseJobs.add(new CaseJob("All"));
        caseJobs.addAll(Arrays.asList(presenter.getCaseJobs()));
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        jobCombo=new ComboBox("Select One", caseJobs.toArray(new CaseJob[0]));
        jobCombo.setPreferredSize(new Dimension(300,20));
        if (selectedJob!=null)
            jobCombo.setSelectedItem(selectedJob);
            
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                CaseJob job=(CaseJob) jobCombo.getSelectedItem();
                
                try {
                    if (job == null){
                        doRefresh(new CaseOutput[0]);
                        return; 
                    }
                    CaseOutput[] outputs=presenter.getCaseOutputs(caseObj.getId(),job.getId());
                    doRefresh(outputs);
                } catch (EmfException exc) {
                    messagePanel.setError("Could not retrieve all outputs for job " + (job != null ? job.getName() : job) + ".");
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Job: ", jobCombo, panel);
layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
        150, 15, // initialX, initialY
        5, 15);// xPad, yPad
        return panel;
    }

    private JPanel tablePanel(CaseOutput[] outputs, EmfConsole parentConsole){
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

    private JPanel controlPanel() {
        JPanel container = new JPanel();
        Insets insets = new Insets(1, 2, 1, 2);
        
//        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
//        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        add.setMargin(insets);
        add.setEnabled(false);
        container.add(add);
        
        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    removeSelectedOutput();
                } catch (EmfException e1) {
                    e1.printStackTrace();
                }
            }
        });
        remove.setMargin(insets);
 //       remove.setEnabled(false);
        container.add(remove);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        edit.setMargin(insets);
        edit.setEnabled(false);
        container.add(edit);
        
        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    displayOutputDatasetsPropertiesViewer();
                } catch (EmfException e1) {
                    messagePanel.setMessage(e1.getMessage());
                }
            }
        });
        view.setMargin(insets);
        container.add(view);
        
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

    protected void displayOutputDatasetsPropertiesViewer() throws EmfException {
        messagePanel.clear();
        List selected = selectModel.selected();
        if (selected.size() == 0) {
            throw new EmfException ("Please select one or more outputs to view.");
        }
        for (int i=0; i<selected.size(); i++) {
            CaseOutput output = (CaseOutput) selected.get(i);
            if (output==null){ 
                throw new EmfException("Output is null "); 
            }
            int id=output.getDatasetId();
            EmfDataset dataset = presenter.getDataset(id);
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
            presenter.doDisplay(view);
        }
    }

    private void removeSelectedOutput() throws EmfException {
        messagePanel.clear();
        CaseOutput[] selected =selectModel.selected().toArray(new CaseOutput[0]);

        if (selected.length==0) {
            messagePanel.setMessage("Please select one or more outputs to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected output(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(selected);
            refresh();
            presenter.doRemove(selected);
        }
//         clearMessage();
//        setCursor(Cursor.getDefaultCursor());
//        messagePanel.setMessage("Finished removing outputs.");
    }
    public void refresh(){
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }


    public void observe(EditOutputsTabPresenterImpl presenter) {
        this.presenter = presenter;
        this.caseObj=presenter.getCaseObj();
    }
    
    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveOutputs();
            }
        });
        populateThread.start();
    }
    
    private synchronized void retrieveOutputs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all outputs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if ( selectedJob == null){
                clearMessage();
                setCursor(Cursor.getDefaultCursor());
            }
            else {
                doRefresh(presenter.getCaseOutputs(caseObj.getId(), selectedJob.getId()));
                messagePanel.clear();
                setCursor(Cursor.getDefaultCursor());
            }
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all outputs.");
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void clearMessage() {
        messagePanel.clear();
    }
}
