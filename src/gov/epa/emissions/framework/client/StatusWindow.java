package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ConcurrentTaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

public class StatusWindow extends EmfInteralFrame implements StatusView {

    private StatusPresenter presenter;

    private MessagePanel messagePanel;

    private StatusTableModel statusTableModel;

    public StatusWindow(User user, StatusServices statusServices, Container parent) {
        super("Status");

        position(parent);
        super.setContentPane(createLayout());

        super.setClosable(false);
        super.setIconifiable(true);
        super.setMaximizable(false);
        super.setResizable(false);

        this.presenter = new StatusPresenter(user, statusServices, this);
        this.presenter.start(new ConcurrentTaskRunner());
    }

    private JPanel createLayout() {
        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messagePanel, BorderLayout.EAST);
        layout.add(panel, BorderLayout.NORTH);

        layout.add(createTable(), BorderLayout.CENTER);

        return layout;
    }

    private JScrollPane createTable() {
        statusTableModel = new StatusTableModel();
        JTable table = new JTable(statusTableModel);
        setColumnWidths(table.getColumnModel());
        
        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(this.getSize());

        return scrollPane;
    }

    private void setColumnWidths(TableColumnModel model) {
        int width = getWidth();
        
    }

    private void position(Container parent) {
        Dimension parentSize = parent.getSize();

        int width = (int) parentSize.getWidth() - 5;
        int height = 150;
        setSize(width, height);

        int x = 0;
        int y = (int) parentSize.getHeight() - height - 50;
        setLocation(x, y);
    }

    public void close() {
        super.dispose();
    }

    public void display() {
        super.setVisible(true);
    }

    public void update(Status[] statuses) {
        messagePanel.setMessage("Last Update : " + new Date(), Color.GRAY);
        statusTableModel.refresh(statuses);

        super.revalidate();//TODO: invalidate ? validate ?
    }

    public void notifyError(String message) {
        messagePanel.setError(message);
    }

}
