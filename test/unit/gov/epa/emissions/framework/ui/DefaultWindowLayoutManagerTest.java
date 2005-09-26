package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultWindowLayoutManagerTest extends MockObjectTestCase {

    public void testShouldPositionFirstWindowRelativeToParent() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.expects(once()).method("getPosition").will(returnValue(parentPosition));

        Mock child = mock(EmfView.class);
        Position childPosition = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child.expects(once()).method("setPosition").with(eq(childPosition));

        WindowLayoutManager layout = new DefaultWindowLayoutManager((EmfView) parent.proxy());

        layout.add((EmfView) child.proxy());
    }

    public void testShouldPositionSecondWindowRelativeToFirst() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.stubs().method("getPosition").will(returnValue(parentPosition));

        WindowLayoutManager layout = new DefaultWindowLayoutManager((EmfView) parent.proxy());

        Mock child1 = mock(EmfView.class);
        Position child1Position = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child1.expects(once()).method("setPosition").with(eq(child1Position));

        layout.add((EmfView) child1.proxy());

        Mock child2 = mock(EmfView.class);
        Position child2Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child2.expects(once()).method("setPosition").with(eq(child2Position));

        layout.add((EmfView) child2.proxy());
    }

    public void testShouldPositionThirdWindowRelativeToFirstWhenSecondWindowIsClosedAndThirdWindowAdded() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.stubs().method("getPosition").will(returnValue(parentPosition));

        WindowLayoutManager layout = new DefaultWindowLayoutManager((EmfView) parent.proxy());

        Mock child1 = mock(EmfView.class);
        Position child1Position = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child1.expects(once()).method("setPosition").with(eq(child1Position));

        layout.add((EmfView) child1.proxy());

        Mock child2 = mock(EmfView.class);
        Position child2Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child2.expects(once()).method("setPosition").with(eq(child2Position));
        EmfView child2Proxy = (EmfView) child2.proxy();

        layout.add(child2Proxy);
        layout.remove();

        Mock child3 = mock(EmfView.class);
        Position child3Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child3.expects(once()).method("setPosition").with(eq(child3Position));

        layout.add((EmfView) child3.proxy());
    }

}
