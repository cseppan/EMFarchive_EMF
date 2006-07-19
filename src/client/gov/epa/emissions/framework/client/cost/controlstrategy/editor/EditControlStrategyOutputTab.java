package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class EditControlStrategyOutputTab extends JPanel implements EditControlStrategyOutputTabView {

    private TextField folder;

    private EditControlStrategyOutputTabPresenter presenter;

    private ControlStrategy controlStrategy;

    private MessagePanel messagePanel;

    public EditControlStrategyOutputTab(ControlStrategy controlStrategy, EmfSession session, MessagePanel messagePanel,
            EmfConsole parentConsole) {
        super.setName("output");
        this.controlStrategy = controlStrategy;
        this.messagePanel = messagePanel;
        setLayout(controlStrategy);
    }

    private void setLayout(ControlStrategy controlStrategy) {
        setLayout(new BorderLayout());
        add(topPanel(), BorderLayout.NORTH);
        add(outputPanel(controlStrategy));
    }

    public void save(ControlStrategy controlStrategy) {
        // TODO: output settings
    }

    public void observe(EditControlStrategyOutputTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {
        try {
            presenter.doExport(controlStrategy, folder.getText());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void analyze() {
        try {
            presenter.doAnalyze(controlStrategy, folder.getText());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private JPanel topPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(productPanel());
        topPanel.add(bottomPanel(), BorderLayout.SOUTH);

        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Output Settings")));

        return topPanel;
    }

    private JPanel bottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(folderPanel());
        JPanel createPanel = createButtonPanel();
        panel.add(createPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonPanel() {
        Button button = new Button("Create", null);
        button.setEnabled(false);
        JPanel createPanel = new JPanel();
        createPanel.add(button);
        return createPanel;
    }

    private JPanel productPanel() {
        JPanel productPanel = new JPanel();
        CheckBox inventoryCheckBox = new CheckBox("Inventory");
        inventoryCheckBox.setEnabled(false);
        CheckBox summaryFIPS = new CheckBox("FIPS Summary");
        summaryFIPS.setEnabled(false);
        productPanel.add(inventoryCheckBox);
        productPanel.add(summaryFIPS);
        return productPanel;
    }

    private JPanel folderPanel() {
        JLabel folderLabel = new JLabel("Folder: ");
        folder = new TextField("folderName", 30);

        Button browseButton = new Button("Browse", browseAction());

        JPanel panel = new JPanel();
        panel.add(folderLabel);
        panel.add(folder);
        panel.add(browseButton);

        return panel;
    }

    private JPanel outputPanel(ControlStrategy controlStrategy) {
        JPanel tablePanel = tablePanel(controlStrategy);
        JPanel buttonPanel = buttonPanel();

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(tablePanel);
        outputPanel.add(buttonPanel, BorderLayout.SOUTH);

        outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Results")));

        return outputPanel;
    }

    private JPanel tablePanel(ControlStrategy controlStrategy) {
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        StrategyResultsTableData tableData = new StrategyResultsTableData(strategyResults);
        EmfTableModel model = new EmfTableModel(tableData);
        SortFilterSelectModel selectModel = new SortFilterSelectModel(model);
        JTable table = new JTable(selectModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollPane);
        return tablePanel;
    }

    private JPanel buttonPanel() {
        Button exportButton = new Button("Export", exportAction());
        exportButton.setEnabled(false);
        Button analysisButton = new Button("Analyze", analysisAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
        return buttonPanel;
    }

    private Action exportAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // NOTE Auto-generated method stub
            }

        };
    }

    private Action analysisAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                analyze();
            }
        };
    }

    private Action browseAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectFolder();
            }
        };
    }

    private void selectFolder() {
        FileChooser chooser = new FileChooser("Select Folder", new File(folder.getText()),
                EditControlStrategyOutputTab.this);

        chooser.setTitle("Select a folder");
        File[] files = chooser.choose();
        if (files == null)
            return;

        if (files[0].isDirectory()) {
            folder.setText(files[0].getAbsolutePath());
        }

        if (files[0].isFile()) {
            folder.setText(files[0].getParent());
        }
    }

}
