package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;

public interface WindowLayoutManager {

    void add(ManagedView view, Object id);

    // TODO: what should the position be of a child added after another one is
    // removed
    void remove();

    void activate(Object id);

}