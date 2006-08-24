package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditOutputsTab extends JPanel implements EditOutputsTabView {

    private EmfConsole parentConsole;

    private EditOutputsTabPresenter presenter;

    private Case caseObj;

    private OutputsTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private ManageChangeables changeables;

    private JTextField outputDir;

    public EditOutputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;

        this.outputDir = new JTextField(20);
        outputDir.setName("outputdir");

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditOutputsTabPresenter presenter) {
        super.removeAll();
        outputDir.setText(caseObj.getOutputFileDir());
        super.add(createLayout(new CaseInput[0], presenter, parentConsole), BorderLayout.CENTER);
        this.caseObj = caseObj;
        this.presenter = presenter;
    }

    private void doRefresh(CaseInput[] inputs) {
        outputDir.setText(caseObj.getOutputFileDir());

        super.removeAll();
        super.add(createLayout(inputs, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseInput[] inputs, EditOutputsTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        tableData = new OutputsTableData(inputs);
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

        layoutGenerator.addLabelWidgetPair("Output Folder:", getFolderChooserPanel(outputDir, "output folder"), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(dir);
        folderPanel.add(browseButton, BorderLayout.EAST);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        JFileChooser chooser = new JFileChooser(new File(dir.getText()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Please select the " + title);

        int option = chooser.showDialog(this, "Select");
        if (option == JFileChooser.APPROVE_OPTION) {
            caseObj.setOutputFileDir("" + chooser.getSelectedFile());
            dir.setText("" + chooser.getSelectedFile());
        }
    }

    private JPanel controlPanel(final EditOutputsTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(remove);

        Button defaultButton = new Button("Defaults", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(defaultButton);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(edit);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(view);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void refresh() {
        doRefresh(tableData.sources());
    }

    public void saveCaseOutputFileDir() {
        caseObj.setOutputFileDir(outputDir.getText());
    }

}
