package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//FIXME: very similar to SectorsManager. Refactor ?
public class DatasetTypesManagerWindow extends ReusableInteralFrame implements DatasetTypesManagerView {

    private DatasetTypesManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private JFrame parentConsole;

    private DatasetTypesServices services;

    public DatasetTypesManagerWindow(JFrame parentConsole, JDesktopPane desktop) {
        super("DatasetTypes Manager", desktop);
        this.parentConsole = parentConsole;
        this.desktop = desktop;

        layout = new JPanel();
        this.getContentPane().add(layout);
        this.setSize(new Dimension(600, 300));
    }

    public void observe(DatasetTypesManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh() {
        try {
            doLayout(services.getDatasetTypes());
        } catch (EmfException e) {
            messagePanel.setError("Could not refresh. Problem communicating with remote services.");
        }

        super.refreshLayout();
    }

    public void display(DatasetTypesServices services) throws EmfException {
        this.services = services;

        doLayout(services.getDatasetTypes());
        super.display();
    }

    private void doLayout(DatasetType[] types) {
        model = new EmfTableModel(new DatasetTypesTableData(types));
        selectModel = new SortFilterSelectModel(model);
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        createLayout(layout, sortFilterSelectPanel);
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        return new JPanel();
    }

}
