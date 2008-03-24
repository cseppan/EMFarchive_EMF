package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class AppendDataWindow extends ReusableInteralFrame implements AppendDataWindowView {

    private AppendDataViewPresenter presenter;
    
    private SingleLineMessagePanel messagePanel;

    private EmfConsole parentConsole;

    private JPanel layout;

    private EmfDataset sourceDataset;

    private ComboBox sourceVersionBox;

    private JTextField sourceDatasetField;

    private JTextField sourceFilterField;

    private JTextField startLineField;
    
    private JTextField endLineField;

    private ComboBox targetDatasetVerison;
    
    private JCheckBox deleteDSCheckBox;

    public AppendDataWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Append Data Window", new Dimension(700, 450), desktopManager);

        this.parentConsole = parentConsole;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        setTitle("Append Data into Dataset: " + presenter.getDataset().getName());
        setName("Append Data into Dataset: " + presenter.getDataset().getId());
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();

        layout.add(messagePanel);
        layout.add(sourcePanel());
        layout.add(targePanel());
        layout.add(revisionPanel());
        layout.add(createButtonPanel());

        super.display();
    }

    private Component createUpperPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        //panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Source Dataset", 0, 0, Font.decode(""), Color.BLUE));

        sourceDatasetField = new JTextField(40);
        sourceDatasetField.setName("sourceDataset");
        Button setButton = new Button("Set", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    selectSourceDataset();
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                }
            }
        });

        JPanel sourceDatasetPanel = new JPanel(new BorderLayout(2, 0));
        sourceDatasetPanel.add(sourceDatasetField, BorderLayout.LINE_START);
        sourceDatasetPanel.add(setButton, BorderLayout.CENTER);
        //sourceDatasetPanel.add(view, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Dataset Name", sourceDatasetPanel, panel);

        sourceVersionBox = new ComboBox("Select a version", new Version[0]);
        layoutGenerator.addLabelWidgetPair("Source Version", sourceVersionBox, panel);

        sourceFilterField = new JTextField(40);
        layoutGenerator.addLabelWidgetPair("Data Filter", sourceFilterField, panel);
        
        deleteDSCheckBox = new JCheckBox();
        deleteDSCheckBox.setSelected(false);
        layoutGenerator.addLabelWidgetPair("Delete after append?", deleteDSCheckBox, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    private JPanel sourcePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Source Dataset", 0, 0, Font.decode(""), Color.BLUE));

        panel.add(createUpperPanel(), BorderLayout.CENTER);
        panel.add(viewButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }
    
    private Component viewButtonPanel(){
        JPanel panel = new JPanel();
        Button view = new Button("View Dataset", new AbstractAction() {
            //Button button = new BrowseButton(new AbstractAction() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        viewSourceDataset();
                    } catch (Exception e) {
                        setErrorMsg(e.getMessage());
                    }
                }
            });
        //view.setEnabled(false);
        panel.add(view);
        return  panel; 
    }
    
    private Component targePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Target Dataset", 0, 0, Font.decode(""), Color.BLUE));

        panel.add(linePanel(), BorderLayout.NORTH);
        
        JPanel pairPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        //pairPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Target Dataset", 0, 0, Font.decode(""), Color.BLUE));

        Version[] versions = null;
        try {
            versions = presenter.getTargetDatasetNonFinalVersions();
        } catch (EmfException e) {
            setErrorMsg(e.getMessage());
        }
        
        targetDatasetVerison = new ComboBox("Select a version", versions);
        layoutGenerator.addLabelWidgetPair("Nonfinal Version  ", targetDatasetVerison, pairPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(pairPanel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        
        panel.add(pairPanel, BorderLayout.CENTER);
        return panel;
    }
    // top part of target dataset
    private JPanel linePanel(){
        JPanel panel = new JPanel(new BorderLayout(30, 0));
        panel.add(createStartLinePanel(), BorderLayout.LINE_START);
        panel.add(createEndLinePanel(), BorderLayout.CENTER);
        return panel; 
    }
    
    private JPanel createStartLinePanel(){
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        startLineField = new JTextField(15);
        startLineField.setName("startLineField");
        //startLineField.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Starting Line Number", startLineField, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }     
    private JPanel createEndLinePanel(){ 
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        endLineField = new JTextField(15);
        endLineField.setName("endLineField");
        //endLineField.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Ending Line Number", endLineField, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 
    }
    
    private JPanel revisionPanel(){
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Revision Information", 0, 0, Font.decode(""), Color.BLUE));

        TextArea what = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("What was added", ScrollableComponent.createWithVerticalScrollBar(what)));

        TextArea why = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("Why it was added", ScrollableComponent.createWithVerticalScrollBar(why)));

        return panel;
    }
    
    private JPanel labelValuePanel(String labelText, JComponent widget) {
        BorderLayout bl = new BorderLayout(3, 4);
        JPanel panel = new JPanel(bl);
        JLabel label = new JLabel(labelText, JLabel.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.add(widget, BorderLayout.CENTER);

        return panel;
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();

        Button okButton = new Button("OK", okAction());
        panel.add(okButton);

        Button closeButton = new Button("Close", closeWindowAction());
        panel.add(closeButton);

        return panel;
    }

    public void observe(AppendDataViewPresenter presenter) {
        this.presenter = presenter;
    }

    private void selectSourceDataset() throws Exception {
        clearMsgPanel();
        
        DatasetType[] datasetTypes = new DatasetType[] { presenter.getDataset().getDatasetType() };
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole, this);
        InputDatasetSelectionPresenter srcDSPresenter = new InputDatasetSelectionPresenter(view,
                presenter.getSession(), datasetTypes);
        srcDSPresenter.display(datasetTypes[0]);

        EmfDataset[] datasets = view.getDatasets();
        sourceDataset = (datasets == null || datasets.length == 0) ? null : presenter.getDataset(datasets[0].getId());

        if (sourceDataset != null) {
            sourceDatasetField.setText(sourceDataset == null ? "" : sourceDataset.getName());
            sourceVersionBox.resetModel(presenter.getVersions(sourceDataset.getId()));
        }
    }
    
    private void viewSourceDataset(){
        try { 
            clearMsgPanel();
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(presenter.getSession(), parentConsole, desktopManager);
            presenter.doDisplayPropertiesView(view, sourceDataset);
//        EmfDataset ds = presenter.getDataset(sourceDataset.getId());
//        DataViewer view = new DataViewer(ds, parentConsole, desktopManager);
//            if (sourceDataset.getInternalSources().length > 0)
//                presenter.doView((Version)sourceVersionBox.getSelectedItem(), sourceDataset.getName(), view);
//            else
//                messagePanel.setError("Could not open viewer.This is an external file.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            e.printStackTrace();
        }
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();

                try {
                    presenter.appendData(sourceDataset.getId(), ((Version)sourceVersionBox.getSelectedItem()).getVersion(),
                            sourceFilterField.getText(),
                            presenter.getDataset().getId(), ((Version)targetDatasetVerison.getSelectedItem()).getVersion(),
                            0, 0);
                    setMsg("Appending data finished.");
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                }
            }
        };
    }

    private Action closeWindowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                disposeView();
            }
        };
    }

    private void clearMsgPanel() {
        messagePanel.clear();
    }

    private void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }
}
