package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DataServices;
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
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
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

    private JFrame parentConsole;

    private DataServices dataServices;

    public SectorsManagerWindow(JFrame parentConsole, JDesktopPane desktop) {
        super("Sectors Manager", new Dimension(475, 300), desktop);
        this.parentConsole = parentConsole;
        this.desktop = desktop;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(SectorsManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DataServices dataServices) throws EmfException {
        this.dataServices = dataServices;
        doLayout(dataServices.getSectors());
        super.display();
    }

    public void refresh() {
        try {
            doLayout(dataServices.getSectors());
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

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateSectors();
            }
        });

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(updateButton);

        return crudPanel;
    }

    private void updateSectors() {
        List sectors = selected();
        // TODO: move it into Presenter - look at UserManagerWindow
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            presenter.doUpdate(sector, updateSectorView());
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

    // FIXME: this table refresh sequence applies to every CRUD panel. Refactor
    private UpdateSectorView updateSectorView() {
        UpdateSectorWindow view = new UpdateSectorWindow(this);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doTableRefresh();
            }
        });

        return view;
    }

}
