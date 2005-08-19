package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ConcurrentTaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JPanel;

public class StatusWindow extends EmfInteralFrame implements StatusView {

    private StatusPresenter presenter;
    private MessagePanel messagePanel;

    public StatusWindow(User user, StatusServices statusServices, Container parent) {
        super("Status Messages");

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
        
        messagePanel = new MessagePanel();
        layout.add(messagePanel);
        
        return layout;
    }

    private void position(Container parent) {
        Dimension parentSize = parent.getSize();

        int width = (int) parentSize.getWidth() - 5;
        int height = 100;
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

    public void update(Status[] messages) {
        messagePanel.setMessage("Last Update @ " + new Date());
    }

    public void notifyError(String message) {
        messagePanel.setError(message);
    }

}
