package gov.epa.emissions.framework.client.data;

import java.awt.Dimension;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.EmfInternalFrame;

import javax.swing.JPanel;

public class UpdateSectorWindow extends EmfInternalFrame implements UpdateSectorView {

    private UpdateSectorPresenter presenter;

    private JPanel layout;

    public UpdateSectorWindow() {
        super("Update Sector");

        layout = new JPanel();
        layout.add(createLayout());
        super.getContentPane().add(layout);

        super.setResizable(false);
        super.setSize(new Dimension(400, 300));
    }

    private JPanel createLayout() {
        return new JPanel();
    }

    public void observe(UpdateSectorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Sector sector) {
        super.display();
    }

    public void close() {
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

}
