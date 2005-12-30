package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

//FIXME: very similar to SectorsManager. Refactor ?
public class DatasetTypesManagerWindow extends ReusableInteralFrame implements DatasetTypesManagerView {

    private DatasetTypesManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private DatasetTypeService services;

    public DatasetTypesManagerWindow(EmfConsole parentConsole) {
        super("DatasetType Manager", new Dimension(600, 300), parentConsole.desktop());
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
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

    public void display(DatasetTypeService services) throws EmfException {
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
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        JButton viewButton = new JButton("View");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewDatasetTypes();
            }
        });
        crudPanel.add(viewButton);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editDatasetTypes();
            }
        });
        crudPanel.add(editButton);

        return crudPanel;
    }

    private void viewDatasetTypes() {
        List sectors = selected();
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            DatasetType type = (DatasetType) iter.next();
            try {
                presenter.doView(type, viewableView());
            } catch (EmfException e) {
                messagePanel.setError("Could not display: " + type.getName() + ". Reason: " + e.getMessage());
                break;
            }
        }
    }

    private void editDatasetTypes() {
        List sectors = selected();
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            DatasetType type = (DatasetType) iter.next();
            try {
                presenter.doEdit(type, editableView());
            } catch (EmfException e) {
                messagePanel.setError("Could not display: " + type.getName() + ". Reason: " + e.getMessage());
                break;
            }
        }
    }

    // generic. Could be moved into 'SortFilterSelectModel' ? - FIXME
    private List selected() {
        List elements = new ArrayList();

        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return elements;
        for (int i = 0; i < selected.length; i++) {
            elements.add(model.element(selected[i]));
        }

        return elements;
    }

    private ViewableDatasetTypeWindow viewableView() {
        ViewableDatasetTypeWindow view = new ViewableDatasetTypeWindow();
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

    private EditableDatasetTypeView editableView() {
        EditDatasetTypeWindow view = new EditDatasetTypeWindow(this);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

    // FIXME: this table refresh sequence applies to every SortFilterTableModel.
    // Refactor
    private void doTableRefresh() {
        model.refresh();
        selectModel.refresh();
        super.refreshLayout();
    }
}
