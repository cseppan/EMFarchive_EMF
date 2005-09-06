package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ExportWindow extends EmfInteralFrame implements ExportView {

    private EmfDataset dataset;

    private SingleLineMessagePanel messagePanel;

    private JTextField filename;

    private ExportPresenter presenter;

    public ExportWindow(EmfDataset dataset) throws EmfException {
        super("Export a dataset");
        this.dataset = dataset;

        super.setSize(new Dimension(600, 175));

        JPanel layoutPanel = createLayout();
        this.getContentPane().add(layoutPanel);
    }

    public void display() {
        this.setVisible(true);
    }

    public void register(ExportPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        this.dispose();
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createExportPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createExportPanel() throws EmfException {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(2, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Dataset"));
        labelsPanel.add(new JLabel("Filename"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(2, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        valuesPanel.add(new JLabel(dataset.getName()));
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

        JButton exportButton = new JButton("Export");
        exportButton.setName("export");
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter == null)
                    return;

                clearMessagePanel();
                doExport();
            }

        });
        container.add(exportButton);

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

    private void refresh() {
        super.validate();
    }

    private void doExport() {
        try {
            presenter.notifyExport(dataset, filename.getText());
            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

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
