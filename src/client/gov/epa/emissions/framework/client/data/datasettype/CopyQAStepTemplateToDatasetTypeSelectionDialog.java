package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class CopyQAStepTemplateToDatasetTypeSelectionDialog extends JDialog implements CopyQAStepTemplateToDatasetTypeSelectionView {

//    private EmfConsole parent;

    protected CopyQAStepTemplateToDatasetTypeSelectionPresenter presenter;

    private JCheckBox replaceCheckBox; 
    
    private JList datasetTypeList;
    
    private DatasetType[] datasetTypes = new DatasetType[] {};
    
    public CopyQAStepTemplateToDatasetTypeSelectionDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
//        this.parent = parent;
        setModal(true);
    }

    public void display(DatasetType[] datasetTypes) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildTopPanel(datasetTypes), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        setTitle("Select Dataset Types");
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public DatasetType[] getSelectedDatasetTypes() {
        return datasetTypes;
    }
    
    public boolean shouldReplace() {
        return replaceCheckBox.isSelected();
    }
    
    private JPanel buildTopPanel(DatasetType[] datasetTypes){
        JPanel panel = new JPanel ();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildReplacePanel());
        panel.add(buildSelectionPanel(datasetTypes));
        return panel; 
    }
    
    private JPanel buildReplacePanel(){
        JPanel panel = new JPanel(new SpringLayout()); 
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        replaceCheckBox = new JCheckBox("", false);
        
        layoutGenerator.addLabelWidgetPair("Replace QA Step Template?  ", replaceCheckBox, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                25, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private JPanel buildSelectionPanel(DatasetType[] datasetTypes) {
        datasetTypeList = new JList();
        datasetTypeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        datasetTypeList.setListData(datasetTypes);
        JScrollPane scrollPane = new JScrollPane(datasetTypeList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
        return panel;
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
                if (datasetTypeList.getSelectedValues() == null || datasetTypeList.getSelectedValues().length == 0) 
                    datasetTypes = new DatasetType[]{}; 
                else {
                    // get selected datasets
                    List<DatasetType> list = new ArrayList<DatasetType>(datasetTypeList.getSelectedValues().length);
                    for (int i = 0; i < datasetTypeList.getSelectedValues().length; i++)
                        list.add((DatasetType) datasetTypeList.getSelectedValues()[i]);
                    datasetTypes = list.toArray(new DatasetType[0]);
                }
                setVisible(false);
                dispose();
            }
        };
    }

    public void observe(CopyQAStepTemplateToDatasetTypeSelectionPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void clearMessage() {
        // NOTE Auto-generated method stub
        
    }


}