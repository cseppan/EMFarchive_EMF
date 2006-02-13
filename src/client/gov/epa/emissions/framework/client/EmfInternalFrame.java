package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.ui.Position;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public abstract class EmfInternalFrame extends JInternalFrame implements ManagedView, ChangeObserver {

    protected DesktopManager desktopManager;

    public EmfInternalFrame(String title, DesktopManager desktopManager) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
        this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent event) {
                windowClosing();
                super.internalFrameClosing(event);
            }
        });
        this.desktopManager = desktopManager;
        super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public EmfInternalFrame(String title, Dimension dimension, DesktopManager desktopManager) {
        this(title, desktopManager);
        dimensions(dimension);
    }

    abstract public void windowClosing();

    public void bringToFront() {
        super.toFront();
        try {
            super.setSelected(true);
            super.setIcon(false);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("could not bring the window - " + super.getTitle() + " to front of the desktop");
        }
        super.setVisible(true);
    }

    public void display() {
        desktopManager.openWindow(this);
    }

    public Position getPosition() {
        Point point = super.getLocation();
        return new Position(point.x, point.y);
    }

    public void setPosition(Position position) {
        super.setLocation(new Point(position.x(), position.y()));
    }

    public void refreshLayout() {
        super.validate();
    }

    public void close() {
        desktopManager.closeWindow(this);
    }

    protected void dimensions(Dimension size) {
        super.setSize(size);
        super.setMinimumSize(size);
    }

    protected void dimensions(int width, int height) {
        this.dimensions(new Dimension(width, height));
    }

    public void signalChanges() {
        if (!this.getTitle().endsWith(" *"))
            this.setTitle(this.getTitle() + " *");
    }

    public void signalSaved() {
        String title = this.getTitle();
        int starPosition = title.indexOf(" *");
        if (starPosition >= 0)
            this.setTitle(title.substring(0, starPosition));
    }

    public int height() {
        return (int) super.getSize().getHeight();
    }

    public int width() {
        return (int) super.getSize().getWidth();
    }
}
