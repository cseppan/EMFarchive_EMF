package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.MultiLineMessagePanel;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ImportWindow extends EmfInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private JTextField name;

    private JTextField filename;

    private DefaultComboBoxModel datasetTypesModel;

    private ExImServices eximServices;

    public ImportWindow(User user, ExImServices eximServices) throws EmfException {
        super("Import Dataset");
        this.eximServices = eximServices;

        setSize(new Dimension(500, 275));

        JPanel layoutPanel = createLayout();
        this.getContentPane().add(layoutPanel);
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new MultiLineMessagePanel(new Dimension(400, 100));
        panel.add(messagePanel);
        panel.add(createImportPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createImportPanel() throws EmfException {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(3, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Dataset Type"));
        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(new JLabel("Filename"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(3, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);
        datasetTypesModel = new DefaultComboBoxModel(eximServices.getDatasetTypes());
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        valuesPanel.add(datasetTypesComboBox);

        name = new JTextField(15);
        name.setName("name");
        valuesPanel.add(name);
        filename = new JTextField(35);
        filename.setName("filename");
        valuesPanel.add(filename);

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton importButton = new JButton("Import");
        importButton.setName("import");
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter == null)
                    return;

                clearMessagePanel();
                doImport();
            }

        });
        container.add(importButton);

        JButton done = new JButton("Done");
        done.setName("done");
        done.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null)
                    presenter.notifyDone();
            }

        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    public void register(ImportPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.dispose();
    }

    public void display() {
        super.setVisible(true);
    }

    private void refresh() {
        super.validate();
    }

    private void doImport() {
        try {
            presenter.notifyImport(filename.getText(), name.getText(), (DatasetType) datasetTypesModel.getSelectedItem());
            String message = "Started importing " + name.getText() + " [ " + filename.getText() + " ]...."
                    + "Please monitor the Status window to track your Import request.";
            messagePanel.setMessage(message);

            name.setText("");
            filename.setText("");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }
}
