package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewRegionPanel extends JPanel implements NewRegionPanelView {
    private TextField name;
    private TextField datasetLabel;
    private TextField description;
    private Button selectButton;
    private MessagePanel messagePanel;
    //private EmfConsole parentConsole;  
    //private EmfSession session;
    private ManageChangeables changeablesList;
    
    private Dimension preferredSize = new Dimension(480, 20);
    
    public NewRegionPanel(MessagePanel messagePanel, ManageChangeables changeablesList, 
            EmfConsole parentConsole, EmfSession session){
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        //this.parentConsole = parentConsole;
      //this.session = session;
    }
    
    public void display (JComponent container){
        
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        name = new TextField("name", 43);
        name.setPreferredSize(preferredSize);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Output Name:", name, panel);
        
        description = new TextField("description", 43);
        description.setPreferredSize(preferredSize);
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Output Name:", description, panel);
    
        layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }
    
    public JPanel datasetPanel() {

        datasetLabel = new TextField("dataset", 38);
        datasetLabel.setEditable(false);
        //datasetLabel.setText( getDatasetProperty("name"));
        changeablesList.addChangeable(datasetLabel);
        datasetLabel.setToolTipText("Press select button to choose from a dataset list.");
        selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5, 0));

        invPanel.add(datasetLabel, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }

    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    messagePanel.clear();
                    //doAddWindow();
                } catch (Exception e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }


    public GeoRegion setFields() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("under construnciton");
    }

    public void validateFields() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("under construnciton");
    }
    
//    private void doAddWindow() throws Exception {
//        DatasetType[] dsTypeArray = presenter.getDSTypes();
//        DatasetType type = presenter.getDatasetType(dsTypeLabel.getText());
//        //DatasetType[] datasetTypes = new DatasetType[] { type };
//        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole);
//        InputDatasetSelectionPresenter datasePresenter = new InputDatasetSelectionPresenter(view, session, dsTypeArray);
//        if (type != null)
//            datasePresenter.display(type, true);
//        else
//            datasePresenter.display(null, true);
//
//        setDatasets(datasePresenter.getDatasets());
//    }
}
