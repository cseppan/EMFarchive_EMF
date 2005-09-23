package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfView;

public class DefaultWindowLayoutManager implements WindowLayoutManager {

    private EmfView parent;

    private int children;

    public DefaultWindowLayoutManager(EmfView parent) {
        this.parent = parent;
        children = 0;
    }

    public void add(EmfView child) {
        children++;
        Position parentPosition = parent.getPosition();
        Position childPosition = new Position(parentPosition.x() + children * 25, parentPosition.y() + children * 25);

        child.setPosition(childPosition);
    }

    // TODO: what should the position be of a child added after another one is
    // removed
    public void remove() {
        children--;
    }

}
