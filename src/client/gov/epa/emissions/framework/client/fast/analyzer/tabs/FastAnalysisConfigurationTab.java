package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.run.tabs.DatasetCommand;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastAnalysisRun;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.Grid;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastAnalysisConfigurationTab extends AbstractFastAnalysisTab {

    private ComboBox gridCombobox;

    private ComboBox baselineRunCombobox;

    private ComboBox sensitivityRunCombobox;

    private TextField cancerRiskDatasetField;

    private ComboBox cancerRiskDatasetVersionComboBox;

    public FastAnalysisConfigurationTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastAnalysisPresenter presenter) {

        super(analysis, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
        this.setName("Configuration");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(this.createMiddlePane(), BorderLayout.CENTER);
        super.display();
    }

    
    protected void addChangables() {

        ManageChangeables changeablesList = this.getChangeablesList();
        changeablesList.addChangeable(this.gridCombobox);
        changeablesList.addChangeable(this.baselineRunCombobox);
        changeablesList.addChangeable(this.sensitivityRunCombobox);
        changeablesList.addChangeable(this.cancerRiskDatasetField);
        changeablesList.addChangeable(this.cancerRiskDatasetVersionComboBox);
    }

    protected void populateFields() {

        FastAnalysis analysis = this.getAnalysis();

        Grid[] grids = new Grid[0];
        try {
            grids = this.getSession().fastService().getGrids();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        Grid grid = analysis.getGrid();

        this.gridCombobox.removeAllItems();
        this.gridCombobox.setModel(new DefaultComboBoxModel(grids));
        this.gridCombobox.setSelectedItem(grid);

        FastAnalysisRun[] baselineRuns = analysis.getBaselineRuns();

        this.baselineRunCombobox.removeAllItems();
        this.baselineRunCombobox.setModel(new DefaultComboBoxModel(this.getBaselineRuns(grid)));

        if (baselineRuns != null && baselineRuns.length > 0) {
            this.baselineRunCombobox.setSelectedItem(baselineRuns[0]);
        }

        FastAnalysisRun[] sensitivityRuns = analysis.getSensitivityRuns();

        this.sensitivityRunCombobox.removeAllItems();
        this.sensitivityRunCombobox.setModel(new DefaultComboBoxModel(this.getSensitivityRuns(grid)));

        if (sensitivityRuns != null && sensitivityRuns.length > 0) {
            this.sensitivityRunCombobox.setSelectedItem(sensitivityRuns[0]);
        }

        EmfDataset dataset = analysis.getCancerRiskDataset();
        if (dataset != null) {

            this.cancerRiskDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.cancerRiskDatasetVersionComboBox);
            this.cancerRiskDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, analysis
                    .getCancerRiskDatasetVersion()));
        }
    }

    private JPanel createMiddlePane() {

        JPanel panel = new JPanel(new GridBagLayout());

        try {

            Grid[] grids = this.getSession().fastService().getGrids();

            Insets labelInsets = new Insets(8, 10, 8, 5);
            Insets valueInsets = new Insets(4, 0, 4, 0);
            Insets buttonInsets = new Insets(0, 10, 0, 10);
            Dimension fieldSize = new Dimension(0, 10);
            Dimension comboBoxSize = new Dimension(100, 24);

            GridBagConstraints constraints = new GridBagConstraints();

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel gridLabel = new JLabel("Grid:");
            panel.add(gridLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.gridCombobox = new ComboBox(grids);
            this.gridCombobox.setPreferredSize(fieldSize);
            panel.add(this.gridCombobox, constraints);

            this.gridCombobox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println(e);
                }
            });

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel invTableLabel = new JLabel("Baseline Analysis Run:");
            panel.add(invTableLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.baselineRunCombobox = new ComboBox(this.getBaselineRuns((Grid) this.gridCombobox.getSelectedItem()));
            this.baselineRunCombobox.setPreferredSize(fieldSize);
            panel.add(this.baselineRunCombobox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel speciesMappingLabel = new JLabel("Sensitivity Analysis Run:");
            panel.add(speciesMappingLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.sensitivityRunCombobox = new ComboBox(this.getSensitivityRuns((Grid) this.gridCombobox
                    .getSelectedItem()));
            this.sensitivityRunCombobox.setPreferredSize(fieldSize);
            panel.add(this.sensitivityRunCombobox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel cancerRiskLabel = new JLabel("Cancer Risk Dataset:");
            panel.add(cancerRiskLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.cancerRiskDatasetField = new TextField("Cancer Risk Dataset", 45);
            this.cancerRiskDatasetField.setPreferredSize(fieldSize);
            this.cancerRiskDatasetField.setEditable(false);
            this.cancerRiskDatasetField.setBackground(Color.WHITE);
            panel.add(this.cancerRiskDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button cancerRiskDatasetButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding cancer risk dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {
                        public void execute() throws EmfException {
                            FastAnalysis analysis = getAnalysis();
                            analysis.setCancerRiskDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.FAST_CANCER_RISK), cancerRiskDatasetField,
                            cancerRiskDatasetVersionComboBox, command);
                }
            });

            panel.add(cancerRiskDatasetButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel cancerRiskDatasetVersionLabel = new JLabel("Cancer Risk Dataset Version:");
            panel.add(cancerRiskDatasetVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.cancerRiskDatasetVersionComboBox = new ComboBox();
            this.cancerRiskDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.cancerRiskDatasetVersionComboBox.setEditable(false);
            this.cancerRiskDatasetVersionComboBox.setEnabled(false);
            panel.add(this.cancerRiskDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.gridwidth = 3;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTH;

            JLabel emptyLabel = new JLabel();
            emptyLabel.setOpaque(false);

            panel.add(emptyLabel, constraints);

        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        return panel;
    }

    private FastAnalysisRun[] getBaselineRuns(Grid grid) {

        FastRun[] fastRuns = new FastRun[0];
        try {
            fastRuns = this.getSession().fastService().getFastRuns(grid.getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        FastAnalysisRun[] analysisRuns = new FastAnalysisRun[fastRuns.length];
        for (int i = 0; i < fastRuns.length; i++) {
            analysisRuns[i] = FastAnalysisRun.createBaselineRun(fastRuns[i]);
        }

        return analysisRuns;
    }

    private FastAnalysisRun[] getSensitivityRuns(Grid grid) {

        FastRun[] fastRuns = new FastRun[0];
        try {
            fastRuns = this.getSession().fastService().getFastRuns(grid.getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        FastAnalysisRun[] analysisRuns = new FastAnalysisRun[fastRuns.length];
        for (int i = 0; i < fastRuns.length; i++) {
            analysisRuns[i] = FastAnalysisRun.createSensitivityRun(fastRuns[i]);
        }

        return analysisRuns;
    }

    private void getFastAnalysisRuns(DatasetType datasetType, TextField datasetField, ComboBox datasetComboBox,
            DatasetCommand command) {

        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole());

        List<DatasetType> datasetTypes = new ArrayList<DatasetType>(1);
        datasetTypes.add(datasetType);

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), datasetTypes.toArray(new DatasetType[0]));
        try {

            inputDatasetPresenter.display(datasetType, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset dataset = inputDatasets[0];
                    datasetField.setText(dataset.getName());
                    datasetField.setToolTipText(dataset.getName());

                    if (dataset != null) {

                        command.setDataset(dataset);
                        command.execute();

                        updateVersions(dataset, datasetComboBox);
                    }
                }
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private void getDataset(DatasetType datasetType, TextField datasetField, ComboBox datasetComboBox,
            DatasetCommand command) {

        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole());

        List<DatasetType> datasetTypes = new ArrayList<DatasetType>(1);
        datasetTypes.add(datasetType);

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), datasetTypes.toArray(new DatasetType[0]));
        try {

            inputDatasetPresenter.display(datasetType, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset dataset = inputDatasets[0];
                    datasetField.setText(dataset.getName());
                    datasetField.setToolTipText(dataset.getName());

                    if (dataset != null) {

                        command.setDataset(dataset);
                        command.execute();

                        updateVersions(dataset, datasetComboBox);
                    }
                }
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private void updateVersions(EmfDataset dataset, ComboBox comboBox) {

        try {

            Version[] versions = this.getPresenter().getVersions(dataset);
            comboBox.removeAllItems();
            comboBox.setModel(new DefaultComboBoxModel(versions));
            comboBox.revalidate();
            if (versions.length > 0) {

                comboBox.setSelectedItem(getDefaultVersionIndex(versions, dataset));
                comboBox.setEnabled(true);
            } else {
                comboBox.setEnabled(false);
            }
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {

        int retVal = 0;
        int defaultversion = dataset.getDefaultVersion();
        for (int i = 0; i < versions.length; i++) {

            if (defaultversion == versions[i].getVersion()) {

                retVal = i;
                break;
            }
        }

        return retVal;
    }

    private int getVersionIndex(EmfDataset dataset, int versionNumber) {

        Version[] versions = new Version[0];
        try {
            versions = this.getPresenter().getVersions(dataset);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        int retVal = 0;
        for (int i = 0; i < versions.length; i++) {

            if (versionNumber == versions[i].getVersion()) {

                retVal = i;
                break;
            }
        }

        return retVal;
    }

    @Override
    void refreshData() {
        this.populateFields();
    }

    public void save(FastAnalysis analysis) throws EmfException {

        this.clearMessage();

        validateFields();

        FastAnalysisRun[] baselineRuns = new FastAnalysisRun[1];
        baselineRuns[0] = (FastAnalysisRun) this.baselineRunCombobox.getSelectedItem();
        getAnalysis().setBaselineRuns(baselineRuns);

        analysis.setBaselineRuns(baselineRuns);

        FastAnalysisRun[] sensitivityRuns = new FastAnalysisRun[1];
        sensitivityRuns[0] = (FastAnalysisRun) sensitivityRunCombobox.getSelectedItem();
        getAnalysis().setSensitivityRuns(sensitivityRuns);

        analysis.setSensitivityRuns(sensitivityRuns);

        analysis.setCancerRiskDataset(this.getAnalysis().getCancerRiskDataset());
        analysis.setGrid((Grid) this.gridCombobox.getSelectedItem());

        Object selectedItem = this.cancerRiskDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            analysis.setCancerRiskDatasetVersion(version.getVersion());
        }
    }

    private void validateFields() throws EmfException {

        this.clearMessage();

        if (this.cancerRiskDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A cancer risk dataset must be specified");
        }
    }

    public void refresh(FastAnalysis analysis) {

        this.setAnalysis(analysis);

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.populateFields();
            this.refreshLayout();
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void viewOnly() {

        // this.nameField.setEditable(false);
        // this.abbreviationField.setEditable(false);
        // this.descriptionField.setEditable(false);
    }
}
