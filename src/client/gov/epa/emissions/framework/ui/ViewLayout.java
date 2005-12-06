package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;

public interface ViewLayout {

    void add(ManagedView view, Object id);

    // TODO: what should the position be of a child added after another one is
    // removed
    void remove();

    /**
     * @return true, if activation successful
     */
    boolean activate(Object id);

    boolean available(Object id);

}