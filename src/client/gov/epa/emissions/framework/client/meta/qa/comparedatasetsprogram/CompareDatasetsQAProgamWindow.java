package gov.epa.emissions.framework.client.meta.qa.comparedatasetsprogram;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.AddRemoveDatasetVersionWidget;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.meta.qa.AddRemoveDatasetWidget;
import gov.epa.emissions.framework.client.meta.qa.EditQAEmissionsPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditQAEmissionsView;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class CompareDatasetsQAProgamWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    private AddRemoveDatasetVersionWidget datasetWidgetBase;
    private AddRemoveDatasetVersionWidget datasetWidgetCompare;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private DatasetVersion[] baseDatasetVersions;
    
    private DatasetVersion[] compareDatasetVersions;
    
    private TextArea groupByExpressionsTextField;
    
    private TextArea aggregateExpressionsTextField;
    
    private TextArea matchingExpressionsTextField;
    
    private String groupByExpressions;
    
    private String aggregateExpressions;
    
    private String matchingExpressions;

    private String program;
        
    public CompareDatasetsQAProgamWindow(DesktopManager desktopManager, String program, 
            EmfSession session, DatasetVersion[] baseDatasetVersions, DatasetVersion[] compareDatasetVersions, 
            String groupByExpressions, String aggregateExpressions, String matchingExpressions) {
        
        super("Emissions Inventories Editor", new Dimension(650, 600), desktopManager);
        this.program = program; 
        this.session = session;
        this.baseDatasetVersions = baseDatasetVersions;
        this.compareDatasetVersions = compareDatasetVersions;
        this.groupByExpressions = groupByExpressions;
        this.aggregateExpressions = aggregateExpressions;
        this.matchingExpressions = matchingExpressions;
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        try {
            this.getContentPane().add(createLayout(dataset));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void observe(EditQAEmissionsPresenter presenter) {
        this.presenter = presenter;
    }
    
    private JPanel createLayout(EmfDataset dataset) throws EmfException {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("Base datasets:", baseDatasetVersionWidget(), content);
        layoutGenerator.addLabelWidgetPair("Compare datasets:", compareDatasetVersionWidget(), content);

        this.groupByExpressionsTextField = new TextArea("Group By Expressions", this.groupByExpressions, 40, 5);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(this.groupByExpressionsTextField);
        scrollableComment.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Group By Expressions:", scrollableComment, content);

        this.aggregateExpressionsTextField = new TextArea("Aggregate Expressions", this.aggregateExpressions, 40, 5);
        ScrollableComponent scrollableComment2 = ScrollableComponent.createWithVerticalScrollBar(this.aggregateExpressionsTextField);
        scrollableComment2.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Aggregate Expressions:", scrollableComment2, content);

        this.matchingExpressionsTextField = new TextArea("Matching Expressions", this.matchingExpressions, 40, 5);
        ScrollableComponent scrollableComment3 = ScrollableComponent.createWithVerticalScrollBar(this.matchingExpressionsTextField);
        scrollableComment3.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Matching Expressions:", scrollableComment3, content);

        
        
        layoutGenerator.makeCompactGrid(content, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel baseDatasetVersionWidget() throws EmfException {
        datasetWidgetBase = new AddRemoveDatasetVersionWidget(false, this, parentConsole, session);
        datasetWidgetBase.setPreferredSize(new Dimension(350,250));
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        if(baseDatasetVersions != null && baseDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : baseDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetBase.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetBase;
    }
    
    private JPanel compareDatasetVersionWidget() throws EmfException {
        datasetWidgetCompare = new AddRemoveDatasetVersionWidget(false, this, parentConsole, session);
        datasetWidgetCompare.setPreferredSize(new Dimension(350,250));
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        if(compareDatasetVersions != null && compareDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : compareDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetCompare.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetCompare;
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
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (datasetWidgetBase.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose base datasets to compare with.");
                    return;
                }
                if (datasetWidgetCompare.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose compare datasets to compare with.");
                    return;
                }
                if (groupByExpressionsTextField.getText().trim().length() == 0) {
                    messagePanel.setError("Please specify GROUP BY expression(s).");
                    return;
                }
                if (aggregateExpressionsTextField.getText().trim().length() == 0) {
                    messagePanel.setError("Please specify aggregrate expression(s).");
                    return;
                }
                
                
/*sample program arguments        

-base
ptipm_cap2005v2_nc_sc_va|0
-compare
$DATASET
-groupby
scc
substring(fips,1,2)
-aggregate
ann_emis
avd_emis
-matching
substring(fips,1,2)=substring(region_cd,1,2)
scc=scc_code
ann_emis=emis_ann
avd_emis=emis_avd
*/
                StringBuilder programArguments = new StringBuilder();
                //base tag
                programArguments.append(EditQAStepWindow.BASE_TAG + "\n");
                for (Object datasetVersion : datasetWidgetBase.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //compare tag
                programArguments.append(EditQAStepWindow.COMPARE_TAG + "\n");
                for (Object datasetVersion : datasetWidgetCompare.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //group by tag
                programArguments.append(EditQAStepWindow.GROUP_BY_EXPRESSIONS_TAG + "\n");
                programArguments.append(groupByExpressionsTextField.getText() + "\n");
                //aggregate tag
                programArguments.append(EditQAStepWindow.AGGREGATE_EXPRESSIONS_TAG + "\n");
                programArguments.append(aggregateExpressionsTextField.getText() + "\n");
                //group by tag
                programArguments.append(EditQAStepWindow.MATCHING_EXPRESSIONS_TAG + "\n");
                programArguments.append(matchingExpressionsTextField.getText());
                presenter.updateProgramArguments(programArguments.toString());
                dispose();
                disposeView();
            }
        };
    }

}
