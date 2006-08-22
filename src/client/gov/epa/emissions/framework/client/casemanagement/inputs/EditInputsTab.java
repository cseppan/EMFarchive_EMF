package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditInputsTab extends JPanel implements EditInputsTabView {

    private EmfConsole parentConsole;

    private EditInputsTabPresenter presenter;

    Case caseObj;

    private InputsTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private JTextField inputDir;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;

        this.inputDir = new JTextField(30);
        inputDir.setName("inputdir");

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditInputsTabPresenter presenter) {
        super.removeAll();
        inputDir.setText(caseObj.getInputFileDir());
        super.add(createLayout(caseObj.getCaseInputs(), presenter, parentConsole), BorderLayout.CENTER);
        this.caseObj = caseObj;
        this.presenter = presenter;
    }

    private void doRefresh(CaseInput[] inputs) {
        inputDir.setText(caseObj.getInputFileDir());

        super.removeAll();
        super.add(createLayout(inputs, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseInput[] inputs, EditInputsTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        tableData = new InputsTableData(inputs);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    public JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", getFolderChooserPanel(inputDir, "input folder"), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button button = new Button("Choose Folder", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(dir);
        folderPanel.add(button, BorderLayout.EAST);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        JFileChooser chooser = new JFileChooser(new File(dir.getText()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Please select the " + title);

        int option = chooser.showDialog(this, "Select");
        if (option == JFileChooser.APPROVE_OPTION) {
            caseObj.setInputFileDir("" + chooser.getSelectedFile());
            dir.setText("" + chooser.getSelectedFile());
        }
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessagePanel();
                doNewInput(presenter);
            }
        });
        container.add(add);

        Button remove = new RemoveButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessagePanel();
                doRemove();
            }
        });
        container.add(remove);

        Button edit = new EditButton("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessagePanel();
                    doEditInput(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        container.add(edit);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessagePanel();
            }
        });
        container.add(view);

        final JCheckBox showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // doRefresh(showAll);
                clearMessagePanel();
            }
        });
        showAll.setEnabled(false);
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doNewInput(EditInputsTabPresenter presenter) {
        NewInputDialog view = new NewInputDialog(parentConsole);
        try {
            presenter.doAddInput(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void doRemove() {
        List inputs = selectModel.selected();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select an input item.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected template(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove((CaseInput[]) inputs.toArray(new CaseInput[0]));
            refresh();
        }
    }

    private void doEditInput(EditInputsTabPresenter presenter) throws EmfException {
        List inputs = selectModel.selected();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select an input item.");
            return;
        }

        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = (CaseInput) iter.next();
            EditCaseInputView inputEditor = new EditCaseInputWindow(input.getName() 
                    + "(" + input.getRecordID() + ")(" + caseObj.getName() + ")", desktopManager);
            presenter.doEditInput(input, inputEditor);
        }
    }

    public void addInput(CaseInput note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    private void clearMessagePanel() {
        messagePanel.clear();
    }
    
    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

    public void checkDuplicate(CaseInput input) throws EmfException {
        presenter.doCheckDuplicate(input, tableData.sources());
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

}
