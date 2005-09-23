package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

public abstract class EmfInternalFrame extends JInternalFrame implements EmfView, ManagedView {

    public EmfInternalFrame(String title) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
    }

    public void bringToFront() {
        super.toFront();
        try {
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
}
