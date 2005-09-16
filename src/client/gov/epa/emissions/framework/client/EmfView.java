package gov.epa.emissions.framework.client;


/**
 * Can contain EMF UI widgets. It's specifically introduced so that either a
 * JFrame or a JInternalFrame can contain a EMF widget such as a
 * JPanel/SortFilterSelectionPanel etc. It allows us to host a widget standalone
 * (in a JFrame) or as a part of a desktop pane (in a JInternalFrame)
 */
public interface EmfView {

    void close();

    void display();
}
