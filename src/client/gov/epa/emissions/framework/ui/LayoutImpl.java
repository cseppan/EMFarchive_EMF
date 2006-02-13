package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsoleView;

import java.util.EmptyStackException;
import java.util.Stack;

public class LayoutImpl implements Layout {

    private EmfConsoleView console;

    private Stack managedViews;

    public LayoutImpl(EmfConsoleView console) {
        this.console = console;
        managedViews = new Stack();
    }

    public void position(ManagedView view) {
        Position lastViewPosition = lastViewPosition();
        Position currentViewPosition = cascade(lastViewPosition);
        if (checkForFullWindowDisplayInsideConsole(currentViewPosition, view, console)) {
            view.setPosition(currentViewPosition);
        } else {
            view.setPosition(new Position(0, 0));
        }
        managedViews.push(view);
    }
    
    public void unregister(ManagedView manageView) {
        managedViews.remove(manageView);
    }

    private boolean checkForFullWindowDisplayInsideConsole(Position currentViewPosition, ManagedView view,
            EmfConsoleView console) {
        int consoleWidth = console.width() + 20;
        int consoleHeight = console.height() + 20;

        return (currentViewPosition.x() + view.width() < consoleWidth)
                && (currentViewPosition.y() + view.height() < consoleHeight);
    }

    private Position lastViewPosition() {
        try {
            ManagedView view = (ManagedView) managedViews.pop();
            return view.getPosition();
        } catch (EmptyStackException e) {
            return new Position(0, 0);
        }
    }

    private Position cascade(Position lastViewPosition) {
        return new Position(lastViewPosition.x() + 20, lastViewPosition.y() + 20);
    }

}
