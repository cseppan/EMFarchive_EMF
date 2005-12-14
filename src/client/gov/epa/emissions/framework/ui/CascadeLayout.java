package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.ManagedView;

import java.util.HashMap;
import java.util.Map;

public class CascadeLayout implements ViewLayout {

    private EmfView parent;

    private int childCount;

    private Map children;

    public CascadeLayout(EmfView parent) {
        this.parent = parent;
        childCount = 0;

        children = new HashMap();
    }

    public void add(EmfView child) {
        childCount++;
        Position parentPosition = parent.getPosition();
        int newx = parentPosition.x() + (childCount % 10) * 22;
        int newy = parentPosition.y() + (childCount % 10) * 22;
        /* don't let the windows start cascading off the bottom of the
         * screen.  Would like to know how big the child window is here.
         */
        Position childPosition = new Position(newx, newy);

        child.setPosition(childPosition);
    }

    // TODO: what should the position be of a child added after another one is
    // removed
    public void remove() {
        childCount--;
    }

    public void add(ManagedView child, Object id) {
        add(child);
        children.put(id, child);
    }

    public boolean activate(Object id) {
        if (!available(id))
            return false;

        ManagedView view = (ManagedView) children.get(id);
        view.bringToFront();
        return true;
    }

    public boolean available(Object id) {
        if (!children.containsKey(id))
            return false;

        ManagedView view = (ManagedView) children.get(id);
        if (view.isAlive())
            return true;

        children.remove(id);// flush dead views
        return false;
    }

}
