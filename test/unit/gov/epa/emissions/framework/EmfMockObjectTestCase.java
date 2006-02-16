package gov.epa.emissions.framework;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public abstract class EmfMockObjectTestCase extends MockObjectTestCase {

    protected void stub(Mock mock, String method, Object returnValue) {
        mock.stubs().method(method).will(returnValue(returnValue));
    }

    protected void expectsOnce(Mock mock, String method, Object arg) {
        mock.expects(once()).method(method).with(eq(arg));
    }

}
