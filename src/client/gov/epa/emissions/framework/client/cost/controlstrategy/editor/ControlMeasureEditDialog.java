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
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlMeasureEditDialog extends JDialog implements ControlMeasureEditView {

//    private EmfConsole parent;

    private ControlMeasureEditPresenter presenter;
    
    private SingleLineMessagePanel messagePanel;
    
 //   private ManageChangeables changeables;  
    
    private DoubleTextField rule, rPenetration, rEffective;
    private NumberFieldVerifier verifier;
    
    private ComboBox version, dataset;

    public ControlMeasureEditDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.verifier= new NumberFieldVerifier("Measure properties: ");
//        this.parent = parent;
    }

    public void display() {
        
        messagePanel = new SingleLineMessagePanel();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
        contentPane.add(messagePanel, BorderLayout.PAGE_START);
  
        try {
            contentPane.add(createLowerSection(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        setTitle("Control Strategy Measures Editor");
        this.pack();
        this.setSize(700,220);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
   
    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;  
    }
    
    private JPanel createPropertySection() throws EmfException{
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySectionRight(), BorderLayout.CENTER);
        panel.add(createPropertySectionLeft(), BorderLayout.WEST);
        return panel;
    }
    
    private JPanel createPropertySectionRight() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Regions"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
            EmfDataset[] datasets = presenter.getDatasets( presenter.getDatasetType("List of Counties (CSV)") );         
            dataset = new ComboBox(new EmfDataset[0]);
            Dimension size= new Dimension(300, 10);
            dataset.setPreferredSize(size);
            fillDatasets(datasets);

            dataset.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        fillVersions((EmfDataset) dataset.getSelectedItem());
                    } catch (EmfException e1) {
                        // NOTE Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            });
            layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

            version =new ComboBox(new Version[0]);           
            version.setPreferredSize(size);
            try {
                fillVersions((EmfDataset) dataset.getSelectedItem());
            } catch (EmfException e1) {
                // NOTE Auto-generated catch block
                e1.printStackTrace();
            }
            
            layoutGenerator.addLabelWidgetPair("Version:", version, panel);
            layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                    5, 5, // initialX, initialY
                    5, 10);// xPad, yPad

        return panel;
    }

    private void fillDatasets(EmfDataset[] ds) {
        List list = new ArrayList();
        EmfDataset blank = new EmfDataset();
        blank.setName("Not selected");
        list.add(blank);
        list.addAll(Arrays.asList(ds));
        EmfDataset[] datasets = (EmfDataset[]) list.toArray(new EmfDataset[0]);

        dataset.removeAllItems();
        dataset.setModel(new DefaultComboBoxModel(datasets));
        dataset.revalidate();
        dataset.setEnabled(true);
    }
    
    private void fillVersions(EmfDataset dataset) throws EmfException{
        version.setEnabled(true);

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
 

    private JPanel createPropertySectionLeft() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Measure Properties"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Set Order:", ruleField(), panel);
        layoutGenerator.addLabelWidgetPair("Set RP %:", rPField(), panel);
        layoutGenerator.addLabelWidgetPair("Set RE %:", rEField(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return panel;
    }
    private DoubleTextField ruleField() {
        rule = new DoubleTextField("Set Order", 1, 100, 10);
        rule.setText("");
        return rule;
    }
    
    private DoubleTextField rPField() {
        rPenetration = new DoubleTextField("Set RP %", 1, 100, 10);
        rPenetration.setText("");
        return rPenetration;
    }
    
    private DoubleTextField rEField() {
        rEffective = new DoubleTextField("Set RE %", 1, 100, 10);
        rEffective.setText("");
        return rEffective;
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
                try {
                    add();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    messagePanel.setError(e1.getMessage());
                }           
            }
        };
    }

    private void add() throws EmfException {
        messagePanel.clear();
        EmfDataset ds =(EmfDataset) dataset.getSelectedItem();
        if (ds.getId() == 0) {
            ds = null;
        }
        Version ver = (ds !=null ? (Version) version.getSelectedItem(): null);
        Integer verValue = (ver !=null? ver.getVersion(): null);
        presenter.doAdd(checkNumber(rule), checkNumber(rPenetration), checkNumber(rEffective), ds, verValue);
        setVisible(false);
        dispose();
    }

    private Double checkNumber(DoubleTextField value) throws EmfException{
        if (value.getText().trim().length() == 0){
            return Double.NaN;
        }
        Double value1 = verifier.parseDouble(value);

        // make sure the number makes sense...
        if (value1 < 1 || value1 > 100) {
            throw new EmfException(value.getName()+":  Enter a number between 1 and 100");
        }
        return value1;

    }

    public void observe(ControlMeasureEditPresenter presenter) {
        this.presenter = presenter;
    }

}
