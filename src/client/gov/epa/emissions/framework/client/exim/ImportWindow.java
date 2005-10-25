package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DatasetTypesServices;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ImportWindow extends ReusableInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private JTextField name;

    private JTextField filename;

    private DefaultComboBoxModel datasetTypesModel;

    private DatasetTypesServices datasetTypesService;

    private JTextField folder;

    public ImportWindow(DatasetTypesServices eximServices, JDesktopPane desktop) throws EmfException {
        super("Import Dataset", desktop);
        super.setName("importWindow");
        this.datasetTypesService = eximServices;

        setSize(new Dimension(600, 275));

        JPanel layoutPanel = createLayout();
        this.getContentPane().add(layoutPanel);
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
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(4, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Dataset Type"));
        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(new JLabel("Folder"));
        labelsPanel.add(new JLabel("Filename"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(4, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);
        datasetTypesModel = new DefaultComboBoxModel(datasetTypesService.getDatasetTypes());
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");
        valuesPanel.add(datasetTypesComboBox);

        name = new TextField("name", 15);
        valuesPanel.add(name);

        folder = new TextField("folder", 35);
        valuesPanel.add(folder);

        filename = new TextField("filename", 35);
        valuesPanel.add(filename);

        registerForEditEvents(name, folder, filename);

        panel.add(valuesPanel);

        return panel;
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
                if (presenter == null)
                    return;

                clearMessagePanel();
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null)
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

    private void refresh() {
        super.validate();
    }

    private void doImport() {
        try {
            presenter.doImport(folder.getText(), filename.getText(), name.getText(),
                    (DatasetType) datasetTypesModel.getSelectedItem());
            String message = "Started import. Please monitor the Status window to track your Import request.";
            messagePanel.setMessage(message);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }

    public void setDefaultBaseFolder(String folder) {
        this.folder.setText(folder);
    }
}
