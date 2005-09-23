package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfView;

public interface WindowLayoutManager {

    public abstract void add(EmfView child);

    // TODO: what should the position be of a child added after another one is
    // removed
    public abstract void remove();

}