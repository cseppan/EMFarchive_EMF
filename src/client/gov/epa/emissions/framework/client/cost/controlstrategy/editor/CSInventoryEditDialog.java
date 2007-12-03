package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class CSInventoryEditDialog extends JDialog {

    private EditControlStrategyPresenter presenter;
    
    private EmfDataset dataset;
 
    private ComboBox versionCombo;
    
    private Version version;

    private EditControlStrategyInventoryFilterTab view;

    public CSInventoryEditDialog(EmfConsole parent, EmfDataset dataset,
            EditControlStrategyPresenter presenter,
            EditControlStrategyInventoryFilterTab view) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.dataset = dataset;
        this.presenter=presenter; 
        this.view=view;
    }

    public void run() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
  
        try {
            contentPane.add(createSection(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        setTitle("Set Version: " + dataset.getName());
        this.pack();
        this.setSize(300,120);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
   
    private JPanel createSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;  
    }
    
    private JPanel createPropertySection() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout());
 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
 
            versionCombo =new ComboBox(new Version[0]);           
            versionCombo.setSize(300,15);
            fillVersions(dataset);
 
            layoutGenerator.addLabelWidgetPair("Version:", versionCombo, panel);
            layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                    55, 5, // initialX, initialY
                    5, 10);// xPad, yPad

        return panel;
    }

    
    private void fillVersions(EmfDataset dataset) throws EmfException{
        versionCombo.setEnabled(true);

        Version[] versions = presenter.getVersions(dataset);
        versionCombo.removeAllItems();
        versionCombo.setModel(new DefaultComboBoxModel(versions));
        versionCombo.revalidate();
        if (versions.length > 0)
            versionCombo.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

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
            public void actionPerformed(ActionEvent e){            
                version = (dataset !=null ? (Version) versionCombo.getSelectedItem(): null);
                view.editVersion(version, dataset);
                dispose();
            }
        };
    }

}
