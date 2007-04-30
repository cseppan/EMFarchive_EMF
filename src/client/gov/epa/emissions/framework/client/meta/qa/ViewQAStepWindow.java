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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ViewQAStepWindow extends DisposableInteralFrame implements QAStepView {
    
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.PATTERN_yyyyMMddHHmm);

    private EditableComboBox program;

    private TextArea programArguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private ViewQAStepPresenter presenter;

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

    private EmfConsole parentConsole;

    private EmfSession session;

    public ViewQAStepWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("View QA Step", new Dimension(650, 625), desktopManager);
        this.parentConsole = parentConsole;
        this.session = session;
    }

    public void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, User user,
            String versionName) {
        this.step = step;
        this.qaStepResult = qaStepResult;
        this.user = user;
        this.qaPrograms = new QAPrograms(null, programs);
        super.setLabel(super.getTitle() + ": " + step.getName() + " - " + dataset.getName() + " (v" + step.getVersion()
                + ")");

        JPanel layout = createLayout(step, qaStepResult, versionName);
        super.getContentPane().add(layout);
        super.display();
    }

    public void windowClosing() {
        doClose();
    }

    public void observe(ViewQAStepPresenter presenter) {
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

        JCheckBox currentTable = new JCheckBox();
        currentTable.setEnabled(false);
        currentTable.setSelected(stepResult.isCurrentTable());
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
        config.setToolTipText("Enter the name of the Dataset that is the configuration "
                + "file (e.g., a REPCONFIG file)");
        layoutGenerator.addLabelWidgetPair("Configuration:", config, panel);

        comments = new TextArea("Comments", step.getComments(), 40, 2);
        addChangeable(comments);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(comments);
        layoutGenerator.addLabelWidgetPair("Comments:", scrollableComment, panel);

        layoutGenerator.addLabelWidgetPair("Folder:", exportFolderPanel(step), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;

    }

    private JPanel exportFolderPanel(QAStep step) {
        exportFolder = new JTextField(40);
        exportFolder.setName("folder");
        String outputFolder = step.getOutputFolder();
        exportFolder.setText(outputFolder != null ? outputFolder : "");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(10, 10));
        folderPanel.add(exportFolder);
        folderPanel.add(button, BorderLayout.EAST);
        return folderPanel;
    }

//    private void selectFolder() {
//        FileChooser chooser = new FileChooser("Select Folder", new File(exportFolder.getText()), this);
//
//        chooser.setTitle("Select a folder");
//        File[] file = chooser.choose();
//        if (file == null || file.length == 0)
//            return;
//
//        if (file[0].isDirectory()) {
//            exportFolder.setText(file[0].getAbsolutePath());
//        }
//
//        if (file[0].isFile()) {
//            exportFolder.setText(file[0].getParent());
//        }
//    }
    
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

        who = new TextField("who", step.getWho(), 10);
        addChangeable(who);
        layoutGenerator.addLabelWidgetPair("User:", who, panel);

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
        program.setEditable(false);
        program.setPrototypeDisplayValue("To make the combobox a bit wider");
        QAProgram qaProgram = step.getProgram();
        if (qaProgram != null)
            program.setSelectedItem(qaProgram.getName());
        else
            program.setSelectedItem(null);
        addChangeable(program);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        programArguments = new TextArea("", step.getProgramArguments(), 40, 3);
        addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        layoutGenerator.addLabelWidgetPair("Arguments:", scrollableDetails, panel);

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
        Button cancel = closeButton();
        panel.add(cancel);

        Button viewResults = new Button("View Results", null);
        viewResults.setEnabled(false);
        panel.add(viewResults);

        Button export = exportButton();
        panel.add(export);
        return panel;
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

    protected void doExport() {
        try {
            messagePanel.setMessage("Started Export. Please monitor the Status window "
                    + "to track your export request.");
            presenter.doExport(step, qaStepResult, exportFolder.getText());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
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
            presenter.doClose();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            exportFolder.setText(mostRecentUsedFolder);
    }

    private void clear() {
        messagePanel.clear();
    }

}
