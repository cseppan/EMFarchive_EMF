package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class SetInputFieldsPanel extends JPanel implements InputFieldsPanelView, ManageChangeables {

    private ListWidget dataset;

    private ComboBox version;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private InputFieldsPanelPresenter presenter;

    private CaseInput input;
    
    private Dimension preferredSize = new Dimension(380, 20);
    
    private EmfSession session;
    
    private EmfConsole parentConsole;

    public SetInputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfSession session, EmfConsole parentConsole) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.session = session; 
        this.parentConsole = parentConsole;
    }

    public void display(CaseInput input, JComponent container) throws EmfException {
        this.input = input;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(125);
        JLabel inputName = new JLabel(input.getInputName().toString());
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

//        JLabel program = new JLabel(input.getProgram().toString());
//        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        JLabel envtVar = new JLabel(input.getEnvtVars()==null? "":input.getEnvtVars().toString());
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        JLabel sector = new JLabel(input.getSector()==null? "All jobs for sector" :input.getSector().toString());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        
        JLabel jobName = new JLabel(presenter.getJobName(input.getCaseJobID()));
        layoutGenerator.addLabelWidgetPair("Job:", jobName, panel);
        
        JLabel dsType = new JLabel(input.getDatasetType()==null? "":input.getDatasetType().toString());
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);
        
        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panel);

        version = new ComboBox(new Version[] { input.getVersion() });
        fillVersions(input.getDataset());
        
        if (input.getVersion() != null)
            version.setSelectedItem(input.getVersion());
        
        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);
        

        JLabel required = new JLabel(input.isRequired()? "True" : "False" );
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }

    private JPanel datasetPanel() {

        dataset = new ListWidget(new EmfDataset[0]);
        if(input.getDataset() != null )
            dataset.add(dataset);

        changeablesList.addChangeable(dataset);
        JScrollPane pane = new JScrollPane(dataset);
        pane.setPreferredSize(new Dimension(330, 20));
        dataset.setToolTipText("Press select button to choose from a dataset list.");

        Button selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(pane, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }
    
    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow();
            }
        };
    }

    protected void doAddWindow() {

        try {
            DatasetType type = input.getDatasetType();
            DatasetType[] datasetTypes = new DatasetType[]{type};
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole, this);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
            if (datasetTypes.length == 1)
                presenter.display(datasetTypes[0]);
            else
                presenter.display(null);
            setDatasets(presenter.getDatasets());
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void setDatasets(EmfDataset [] datasets) {
        dataset.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
            dataset.addElement(datasets[i]);
        }
        fillVersions(datasets[0]);
    }


    private void fillVersions(EmfDataset dataset) {
        version.setEnabled(true);
        try {
            Version[] versions = presenter.getVersions(dataset);
            version.removeAllItems();
            version.setModel(new DefaultComboBoxModel(versions));
            version.revalidate();
            if (versions.length > 0)
                version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    public CaseInput setFields() throws EmfException {
        updateDataset();
        updateVersion();
        input.setLastModifiedDate(new Date());
        return input;
    }


    private void updateDataset() {
        EmfDataset selected = (EmfDataset) dataset.getAllElements()[0];
        if (selected != null && selected.getName().equalsIgnoreCase("Not selected")) {
            input.setDataset(null);
            return;
        }

        input.setDataset(selected);
    }

    private void updateVersion() throws EmfException {
        EmfDataset ds = (EmfDataset) dataset.getAllElements()[0];
        Version ver = (Version) version.getSelectedItem();

        if (ds == null || ds.getName().equalsIgnoreCase("Not selected")) {
            input.setVersion(null);
            return;
        }

        String type = ds.getDatasetType().getName();
        if (ds.getName() != null && ver == null && type.indexOf("External") < 0)
            throw new EmfException("Please select a dataset version.");

        input.setVersion(ver);
    }

    public void observe(InputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public CaseInput getInput() {
        return this.input;
    }

    public void validateFields() {
        // NOTE Auto-generated method stub
        
    }

    public void addChangeable(ManageChangeables changeable) {
        //
    }

    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
        
    }

//    public void validateFields() {
//        setFields();
//    }
    
}
