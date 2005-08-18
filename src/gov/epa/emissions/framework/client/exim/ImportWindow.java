package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.ErrorMessagePanel;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

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

public class ImportWindow extends EmfInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private ErrorMessagePanel errorMessagePanel;

    private JTextField name;

    private JTextField filename;

    public ImportWindow(User user, ExImServices eximServices) {
        super("Import Dataset");

        JPanel layoutPanel = createLayout();
        this.getContentPane().add(layoutPanel);

        setSize(new Dimension(350, 200));
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        errorMessagePanel = new ErrorMessagePanel();
        panel.add(errorMessagePanel);
        panel.add(createImportPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createImportPanel() {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(2, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        labelsPanel.add(new JLabel("Name"));
        labelsPanel.add(new JLabel("Filename"));

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(2, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        name = new JTextField(10);
        name.setName("name");
        valuesPanel.add(name);
        filename = new JTextField(10);
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
    }

    public void display() {
        super.setVisible(true);
    }

}
