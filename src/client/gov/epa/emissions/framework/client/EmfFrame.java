package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public abstract class EmfFrame extends JFrame implements EmfView {

    public EmfFrame(String name, String title) {
        super(title);
        super.setName(name);
        super.setUndecorated(true);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        super.setResizable(false);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                close();
                super.windowClosing(event);
            }
        });
    }

    protected void doClose() {// needs to be overridden, if needed
    }

    public Position getPosition() {
        Point point = super.getLocation();
        return new Position(point.x, point.y);
    }

    public void setPosition(Position position) {
        super.setLocation(new Point(position.x(), position.y()));
    }

    final public void close() {
        doClose();
        super.dispose();
    }

    public void display() {
        this.setVisible(true);
    }

    public void refreshLayout() {
        super.validate();
    }
}
