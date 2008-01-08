package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlMeasureEditDialog extends JDialog implements ControlMeasureEditView {

    // private EmfConsole parent;

    private ControlMeasureEditPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    // private ManageChangeables changeables;

    private DoubleTextField applyOrder, rPenetration, rEffective;

    private NumberFieldVerifier verifier;

    private ComboBox version, dataset;

    private CheckBox rpOverride, reOverride;

    public ControlMeasureEditDialog(EmfConsole parent) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.setModal(true);
        this.verifier = new NumberFieldVerifier("Measure properties: ");
        // this.parent = parent;
    }

    public void display() {

        messagePanel = new SingleLineMessagePanel();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
        contentPane.add(messagePanel, BorderLayout.PAGE_START);

        try {
            contentPane.add(createLowerSection(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        setTitle("Control Strategy Measures Editor");
        this.pack();
        this.setSize(700, 260);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPropertySection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySectionRight(), BorderLayout.CENTER);
        panel.add(createPropertySectionLeft(), BorderLayout.WEST);
        return panel;
    }

    private JPanel createPropertySectionRight() throws EmfException {
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Regions"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
        EmfDataset[] datasets = presenter.getDatasets(presenter.getDatasetType("List of Counties (CSV)"));
        EmfDataset blankDataset = new EmfDataset();
        blankDataset.setName("None");
        datasetList.add(blankDataset);
        datasetList.addAll(Arrays.asList(datasets));
        dataset = new ComboBox("Not selected", datasetList.toArray());
        Dimension size = new Dimension(300, 10);
        dataset.setPreferredSize(size);

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions((EmfDataset) dataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

        version = new ComboBox(new Version[0]);
        version.setPreferredSize(size);
        try {
            fillVersions((EmfDataset) dataset.getSelectedItem());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

        layoutGenerator.addLabelWidgetPair("Version:", version, panel);
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        outerPanel.add(panel, BorderLayout.NORTH);
        return outerPanel;
    }

    private void fillVersions(EmfDataset dataset) throws EmfException {
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None"))
            dataset = null;
        Version[] versions = presenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    private JPanel createPropertySectionLeft() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Measure Properties"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Set Order:", applyOrderField(), panel);
        layoutGenerator.addLabelWidgetPair("Set RP %:", rPField(), panel);
        rpOverride = new CheckBox("");
        rpOverride.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    rPenetration.setEnabled(true);
                } else {
                    rPenetration.setEnabled(false);
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Use RP from measure:", rpOverride, panel);
        layoutGenerator.addLabelWidgetPair("Set RE %:", rEField(), panel);
        reOverride = new CheckBox("");
        reOverride.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    rEffective.setEnabled(true);
                } else {
                    rEffective.setEnabled(false);
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Use RE from measure:", reOverride, panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return panel;
    }

    private DoubleTextField applyOrderField() {
        applyOrder = new DoubleTextField("Set Order", 10);
        applyOrder.setText("");
        return applyOrder;
    }

    private DoubleTextField rPField() {
        rPenetration = new DoubleTextField("Set RP %", 1, 100, 10);
        rPenetration.setText("");
        return rPenetration;
    }

    private DoubleTextField rEField() {
        rEffective = new DoubleTextField("Set RE %", 1, 100, 10);
        rEffective.setText("");
        return rEffective;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    edit();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private void edit() throws EmfException {
        messagePanel.clear();
        EmfDataset ds = (EmfDataset) dataset.getSelectedItem();
        if (ds == null) {
            ds = null;
        }
        Version ver = (ds != null ? (Version) version.getSelectedItem() : null);
        Integer verValue = (ver != null ? ver.getVersion() : null);
        presenter.doEdit(validateApplyOrder(applyOrder), validatePercentage(rPenetration), rpOverride.isSelected(),
                validatePercentage(rEffective), reOverride.isSelected(), ds, verValue);
        setVisible(false);
        dispose();
    }

    private Double validatePercentage(DoubleTextField value) throws EmfException {
        if (value.getText().trim().length() == 0) {
            return null;
        }
        double value1 = verifier.parseDouble(value.getText());

        // make sure the number makes sense...
        if (value1 < 1 || value1 > 100) {
            throw new EmfException(value.getName() + ":  Enter a number between 1 and 100");
        }
        return value1;
    }

    private Double validateApplyOrder(DoubleTextField value) throws EmfException {
        if (value.getText().trim().length() == 0) {
            return null;
        }
        return verifier.parseDouble(value.getText());
    }

    public void observe(ControlMeasureEditPresenter presenter) {
        this.presenter = presenter;
    }

}
