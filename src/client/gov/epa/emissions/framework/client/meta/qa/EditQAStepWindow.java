package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private JCheckBox currentTable;
    
    private EmfSession session;

    private EmfConsole parentConsole;
   
    public EditQAStepWindow(DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Edit QA Step", new Dimension(650, 625), desktopManager);
        this.parentConsole = parentConsole;
    }

    public void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, String versionName,
            EmfSession session) {
        this.session = session;
        this.step = step;
        this.qaStepResult = qaStepResult;
        this.user = session.user();
        this.qaPrograms = new QAPrograms(session, programs);
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
        TextField tableName = new TextField("tableName", table, 20);
        tableName.setEditable(false);
        tableName.setToolTipText("The name of the output of the step (e.g. name of a table in the database");
        layoutGenerator.addLabelWidgetPair("Ouput Name:", tableName, panel);

        JLabel creationStatusLabel = new JLabel();
        String tableCreationStatus = stepResult.getTableCreationStatus();
        creationStatusLabel.setText((tableCreationStatus != null) ? tableCreationStatus : "");
        layoutGenerator.addLabelWidgetPair("Run Status:", creationStatusLabel, panel);

        JLabel creationDateLabel = new JLabel();
        Date tableCreationDate = stepResult.getTableCreationDate();
        String creationDate = (tableCreationDate != null) ? EmfDateFormat.format_MM_DD_YYYY_HH_mm(tableCreationDate)
                : "";
        creationDateLabel.setText(creationDate);
        layoutGenerator.addLabelWidgetPair("Run Date:", creationDateLabel, panel);

        currentTable = new JCheckBox();
        currentTable.setEnabled(false);
        currentTable.setSelected(stepResult.isCurrentTable());
        currentTable.setToolTipText(
                "True when the source data and QA step have not been modified since the step was run");
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
        comments.setToolTipText(
                "Enter any notes of interest that you found when performing the step");

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
        layoutGenerator.addLabelWidgetPair("Version:", new Label(versionName + " (" + step.getVersion() + ")"), panel);

        program = new EditableComboBox(qaPrograms.names());
        program.setPrototypeDisplayValue("To make the combobox a bit wider");
        QAProgram qaProgram = step.getProgram();
        if (qaProgram != null)
            program.setSelectedItem(qaProgram.getName());
        else
            program.setSelectedItem(null);
        addChangeable(program);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);
        program.addActionListener(new AbstractAction()
          {
            public void actionPerformed(ActionEvent e) {
                currentTable.setSelected(false);
            }
        });
 
        programArguments = new TextArea("", step.getProgramArguments(), 40, 3);
        addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        layoutGenerator.addLabelWidgetPair("Arguments:", scrollableDetails, panel);
        programArguments.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                currentTable.setSelected(false);
            }
        });

        order = new NumberFormattedTextField(5, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        required = new CheckBox("", step.isRequired());
        if (step.isRequired())
            required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", step.getDescription(), 40, 3);
        addChangeable(description);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
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
        Button run = runButton();
        panel.add(run);

        Button viewResults = new Button("View Results", null);
        viewResults.setEnabled(false);
        panel.add(viewResults);

        Button export = exportButton();
        panel.add(export);
        return panel;
    }

    private Button runButton() {
        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doRun();
            }
        });
        return run;
    }

    private Button exportButton() {
        Button export = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doExport();
            }
        });
        return export;
    }

    protected void doRun() {
        try {
            messagePanel.setMessage("Started Run. Please monitor the Status window " + "to track your run request.");
            status.setSelectedItem("In Progress");
            date.setValue(new Date());
            who.setText(presenter.userName());

            presenter.run();

            saveButton.setEnabled(false);// to prevent user from entering and click save after started running a qa
                                            // step
            resetChanges();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
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
        // if (step.getTableCreationStatus().equals("In Progress"))
        // throw new EmfException("You can't save changes while running it");
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
        return step;
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            exportFolder.setText(mostRecentUsedFolder);
    }

    private void clear() {
        messagePanel.clear();
    }
}
