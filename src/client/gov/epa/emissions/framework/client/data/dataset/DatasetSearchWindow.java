package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DatasetSearchWindow extends ReusableInteralFrame {

    private EmfConsole parent;

    private DatasetsBrowserPresenter presenter;

    private MessagePanel messagePanel;

    private TextField name;

    private TextField desc;

    private ComboBox keyword;

    private TextField value;

    public DatasetSearchWindow(String title, EmfConsole parentConsole, DesktopManager desktopManager) {
        super(title, new Dimension(400, 260), desktopManager);
        parent = parentConsole;
    }

    public void display() {
        getContentPane().add(createLayout());
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        try {
            panel.add(createSearchPanel(), BorderLayout.CENTER);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private Component createSearchPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGen = new SpringLayoutGenerator();

        name = new TextField("namefilter", 18);
        desc = new TextField("descfilter", 18);
        keyword = new ComboBox("Select one", presenter.getKeywords());
        value = new TextField("keyvalue", 18);

        layoutGen.addLabelWidgetPair("Name contains:", name, panel);
        layoutGen.addLabelWidgetPair("Description contains:", desc, panel);
        layoutGen.addLabelWidgetPair("Keyword:", keyword, panel);
        layoutGen.addLabelWidgetPair("Keyword value:", value, panel);

        // Lay out the panel.
        layoutGen.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        Button okButton = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    messagePanel.clear();
                    EmfDataset[] datasets = search(presenter.getDSType(), getDataset());

                    if (datasets.length > 100) {
                        String msg = "Number of datasets > 100. Would you like to continue?";
                        int option = JOptionPane.showConfirmDialog(parent, msg, "Warning", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (option == JOptionPane.NO_OPTION)
                            return;
                    }

                    presenter.refreshView(datasets);
                } catch (EmfException e) {
                    if (e.getMessage().length() > 100)
                        messagePanel.setError(e.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e.getMessage());
                }
            }
        });
        okButton.setToolTipText("Search similar dataset(s)");
        panel.add(okButton);

        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        panel.add(closeButton, BorderLayout.LINE_END);
        getRootPane().setDefaultButton(okButton);

        controlPanel.add(panel, BorderLayout.CENTER);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        return controlPanel;
    }

    protected EmfDataset getDataset() {
        EmfDataset ds = new EmfDataset();
        ds.setDatasetType(presenter.getDSType());
        ds.setName(name.getText());
        ds.setDescription(desc.getText());
        Keyword kw = (Keyword) keyword.getSelectedItem();

        if (kw != null) {
            KeyVal kv = new KeyVal(kw, value.getText());
            ds.setKeyVals(new KeyVal[] { kv });
        }

        return ds;
    }

    public EmfDataset[] search(DatasetType type, EmfDataset dataset) throws EmfException {
        return presenter.advSearch4Datasets(type, dataset);
    }

    public void observe(DatasetsBrowserPresenter presenter) {
        this.presenter = presenter;
    }

}
