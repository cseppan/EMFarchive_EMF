package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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

    private ComboBox targetDatasetVerison;

    public AppendDataWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Append Data Window", new Dimension(750, 350), desktopManager);

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
        layout.add(createUpperPanel());
        layout.add(createLowerPanel());
        layout.add(createButtonPanel());

        super.display();

    }

    private Component createUpperPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Source Dataset", 0, 0, Font.decode(""), Color.BLUE));

        sourceDatasetField = new JTextField(40);
        sourceDatasetField.setName("sourceDataset");

        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    selectSourceDataset();
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                }
            }
        });

        Icon icon = new ImageResources().open("Select a Dataset");
        button.setIcon(icon);

        JPanel sourceDatasetPanel = new JPanel(new BorderLayout(2, 0));
        sourceDatasetPanel.add(sourceDatasetField, BorderLayout.LINE_START);
        sourceDatasetPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Source Dataset", sourceDatasetPanel, panel);

        sourceVersionBox = new ComboBox("Select a version", new Version[0]);
        layoutGenerator.addLabelWidgetPair("Source Dataset Version", sourceVersionBox, panel);

        sourceFilterField = new JTextField(40);
        layoutGenerator.addLabelWidgetPair("Source Dataset Filter", sourceFilterField, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private Component createLowerPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Target Dataset", 0, 0, Font.decode(""), Color.BLUE));

        startLineField = new JTextField(40);
        startLineField.setName("startLineField");
        startLineField.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Starting Line Number", startLineField, panel);

        Version[] versions = null;
        
        try {
            versions = presenter.getTargetDatasetNonFinalVersions();
        } catch (EmfException e) {
            setErrorMsg(e.getMessage());
        }
        
        targetDatasetVerison = new ComboBox("Select a version", versions);
        layoutGenerator.addLabelWidgetPair("Current Dataset Version", targetDatasetVerison, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

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
        sourceDataset = (datasets == null || datasets.length == 0) ? null : datasets[0];

        if (sourceDataset != null) {
            sourceDatasetField.setText(sourceDataset == null ? "" : sourceDataset.getName());
            sourceVersionBox.resetModel(presenter.getVersions(sourceDataset.getId()));
        }
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();

                try {
                    presenter.appendData(sourceDataset.getId(), ((Version)sourceVersionBox.getSelectedItem()).getVersion(),
                            sourceFilterField.getText(),
                            presenter.getDataset().getId(), ((Version)targetDatasetVerison.getSelectedItem()).getVersion());
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
}
