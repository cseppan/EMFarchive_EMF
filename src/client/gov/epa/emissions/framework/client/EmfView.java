package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

/**
 * Can contain EMF UI widgets. It's specifically introduced so that either a JFrame or a JInternalFrame can contain a
 * EMF widget.
 */
public interface EmfView {

    void close();

    void display();

    Position getPosition();

    void setPosition(Position position);
}
