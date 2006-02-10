package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

//FIXME: look at the common design b/w this and UserManagerWindow. Refactor ?
public class SectorsManagerWindow extends ReusableInteralFrame implements SectorsManagerView {

    private SectorsManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public SectorsManagerWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Sector Manager", new Dimension(475, 300), parentConsole.desktop(), desktopManager);
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(SectorsManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DataCommonsService service) throws EmfException {
        this.service = service;
        doLayout(service.getSectors());
        super.display();
    }

    public void refresh() {
        try {
            doLayout(service.getSectors());
        } catch (EmfException e) {
            messagePanel.setError("Could not refresh. Problem communicating with remote services.");
        }

        super.refreshLayout();
    }

    // FIXME: this table refresh sequence applies to every SortFilterTableModel.
    // Refactor
    private void doTableRefresh() {
        model.refresh();
        selectModel.refresh();
        super.refreshLayout();
    }

    private void doLayout(Sector[] sectors) {
        model = new EmfTableModel(new SectorsTableData(sectors));
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
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewSectors();
            }

        };

        String message = "Opening too many windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, selectModel, confirmDialog);
        crudPanel.add(viewButton);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editSectors();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, selectModel, confirmDialog);
        crudPanel.add(editButton);

        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                createNewSector();
            }
        });
        crudPanel.add(newButton);

        return crudPanel;
    }

    private void viewSectors() {
        List sectors = selected();
        // TODO: move it into Presenter - look at UserManagerWindow
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            presenter.doView(sector, displaySectorView());
        }
    }

    private void editSectors() {
        List sectors = selected();
        // TODO: move it into Presenter - look at UserManagerWindow
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            try {
                presenter.doEdit(sector, editSectorView(), displaySectorView());
            } catch (EmfException e) {
                setError("Could not edit Sector: " + sector.getName() + ". Reason: " + e.getMessage());
            }
        }
    }

    private void createNewSector() {
        Sector sector = new Sector("New Sector", "New Sector");
        presenter.displayNewSector(sector, newSectorView());
    }

    private void setError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
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

    // FIXME: this table refresh sequence applies to every CRUD panel. Refactor
    private ViewSectorWindow displaySectorView() {
        ViewSectorWindow view = new ViewSectorWindow(desktopManager);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

    private EditableSectorView editSectorView() {
        EditSectorWindow view = new EditSectorWindow(this, desktopManager);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

    private NewSectorView newSectorView() {
        NewSectorWindow view = new NewSectorWindow(this, desktopManager);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

}
