package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

public abstract class EmfInternalFrame extends JInternalFrame implements ManagedView {

    public EmfInternalFrame(String title) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
    }

    public EmfInternalFrame(String title, Dimension dimension) {
        this(title);
        dimensions(dimension);
    }

    public void bringToFront() {
        super.toFront();
        try {
            super.setSelected(true);
            super.setIcon(false);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("could not bring the window - " + super.getTitle() + " to front of the desktop");
        }
        display();
    }

    public void display() {
        super.setVisible(true);
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

    protected void dimensions(Dimension size) {
        super.setSize(size);
        super.setMinimumSize(size);
    }

    protected void dimensions(int width, int height) {
        this.dimensions(new Dimension(width, height));
    }
}
