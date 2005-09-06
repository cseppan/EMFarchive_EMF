package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ConcurrentTaskRunner;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class StatusWindow extends EmfInteralFrame implements StatusView {

    private StatusPresenter presenter;

    private MessagePanel messagePanel;

    private StatusTableModel statusTableModel;

    private TaskRunner taskRunner;

    public StatusWindow(User user, StatusServices statusServices, Container parent) {
        super("Status");

        position(parent);
        super.setContentPane(createLayout());

        super.setClosable(false);
        super.setIconifiable(true);
        super.setMaximizable(false);
        super.setResizable(true);

        this.presenter = new StatusPresenter(user, statusServices, this);
        taskRunner = new ConcurrentTaskRunner();
        this.presenter.start(taskRunner);
    }

    private JPanel createLayout() {
        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTable(), BorderLayout.CENTER);

        return layout;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        messagePanel = new SingleLineMessagePanel();
        container.add(messagePanel);
        container.add(createClearButton());

        panel.add(container, BorderLayout.EAST);
        return panel;
    }

    private JButton createClearButton() {
        Icon icon = new ImageIcon("images/green.gif");

        JButton button = new JButton("Clear", icon);
        button.setToolTipText("Clears the Status messages");
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // FIXME: notify presenter about 'clear', and let the presenter
                // communicate the action to the 'table-model'
                statusTableModel.clear();
            }
        });

        return button;
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
        TableColumn message = model.getColumn(1);
        message.setPreferredWidth((int) (getWidth() * 0.75));
    }

    private void position(Container parent) {
        Dimension parentSize = parent.getSize();

        int width = (int) parentSize.getWidth() - 5;
        int height = 150;
        setSize(width, height);

        int x = 0;
        int y = (int) parentSize.getHeight() - height - 100;
        setLocation(x, y);
    }

    public void close() {
        taskRunner.stop();
        super.dispose();
    }

    public void display() {
        super.setVisible(true);
    }

    public void update(Status[] statuses) {
        DateFormat format = new SimpleDateFormat("hh:mm:ss MM/dd/yyyy");
        messagePanel.setMessage("Last Update : " + format.format(new Date()), Color.GRAY);
        statusTableModel.refresh(statuses);

        super.revalidate();
    }

    public void notifyError(String message) {
        messagePanel.setError(message);
    }

}
