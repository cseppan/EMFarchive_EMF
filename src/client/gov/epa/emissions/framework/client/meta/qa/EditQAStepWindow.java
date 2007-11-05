package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.FormattedDateField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.data.QAPrograms;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditQAStepWindow extends DisposableInteralFrame implements EditQAStepView {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.PATTERN_yyyyMMddHHmm);

    private EditableComboBox program;

    private TextArea programArguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private EditQAStepPresenter presenter;

    private QAStep step;

    private TextField who;

    private TextArea comments;

    private ComboBox status;

    private FormattedDateField date;

    private CheckBox required;

    private User user;

    private TextField config;

    private QAPrograms qaPrograms;

    private JTextField exportFolder;

    private QAStepResult qaStepResult;

    private Button saveButton;

    private Button runButton;

    private JCheckBox currentTable;

    private EmfSession session;

    private EmfConsole parentConsole;

    private TextField tableName;

    private JLabel creationStatusLabel;

    private JLabel creationDateLabel;

    private EmfDataset[] datasets;

    private EmfDataset[] invDatasets;

    private ArrayList<EmfDataset> inputDatasets;

    private ArrayList<EmfDataset> inputInvDatasets;

    private EmfDataset[] inputDatasetsArray;

    private EmfDataset[] inputInvDatasetsArray;

    private EmfDataset origDataset;

    private static final String invTableTag = "-invtable";

    private static final String avgDaySummaryProgram = "Average day to Annual State Summary";

    private static final String avgDayToAnnualProgram = "Average day to Annual Inventory";

    public EditQAStepWindow(DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Edit QA Step", new Dimension(680, 580), desktopManager);
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        inputDatasets = new ArrayList<EmfDataset>();
        inputInvDatasets = new ArrayList<EmfDataset>();
    }

    public void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset,
            String versionName, EmfSession session) {
        this.session = session;
        this.step = step;
        this.qaStepResult = qaStepResult;
        this.user = session.user();
        this.qaPrograms = new QAPrograms(session, programs);
        this.origDataset = dataset;
        super.setLabel(super.getTitle() + ": " + step.getName() + " - " + dataset.getName() + " (v" + step.getVersion()
                + ")");

        JPanel layout = createLayout(step, qaStepResult, versionName);
        super.getContentPane().add(layout);
        super.display();
    }

    public void windowClosing() {
        doClose();
    }

    public void observe(EditQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout(QAStep step, QAStepResult qaStepResult, String versionName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel(step, qaStepResult, versionName));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStep step, QAStepResult qaStepResult, String versionName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(upperPanel(step, versionName));
        panel.add(lowerPanel(step, qaStepResult));

        return panel;
    }

    private JPanel lowerPanel(QAStep step, QAStepResult qaStepResult) {

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(lowerTopLeftPanel(step));
        topPanel.add(lowerTopRightPanel(qaStepResult));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(topPanel);
        panel.add(lowerBottomPanel(step));
        return panel;
    }

    private JPanel lowerTopRightPanel(QAStepResult stepResult) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        if (stepResult == null)
            stepResult = new QAStepResult();

        String table = stepResult.getTable();
        table = (table == null) ? "" : table;
        tableName = new TextField("tableName", table, 20);
        tableName.setEditable(false);
        tableName.setToolTipText("The name of the output of the step (e.g. name of a table in the database");
        layoutGenerator.addLabelWidgetPair("Output Name:", tableName, panel);

        creationStatusLabel = new JLabel();
        String tableCreationStatus = stepResult.getTableCreationStatus();
        creationStatusLabel.setText((tableCreationStatus != null) ? tableCreationStatus : "");
        layoutGenerator.addLabelWidgetPair("Run Status:", creationStatusLabel, panel);

        creationDateLabel = new JLabel();
        Date tableCreationDate = stepResult.getTableCreationDate();
        String creationDate = (tableCreationDate != null) ? EmfDateFormat.format_MM_DD_YYYY_HH_mm(tableCreationDate)
                : "";
        creationDateLabel.setText(creationDate);
        layoutGenerator.addLabelWidgetPair("Run Date:", creationDateLabel, panel);

        currentTable = new JCheckBox();
        currentTable.setEnabled(false);
        currentTable.setSelected(stepResult.isCurrentTable());
        currentTable
                .setToolTipText("True when the source data and QA step have not been modified since the step was run");
        layoutGenerator.addLabelWidgetPair("Current Output?", currentTable, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        return panel;
    }

    private JPanel lowerBottomPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        config = new TextField("config", step.getConfiguration(), 40);
        addChangeable(config);
        config.setToolTipText("The name of the Dataset that is the configuration "
                + "file for the step (e.g., a REPCONFIG file)");
        layoutGenerator.addLabelWidgetPair("Configuration:", config, panel);

        comments = new TextArea("Comments", step.getComments(), 40, 2);
        addChangeable(comments);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(comments);
        layoutGenerator.addLabelWidgetPair("Comments:", scrollableComment, panel);
        comments.setToolTipText("Enter any notes of interest that you found when performing the step");

        layoutGenerator.addLabelWidgetPair("Export Folder:", exportFolderPanel(step), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;

    }

    private JPanel exportFolderPanel(QAStep step) {
        exportFolder = new JTextField(40);
        exportFolder.setToolTipText("The folder (directory) to which the step results will be exported");
        exportFolder.setName("folder");
        String outputFolder = step.getOutputFolder();
        exportFolder.setText(outputFolder != null ? outputFolder : "");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 10));
        folderPanel.add(exportFolder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        return folderPanel;
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(exportFolder.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to contain the exported QA step results");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            exportFolder.setText(file.getAbsolutePath());
        }
    }

    private JPanel lowerTopLeftPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        status = status(step);
        addChangeable(status);
        layoutGenerator.addLabelWidgetPair("QA Status:", status, panel);
        status.setToolTipText("Choosing a new status will automatically set the user and date");

        who = new TextField("who", step.getWho(), 10);
        addChangeable(who);
        layoutGenerator.addLabelWidgetPair("QA User:", who, panel);

        date = new FormattedDateField("Date", step.getDate(), DATE_FORMATTER, messagePanel);
        addChangeable(date);
        layoutGenerator.addLabelWidgetPair("QA Date:", date, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                40, 10);// xPad, yPad

        return panel;

    }

    private ComboBox status(QAStep step) {
        ComboBox status = new ComboBox(statusValue(step), QAProperties.status());
        status.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                date.setValue(new Date());
                who.setText(user.getName());
            }
        });

        return status;
    }

    private String statusValue(QAStep step) {
        return step.getStatus() != null ? step.getStatus() : QAProperties.initialStatus();
    }

    private JPanel upperPanel(QAStep step, String versionName) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", new Label(step.getName()), panel);

        program = new EditableComboBox(qaPrograms.names());
        program.setPreferredSize(new Dimension(250, 20));
        program.setPrototypeDisplayValue(EmptyStrings.create(20));
        QAProgram qaProgram = step.getProgram();
        if (qaProgram != null)
            program.setSelectedItem(qaProgram.getName());
        else
            program.setSelectedItem(null);
        addChangeable(program);
        program.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                currentTable.setSelected(false);
            }
        });

        JPanel prgpanel = new JPanel();
        prgpanel.add(new Label(versionName + " (" + step.getVersion() + ")"));
        prgpanel.add(new JLabel(EmptyStrings.create(37)));
        prgpanel.add(new JLabel("Program:  "));
        prgpanel.add(program);

        layoutGenerator.addLabelWidgetPair("Version:", prgpanel, panel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        programArguments = new TextArea("", step.getProgramArguments(), 41, 3);
        addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        buttonPanel.add(scrollableDetails);
        JPanel setButtonPanel = new JPanel(new BorderLayout());
        setButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
        Button button1 = setButton();
        setButtonPanel.add(button1, BorderLayout.NORTH);
        buttonPanel.add(setButtonPanel);
        layoutGenerator.addLabelWidgetPair("Arguments:", buttonPanel, panel);
        programArguments.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                currentTable.setSelected(false);
            }
        });

        required = new CheckBox("", step.isRequired());
        if (step.isRequired())
            required.setEnabled(false);

        order = new NumberFormattedTextField(3, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        addChangeable(order);

        JPanel checkBoxPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layout = new SpringLayoutGenerator();
        JPanel reqirepanel = new JPanel();
        reqirepanel.add(new JLabel(EmptyStrings.create(57)));
        reqirepanel.add(new JLabel("Required?"));
        reqirepanel.add(required);
        layout.addWidgetPair(order, reqirepanel, checkBoxPanel);
        layout.makeCompactGrid(checkBoxPanel, 1, 2, 0, 0, 0, 0);
        layoutGenerator.addLabelWidgetPair("Order:", checkBoxPanel, panel);

        description = new TextArea("", step.getDescription(), 41, 3);
        addChangeable(description);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions();
            }

            public void keyReleased(KeyEvent e) {
                keyActions();
            }

            public void keyPressed(KeyEvent e) {
                keyActions();
            }
        };
    }

    private void keyActions() {
        try {
            messagePanel.clear();
            Float.parseFloat(order.getText());
        } catch (NumberFormatException ex) {
            messagePanel.setError("Order should be a floating point number");
        }
    }

    private AbstractAction orderAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        };
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        saveButton = saveButton();
        getRootPane().setDefaultButton(saveButton);
        panel.add(saveButton);

        Button cancel = closeButton();
        panel.add(cancel);

        panel.add(Box.createHorizontalStrut(50));
        runButton = runButton();
        panel.add(runButton);

        Button viewResults = viewResultsButton();
        panel.add(viewResults);

        Button export = exportButton();
        panel.add(export);
        
        Button refresh = refreshButton();
        panel.add(refresh);
        
        return panel;
    }

    private Button viewResultsButton() {
        Button view = new Button("View Results", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                try {
                    viewResults();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        
        view.setMnemonic('V');
        return view;
    }

    private Button runButton() {
        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    checkDatasets();
                    clear();
                    runQAStep();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });

        return run;
    }

    private void checkDatasets() throws EmfException {

        final String invTableTag = "-invtable";
        String programSwitches = "";
        String nextDataset = "";

        programSwitches = programArguments.getText();

        if (avgDaySummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())
                && !(programSwitches.equals(""))) {
            int index1 = programSwitches.indexOf(invTableTag);
            if (programSwitches.substring(0, 12).equals("-inventories") && index1 != -1) {
                String inventoriesToken = programSwitches.substring(0, index1);
                String invtableToken = programSwitches.substring(index1 + invTableTag.length());

                StringTokenizer tokenizer2 = new StringTokenizer(inventoriesToken);

                tokenizer2.nextToken();
                int i = 0;

                while (tokenizer2.hasMoreTokens()) {
                    try {
                        nextDataset = tokenizer2.nextToken().trim();
                        inputDatasets.add(presenter.getDataset(nextDataset));
                        i++;
                    } catch (EmfException ex) {
                        throw new EmfException("The dataset name " + nextDataset + " is not valid");
                    }
                }

                inputDatasetsArray = new EmfDataset[inputDatasets.size()];
                inputDatasets.toArray(inputDatasetsArray);
                StringTokenizer tokenizer3 = new StringTokenizer(invtableToken);
                int j = 0;

                while (tokenizer3.hasMoreTokens()) {
                    try {
                        inputInvDatasets.add(presenter.getDataset(tokenizer3.nextToken().trim()));
                        j++;
                    } catch (EmfException ex) {
                        throw new EmfException("The inventory table dataset is invalid");
                    }
                }
            }
        } else if (avgDayToAnnualProgram.equals(program.getSelectedItem()) && !(programSwitches.equals(""))) {
            if (programSwitches.substring(0, 12).equals("-inventories")) {
                StringTokenizer tokenizer2 = new StringTokenizer(programSwitches);
                tokenizer2.nextToken();
                int i = 0;

                while (tokenizer2.hasMoreTokens()) {
                    try {
                        nextDataset = tokenizer2.nextToken().trim();
                        inputDatasets.add(presenter.getDataset(nextDataset));
                        i++;
                    } catch (EmfException ex) {
                        throw new EmfException("The dataset name " + nextDataset + " is not valid");
                    }
                }

                inputDatasetsArray = new EmfDataset[inputDatasets.size()];
                inputDatasets.toArray(inputDatasetsArray);
            }
        }
    }

    private Button exportButton() {
        Button export = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doExport();
            }
        });
        export.setMnemonic('x');
        
        return export;
    }
    
    private Button refreshButton() {
        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clear();
                    resetRunStatus(presenter.getStepResult(step));
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        refresh.setMnemonic('R');

        return refresh;
    }

    private Button setButton() {
        Button export = new Button("Set", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();

                if (avgDaySummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showAvgDaySummaryWindow();
                } else if (avgDayToAnnualProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showAvgDayToAnnualWindow();
                } else {
                    doSetWindow();
                }
            }
        });
        return export;
    }

    private void showAvgDaySummaryWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        String programSwitches = "";
        String nextDataset = "";
        programSwitches = programArguments.getText();

        if (!(programSwitches.equals(""))) {
            int index1 = programSwitches.indexOf(invTableTag);

            if (programSwitches.substring(0, 12).equals("-inventories") && index1 != -1) {
                String inventoriesToken = programSwitches.substring(0, index1);
                String invtableToken = programSwitches.substring(index1 + invTableTag.length());
                StringTokenizer tokenizer2 = new StringTokenizer(inventoriesToken);
                tokenizer2.nextToken();
                int i = 0;

                while (tokenizer2.hasMoreTokens()) {
                    try {
                        nextDataset = tokenizer2.nextToken().trim();
                        inputDatasets.add(presenter.getDataset(nextDataset));
                        i++;
                    } catch (EmfException ex) {
                        messagePanel.setError("The dataset name " + nextDataset + " is not valid");
                    }
                }

                inputDatasetsArray = new EmfDataset[inputDatasets.size()];
                inputDatasets.toArray(inputDatasetsArray);
                StringTokenizer tokenizer3 = new StringTokenizer(invtableToken);
                int j = 0;

                while (tokenizer3.hasMoreTokens()) {
                    try {
                        inputInvDatasets.add(presenter.getDataset(tokenizer3.nextToken().trim()));
                        j++;
                    } catch (EmfException ex) {
                        messagePanel.setError("The inventory table dataset is invalid");
                    }
                }

                inputInvDatasetsArray = new EmfDataset[inputInvDatasets.size()];
                inputInvDatasets.toArray(inputInvDatasetsArray);

                EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, session, inputDatasetsArray,
                        inputInvDatasetsArray);
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this);
                presenter.display(origDataset, step);

                inputDatasets.clear();
                inputInvDatasets.clear();

                for (int m = 0; m < inputDatasetsArray.length; m++)
                    inputDatasetsArray[m] = null;

                for (int k = 0; k < inputInvDatasetsArray.length; k++)
                    inputInvDatasetsArray[k] = null;
            } else {
                EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, session);
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this);
                presenter.display(origDataset, step);
                messagePanel.setError("The data in the Program Arguments window is invalid");
            }
        } else {
            EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, session);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this);
            presenter.display(origDataset, step);
            messagePanel.setError("The Program Arguments window is blank");
        }
    }

    private void showAvgDayToAnnualWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        String programSwitches = "";
        StringTokenizer tokenizer2;
        String nextDataset = "";
        inputDatasets.clear();
        programSwitches = programArguments.getText();

        if (!(programSwitches.equals(""))) {
            int index1 = programSwitches.indexOf(invTableTag);

            if (programSwitches.substring(0, 12).equals("-inventories")) {
                if (index1 != -1) {
                    String inventoriesToken = programSwitches.substring(0, index1);
                    tokenizer2 = new StringTokenizer(inventoriesToken);
                } else {
                    tokenizer2 = new StringTokenizer(programSwitches);
                }

                tokenizer2.nextToken();
                int i = 0;

                while (tokenizer2.hasMoreTokens()) {
                    try {
                        nextDataset = tokenizer2.nextToken().trim();
                        inputDatasets.add(presenter.getDataset(nextDataset));
                        i++;
                    } catch (EmfException ex) {
                        messagePanel.setError("The dataset name " + nextDataset + " is not valid");
                    }
                }

                inputDatasetsArray = new EmfDataset[inputDatasets.size()];
                inputDatasets.toArray(inputDatasetsArray);
                EditQANonsummaryEmissionsWindow view = new EditQANonsummaryEmissionsWindow(desktopManager, session,
                        inputDatasetsArray, messagePanel);
                EditQANonsummaryEmissionsPresenter presenter = new EditQANonsummaryEmissionsPresenter(view, this);
                presenter.display(origDataset, step);

                for (int m = 0; m < inputDatasetsArray.length; m++)
                    inputDatasetsArray[m] = null;

            } else {
                EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, session);
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this);
                presenter.display(origDataset, step);
                messagePanel.setError("The data in the Program Arguments window is invalid");
            }
        } else {
            EditQANonsummaryEmissionsWindow view = new EditQANonsummaryEmissionsWindow(desktopManager, session);
            EditQANonsummaryEmissionsPresenter presenter = new EditQANonsummaryEmissionsPresenter(view, this);
            presenter.display(origDataset, step);
            messagePanel.setError("The Program Arguments window is blank");
        }
    }

    private void doSetWindow() {
        String argumentsText = programArguments.getText();
        EditQAArgumentsWindow view = new EditQAArgumentsWindow(desktopManager, argumentsText);
        EditQAArgumentsPresenter presenter = new EditQAArgumentsPresenter(view, this);
        presenter.display();
        view.display(origDataset, step);
    }

    public void updateArgumentsTextArea(String text) {
        programArguments.clear();
        programArguments.setText(text);
    }

    public void updateDatasets(Object[] retreivedDatasets, Object[] retrievedInvDatasets) {

        String datasetNames = "-inventories\n";

        for (int i = 0; i < retreivedDatasets.length; i++) {
            datasets = new EmfDataset[36];
            datasets[i] = (EmfDataset) retreivedDatasets[i];
            datasetNames += datasets[i] + "\n";
        }

        for (int i = 0; i < retrievedInvDatasets.length; i++) {
            invDatasets = new EmfDataset[1];
            invDatasets[i] = (EmfDataset) retrievedInvDatasets[i];
            datasetNames += "-invtable " + "\n" + invDatasets[i];
        }

        updateArgumentsTextArea(datasetNames);

    }

    public void updateDatasets(Object[] retreivedDatasets) {

        String datasetNames = "-inventories\n";

        for (int i = 0; i < retreivedDatasets.length; i++) {
            datasets = new EmfDataset[36];
            datasets[i] = (EmfDataset) retreivedDatasets[i];
            datasetNames += datasets[i] + "\n";
        }

        updateArgumentsTextArea(datasetNames);
    }

    protected void runQAStep() throws EmfException {
        messagePanel
                .setMessage("Started Run. Please monitor the Status window and click Refresh button to track your run request.");
        status.setSelectedItem("In Progress");
        date.setValue(new Date());
        who.setText(presenter.userName());
        saveButton.setEnabled(false);// to prevent user from clicking save after started running a qa step
        runButton.setEnabled(false); // only allow user to run one time at a open window session

        presenter.run();
        resetChanges();
    }

    private void resetRunStatus(QAStepResult result) {
        if (result == null)
            return;
        
        saveButton.setEnabled(true);
        who.setText(step.getWho());
        date.setText(DATE_FORMATTER.format(step.getDate()));
        status.setSelectedItem(step.getStatus());
        tableName.setText(result.getTable());
        creationStatusLabel.setText(result.getTableCreationStatus());
        creationDateLabel.setText(EmfDateFormat.format_MM_DD_YYYY_HH_mm(result
                .getTableCreationDate()));
        currentTable.setSelected(result.isCurrentTable());
        qaStepResult = result;
        super.revalidate();
    }

    protected void doExport() {
        try {
            messagePanel.setMessage("Started Export. Please monitor the Status window "
                    + "to track your export request.");
            presenter.export(step, qaStepResult, exportFolder.getText());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void viewResults() throws EmfException {
        final String exportDir = exportFolder.getText();

        if (exportDir == null || exportDir.trim().isEmpty())
            throw new EmfException("Please specify the exported result directory.");

        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    clear();
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    presenter.viewResults(step, exportDir.trim());
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        viewResultsThread.start();
    }

    private Button saveButton() {
        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clear();
                    presenter.save();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        return save;
    }

    private Button closeButton() {
        Button cancel = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doClose();
            }
        });
        return cancel;
    }

    protected void doClose() {
        if (super.shouldDiscardChanges())
            presenter.close();
    }

    public QAStep save() throws EmfException {

        if (order.getText().equals("")) {
            throw new EmfException("Order should be a floating point number");
        }

        step.setProgram(qaPrograms.get(program.getSelectedItem()));
        step.setProgramArguments(programArguments.getText());
        step.setOrder(Float.parseFloat(order.getText()));
        step.setDescription(description.getText().trim());
        step.setRequired(required.isSelected());

        step.setStatus((String) status.getSelectedItem());
        step.setComments(comments.getText());
        step.setWho(who.getText());
        step.setDate(date.value());
        step.setConfiguration(config.getText());
        if (exportFolder.getText() != null)
            step.setOutputFolder(exportFolder.getText().trim());
        return step;
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            exportFolder.setText(mostRecentUsedFolder);
    }

    private void clear() {
        messagePanel.clear();
    }

    // Modified length in Dimension to make View window ful size
    public void displayResultsTable(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QAStep \"" + qaStepName + "\" results ",
                new Dimension(800, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }
}