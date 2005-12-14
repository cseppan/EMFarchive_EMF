package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultViewLayoutTest extends MockObjectTestCase {

    public void testShouldLookupViewBasedOnId() {
        Mock parent = mock(EmfView.class);
        parent.expects(once()).method("getPosition").will(returnValue(new Position(10, 20)));

        Mock child = mock(ManagedView.class);
        child.expects(once()).method("setPosition");
        child.stubs().method("isAlive").will(returnValue(Boolean.TRUE));

        ViewLayout layout = new CascadeLayout((EmfView) parent.proxy());

        layout.add((ManagedView) child.proxy(), "1");
        assertTrue("Should have been added to the layout", layout.available("1"));

        child.stubs().method("isAlive").will(returnValue(Boolean.FALSE));
        assertFalse("Should fail to activate as the view is closed", layout.available("1"));
    }

    public void testShouldBeAbleToActivateViewBasedOnId() {
        Mock parent = mock(EmfView.class);
        parent.expects(once()).method("getPosition").will(returnValue(new Position(10, 20)));

        Mock child = mock(ManagedView.class);
        child.expects(once()).method("setPosition");
        child.expects(once()).method("bringToFront");
        child.stubs().method("isAlive").will(returnValue(Boolean.TRUE));
        
        ViewLayout layout = new CascadeLayout((EmfView) parent.proxy());

        layout.add((ManagedView) child.proxy(), "1");
        assertTrue("Should have been added to the layout", layout.activate("1"));
        assertFalse("Should not activate as it was never added", layout.activate("2"));
    }

    public void testShouldPositionFirstWindowRelativeToParent() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.expects(once()).method("getPosition").will(returnValue(parentPosition));

        Mock child = mock(ManagedView.class);
        Position childPosition = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child.expects(once()).method("setPosition").with(eq(childPosition));

        ViewLayout layout = new CascadeLayout((EmfView) parent.proxy());

        layout.add((ManagedView) child.proxy(), "child");
    }

    public void testShouldPositionSecondWindowRelativeToFirst() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.stubs().method("getPosition").will(returnValue(parentPosition));

        ViewLayout layout = new CascadeLayout((EmfView) parent.proxy());

        Mock child1 = mock(ManagedView.class);
        Position child1Position = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child1.expects(once()).method("setPosition").with(eq(child1Position));

        layout.add((ManagedView) child1.proxy(), "child1");

        Mock child2 = mock(ManagedView.class);
        Position child2Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child2.expects(once()).method("setPosition").with(eq(child2Position));

        layout.add((ManagedView) child2.proxy(), "child2");
    }

    public void testShouldPositionThirdWindowRelativeToFirstWhenSecondWindowIsClosedAndThirdWindowAdded() {
        Mock parent = mock(EmfView.class);
        Position parentPosition = new Position(10, 20);
        parent.stubs().method("getPosition").will(returnValue(parentPosition));

        ViewLayout layout = new CascadeLayout((EmfView) parent.proxy());

        Mock child1 = mock(ManagedView.class);
        Position child1Position = new Position(parentPosition.x() + 25, parentPosition.y() + 25);
        child1.expects(once()).method("setPosition").with(eq(child1Position));

        layout.add((ManagedView) child1.proxy(), "child1");

        Mock child2 = mock(ManagedView.class);
        Position child2Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child2.expects(once()).method("setPosition").with(eq(child2Position));
        ManagedView child2Proxy = (ManagedView) child2.proxy();

        layout.add(child2Proxy, "child2");
        layout.remove();

        Mock child3 = mock(ManagedView.class);
        Position child3Position = new Position(child1Position.x() + 25, child1Position.y() + 25);
        child3.expects(once()).method("setPosition").with(eq(child3Position));

        layout.add((ManagedView) child3.proxy(), "child3");
    }

}
