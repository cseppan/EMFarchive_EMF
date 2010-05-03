package gov.epa.emissions.framework.client.sms;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SectorScenarioDialog extends JDialog implements SectorScenarioView {

//    private EmfConsole parent;

    private SectorScenarioPresenter presenter;

    private TextField name; 
    
    private TextArea description; 
    
    private TextField abbreviation; 
    
    private ComboBox eecsMappingDataset;

    private ComboBox eecsMappingDatasetVersion;

    private ComboBox sectorMappingDataset;

    private ComboBox sectorMappingDatasetVersion;

    private JList datasetList;
    
    private EmfSession session;
    
    public SectorScenarioDialog(EmfConsole parent, EmfSession session) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
//        this.parent = parent;
        this.session = session;
        setModal(true);
    }

    public void display() throws EmfException {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        setTitle("Select Inventory Datasets");
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void refreshDatasets(EmfDataset[] datasets) {
        datasetList.setListData(datasets);
    }

    private JPanel buildTopPanel() throws EmfException{
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildNameContains());
        return panel; 
    }

    private JPanel buildNameContains() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Name:", 25);
        layoutGenerator.addLabelWidgetPair("Name: ", name, panel);

        description = new TextArea("Description:", "", 25, 4);
        layoutGenerator.addLabelWidgetPair("Description: ", description, panel);

        abbreviation = new TextField("Abbreviation:", 25);
        layoutGenerator.addLabelWidgetPair("Abbreviation: ", abbreviation, panel);
        
        eecsMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.EECS_MAPPING)));
//        if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

        eecsMappingDataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        eecsMappingDatasetVersion =new ComboBox(new Version[0]);      
//        version.setPrototypeDisplayValue(width);
        try {
            fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
//        if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
     
        layoutGenerator.addLabelWidgetPair("EECS Mapping Dataset:", eecsMappingDataset, panel);
        layoutGenerator.addLabelWidgetPair("EECS Mapping Dataset Version:", eecsMappingDatasetVersion, panel);

        sectorMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.SECTOR_MAPPING)));
//      if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

        sectorMappingDataset.addActionListener(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
              try {
                  fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
              } catch (EmfException e1) {
                  // NOTE Auto-generated catch block
                  e1.printStackTrace();
              }
          }
      });

        sectorMappingDatasetVersion = new ComboBox(new Version[0]);      
//      version.setPrototypeDisplayValue(width);
      try {
          fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
      } catch (EmfException e1) {
          // NOTE Auto-generated catch block
          e1.printStackTrace();
      }
//      if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
   
      layoutGenerator.addLabelWidgetPair("Sector Mapping Dataset:", sectorMappingDataset, panel);
      layoutGenerator.addLabelWidgetPair("Sector Mapping Dataset Version:", sectorMappingDatasetVersion, panel);

        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private void fillVersions(ComboBox version, EmfDataset dataset) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = presenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

    }
    
    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }
 
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                SectorScenario sectorScenario = new SectorScenario();
                sectorScenario.setName(name.getText());
                sectorScenario.setDescription(description.getText());
                sectorScenario.setAbbreviation(abbreviation.getText());
                sectorScenario.setCreator(session.user());
                sectorScenario.setRunStatus("Not started");
                sectorScenario.setLastModifiedDate(new Date());
                sectorScenario.setEecsMapppingDataset((EmfDataset)eecsMappingDataset.getSelectedItem());
                sectorScenario.setEecsMapppingDatasetVersion(0);
                sectorScenario.setSectorMapppingDataset((EmfDataset)sectorMappingDataset.getSelectedItem());
                sectorScenario.setSectorMapppingDatasetVersion(0);

                try {
                    presenter.addSectorScenario(sectorScenario);
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
                
                setVisible(false);
                dispose();
            }
        };
    }

    public void observe(SectorScenarioPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }


}