package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.ImageResources;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ImportWindow extends ReusableInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private JTextField name;

    private JTextField filename;

    private DefaultComboBoxModel datasetTypesModel;

    private DataCommonsService service;

    private JTextField folder;

    public ImportWindow(DataCommonsService service, JDesktopPane desktop) throws EmfException {
        super("Import Dataset", new Dimension(700, 275), desktop);
        super.setName("importWindow");
        this.service = service;

        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createImportPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createImportPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        DatasetType[] allDatasetTypes = service.getDatasetTypes();
        DatasetType[] allTypesWithMessage = new DatasetType[allDatasetTypes.length+1];
        copyDatasetTypes(allDatasetTypes,allTypesWithMessage);
        datasetTypesModel = new DefaultComboBoxModel(allTypesWithMessage);
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");
        layoutGenerator.addLabelWidgetPair("Dataset Type", datasetTypesComboBox, panel);

        name = new TextField("name", 35);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        JPanel chooser = new JPanel(new BorderLayout());
        folder = new TextField("folder", 35);
        chooser.add(folder, BorderLayout.LINE_START);
        chooser.add(importFileButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", chooser, panel);

        filename = new TextField("filename", 35);
        layoutGenerator.addLabelWidgetPair("Filename", filename, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        registerForEditEvents(name, folder, filename);// edit-awareness

        return panel;
    }

    private void copyDatasetTypes(DatasetType[] allDatasetTypes, DatasetType[] allTypesWithMessage) {
        allTypesWithMessage[0]=new DatasetType("Choose a type ...");
        for (int i = 0; i < allDatasetTypes.length; i++) {
            allTypesWithMessage[i+1]=allDatasetTypes[i];
        }
        
    }

    private JButton importFileButton() {
        Button button = new Button("Choose File", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                FileChooser chooser = new FileChooser("Import File", new File(folder.getText()), ImportWindow.this);
                File file = chooser.choose();
                if (file == null)
                    return;

                if (file.isDirectory()) {
                    folder.setText(file.getAbsolutePath());
                    filename.setText("");
                    return;
                }

                folder.setText(file.getParent());
                filename.setText(file.getName());
//For demo #3 changing the filename
//                name.setText(formatDatasetName(file.getName()));
              name.setText(file.getName());
            }
        });

        Icon icon = new ImageResources().open("Import a File");
        button.setIcon(icon);

        return button;
    }

    private void registerForEditEvents(JTextField name, JTextField directory, JTextField filename) {
        name.getDocument().addDocumentListener(notifyBeginInput());
        directory.getDocument().addDocumentListener(notifyBeginInput());
        filename.getDocument().addDocumentListener(notifyBeginInput());
    }

    private DocumentListener notifyBeginInput() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                if (presenter != null)
                    presenter.notifyBeginInput();
            }

            public void removeUpdate(DocumentEvent event) {
                if (presenter != null)
                    presenter.notifyBeginInput();
            }

            public void changedUpdate(DocumentEvent event) {// ignore
            }
        };
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton importButton = new Button("Import", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessagePanel();
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    public void register(ImportPresenter presenter) {
        this.presenter = presenter;
    }

    private void doImport() {
        try {
            presenter.doImport(folder.getText(), filename.getText(), name.getText(), (DatasetType) datasetTypesModel
                    .getSelectedItem());
            String message = "Started import. Please monitor the Status window to track your Import request.";
            messagePanel.setMessage(message);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void clearMessagePanel() {
        messagePanel.clear();
        super.refreshLayout();
    }

    public void setDefaultBaseFolder(String folder) {
        this.folder.setText(folder);
    }
}
