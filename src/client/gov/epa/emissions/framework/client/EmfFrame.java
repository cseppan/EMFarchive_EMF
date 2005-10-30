package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class EmfFrame extends JFrame implements EmfView {

    public EmfFrame(String name, String title) {
        super(title);
        super.setName(name);
        super.setUndecorated(true);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        super.setResizable(false);
    }

    public Position getPosition() {
        Point point = super.getLocation();
        return new Position(point.x, point.y);
    }

    public void setPosition(Position position) {
        super.setLocation(new Point(position.x(), position.y()));
    }

    public void close() {
        super.dispose();
    }

    public void display() {
        this.setVisible(true);
    }

    public void refreshLayout() {
        super.validate();
    }
}
