package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class OutputFieldsPanel extends JPanel implements OutputFieldsPanelView {

    private TextField outputName;
    
    private ComboBox jobCombo;

    private ComboBox dsTypeCombo;

    private ComboBox datasetCombo;
    
    private JLabel sector;
    
    private JLabel datasetCreationDate, datasetCreater; 
    
    private JLabel program;

    private JLabel status;

    private TextArea message;

//    private CheckBox required;
//
    private String[] datasetValues; 

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private OutputFieldsPanelPresenter presenter;

    private CaseOutput output;

    public OutputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }

    public void display(CaseOutput output, JComponent container) throws EmfException {
        this.output = output;
        this.datasetValues = presenter.getDatasetValues();
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(150);

        outputName = new TextField("name", output.getName(), 40);
        outputName.setEditable(true);
//        outputName.setMaximumSize(new Dimension(200, 20));
        changeablesList.addChangeable(outputName);
        layoutGenerator.addLabelWidgetPair("Output Name:", outputName, panel);
        CaseJob[] jobArray = presenter.getCaseJobs();
        jobCombo= new ComboBox(jobArray);
        jobCombo.setSelectedIndex(presenter.getJobIndex(output.getJobId(), jobArray));
        changeablesList.addChangeable(jobCombo);
        jobCombo.setPrototypeDisplayValue(width);
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                updateSectorName((CaseJob)jobCombo.getSelectedItem());
            }
        });
        layoutGenerator.addLabelWidgetPair("Job:", jobCombo, panel);

        DatasetType[] dsTypeArray = presenter.getDSTypes();
        dsTypeCombo = new ComboBox(dsTypeArray);
        String datasetType=getDatasetProperty("datasetType");
        DatasetType type = presenter.getDatasetType(datasetType);
        dsTypeCombo.setSelectedItem(type);
        dsTypeCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                fillDatasets((DatasetType) dsTypeCombo.getSelectedItem());
                datasetLabels(null);
            }
        });
        changeablesList.addChangeable(dsTypeCombo);
        dsTypeCombo.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsTypeCombo, panel);

        // fill in dataset
        EmfDataset[] datasets=presenter.getDatasets(type);
        datasetCombo = new ComboBox(new EmfDataset[0]);
        String dsName = getDatasetProperty("name");
        fillDatasets(type);
        if (type !=null && !dsName.trim().isEmpty())
            datasetCombo.setSelectedIndex(presenter.getDatasetIndex(dsName, datasets));
        datasetCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                datasetLabels((EmfDataset) datasetCombo.getSelectedItem());
            }
        });
        changeablesList.addChangeable(datasetCombo);
        datasetCombo.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset:", datasetCombo, panel);
        
        sector=new JLabel("");
        datasetCreationDate=new JLabel("");
        datasetCreater=new JLabel("");
        program=new JLabel("");
        status = new JLabel("");
        updateSectorName((CaseJob)jobCombo.getSelectedItem());
        outputLabels(); 
        if(type==null) 
            datasetLabels(null);
        else 
            datasetLabels((EmfDataset) datasetCombo.getSelectedItem());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        layoutGenerator.addLabelWidgetPair("Creator:", datasetCreater, panel);
        layoutGenerator.addLabelWidgetPair("Creation Date:", datasetCreationDate, panel);
        layoutGenerator.addLabelWidgetPair("Exec name:", program, panel);
        layoutGenerator.addLabelWidgetPair("Status:", status, panel);
        layoutGenerator.addLabelWidgetPair("Message:", message(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }

    private void datasetLabels(EmfDataset selectedItem) {
        String dateText="";
        String createrText="";
        if (selectedItem !=null ){
            datasetValues=presenter.getDatasetValues(new Integer(selectedItem.getId()));
            dateText=getDatasetProperty("createdDateTime").substring(0, 16);
            createrText=getDatasetProperty("creator");
        }
        datasetCreationDate.setText(dateText);
        datasetCreater.setText(createrText);
    }
    
    private void outputLabels() {
        String programText="";
        String statusText ="";
        if (output !=null ){
            programText=output.getExecName();
            statusText=output.getStatus();
        }
        program.setText(programText);
        status.setText(statusText);

    }
    
    protected void updateSectorName(CaseJob job) {
        String sectorText="";
        Sector sector1=job.getSector();
        if (sector1!=null)
            sectorText=sector1.getName();
        
        sector.setText(sectorText);
    }
    
    private ScrollableComponent message() {
        message = new TextArea("message", output.getMessage(), 40, 3 );
        changeablesList.addChangeable(message);
        //description.setPreferredSize(new Dimension(200, 60));

        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        descScrollableTextArea.setMinimumSize(new Dimension(200, 20));
        return descScrollableTextArea;
    }
    

    private void fillDatasets( DatasetType type) {
        try {
            List list = new ArrayList();
            EmfDataset blank = new EmfDataset();
            blank.setName("Not selected");
            list.add(blank);
            list.addAll(Arrays.asList(presenter.getDatasets(type)));
            EmfDataset[] datasets = (EmfDataset[]) list.toArray(new EmfDataset[0]);

            datasetCombo.removeAllItems();
            datasetCombo.setModel(new DefaultComboBoxModel(datasets));
            datasetCombo.revalidate();
            datasetCombo.setEnabled(true);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

 
    public CaseOutput setFields() {
        updateOutputName();
        updateJob();
        updateDatasetType();
        updateDataset();
        updateMessage();
        return output;
    }

    private void updateDatasetType() {
         output.setDatasetType(dsTypeCombo.getSelectedItem().toString());
    }
    
   private void updateJob() {
        CaseJob job = (CaseJob) jobCombo.getSelectedItem();
        if (job==null || job.getName().equalsIgnoreCase(OutputFieldsPanelPresenter.ALL_FOR_SECTOR)) {
            output.setJobId(0);
            return;
        }
        output.setJobId(job.getId());
    }

    private void updateOutputName() {
        output.setName(outputName.getText().trim());
    }
    
    private void updateMessage() {
        output.setMessage(message.getText());
    }
    
    private void updateDataset() {
        EmfDataset selected = (EmfDataset) datasetCombo.getSelectedItem();
        if (selected != null && selected.getName().equalsIgnoreCase("Not selected")) {
            output.setDatasetId(0);
            return;
        }

        output.setDatasetId(selected.getId());
    }

    public void observe(OutputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public CaseOutput getOutput() {
        return this.output;
    }

    public void validateFields() throws EmfException {
        if (this.dsTypeCombo.getSelectedItem() == null)
            throw new EmfException("Please select a dataset type for the output");
        if (outputName.getText().trim().equalsIgnoreCase(""))
            throw new EmfException("Please specify an output name.");
        if (((CaseJob)jobCombo.getSelectedItem()).getId()==0)
            throw new EmfException("Please choose a valid job.");
    }

    private String getDatasetProperty(String property) {
        if (datasetValues == null)
            return null;
        
        String value = null;

        for (String values : datasetValues) {
            if (values.startsWith(property))
                value = values.substring(values.indexOf(",") + 1);
        }
        return value;
    }
}
