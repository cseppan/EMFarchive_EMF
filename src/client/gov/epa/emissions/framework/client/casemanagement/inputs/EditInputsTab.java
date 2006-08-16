package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
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

    private ManageChangeables changeables;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;
    
    private int count = 0;
    
    private JTextField inputDir;
    
    private JTextField outputDir;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        
        this.inputDir = new JTextField(20);
        inputDir.setName("inputdir");

        this.outputDir = new JTextField(20);
        outputDir.setName("outputdir");

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditInputsTabPresenter presenter) {
        super.removeAll();
        super.add(createLayout(caseObj.getCaseInputs(), presenter, parentConsole), BorderLayout.CENTER);
        this.caseObj = caseObj;
        this.presenter = presenter;
    }

    private void doRefresh(CaseInput[] inputs) {
        inputDir.setText(caseObj.getInputFileDir());
        outputDir.setText(caseObj.getOutputFileDir());
        
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
        changeables.addChangeable(tableData);
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
        layoutGenerator.addLabelWidgetPair("Output Folder:", getFolderChooserPanel(outputDir, "output folder"), panel);
        
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        
        return panel;
    }
    
    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button button = new Button("Choose Folder", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
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
        if(option == JFileChooser.APPROVE_OPTION){
            if (title.startsWith("input")) {
                caseObj.setInputFileDir("" + chooser.getSelectedFile());
                dir.setText("" + chooser.getSelectedFile());
            }
            
            if (title.startsWith("output")) {
                caseObj.setOutputFileDir("" + chooser.getSelectedFile());
                dir.setText("" + chooser.getSelectedFile());
            }
        }
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewInput(presenter);
            }
        });
        container.add(add);

        Button remove = new Button("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        });
        container.add(remove);

        Button edit = new Button("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doEditInput(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        container.add(edit);

        Button view = new Button("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(view);

        final JCheckBox showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                    //doRefresh(showAll);
            }
        });
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
            tableData.remove((CaseInput[])inputs.toArray(new CaseInput[0]));
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
            EditInputView inputEditor = new EditInputWindow( 
                    input.getName() + " (" + caseObj.getName() + ")(" 
                    + ++count + ")", desktopManager);
            presenter.doEditInput(input, inputEditor);
        }
    }

    public void addInput(CaseInput note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

//    public void refresh() {
//        tableData.refresh();
//        selectModel.refresh();
//        
//        tablePanel.removeAll();
//        tablePanel.add(createSortFilterPanel(parentConsole));
//    }
//
//    private void refreshShowables() {
//        tableData.refreshShowables();
//        selectModel.refresh();
//        
//        tablePanel.removeAll();
//        tablePanel.add(createSortFilterPanel(parentConsole));
//    }
//    
//    private void doRefresh(JCheckBox showAll) {
//        if (showAll.isSelected())
//            refresh();
//        else
//            refreshShowables();
//    }

    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

}
