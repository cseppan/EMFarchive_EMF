package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.ui.ImageResources;

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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class StatusWindow extends ReusableInteralFrame implements StatusView {

    private MessagePanel messagePanel;

    private StatusTableModel statusTableModel;

    public StatusWindow(Container parent, JDesktopPane desktop) {
        super("Status", desktop);
        super.setName("statusWindow");

        position(parent);
        super.setContentPane(createLayout());

        super.setClosable(false);
        super.setMaximizable(false);
    }

    private JPanel createLayout() {
        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTable(), BorderLayout.CENTER);

        return layout;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        messagePanel = new SingleLineMessagePanel();
        container.add(messagePanel);
        
        JButton clearButton = createClearButton();
        getRootPane().setDefaultButton(clearButton);
        container.add(clearButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JButton createClearButton() {
        JButton button = new JButton(trashIcon());
        button.setName("clear");
        button.setBorderPainted(false);
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

    private ImageIcon trashIcon() {
        return new ImageResources().trash("Clear Messages");
    }

    private JScrollPane createTable() {
        statusTableModel = new StatusTableModel();
        JTable table = new JTable(statusTableModel);
        table.setName("statusMessages");
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
        super.dimensions(width, height);

        int x = 0;
        int y = (int) parentSize.getHeight() - height - 100;
        setLocation(x, y);
    }

    public void close() {       
        super.dispose();
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
