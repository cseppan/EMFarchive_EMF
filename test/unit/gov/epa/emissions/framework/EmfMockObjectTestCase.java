package gov.epa.emissions.framework;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.matcher.InvokeCountMatcher;

public abstract class EmfMockObjectTestCase extends MockObjectTestCase {

    protected void stub(Mock mock, String method, Object returnValue) {
        mock.stubs().method(method).will(returnValue(returnValue));
    }

    protected void stub(Mock mock, String method, Object param, Object returnValue) {
        mock.stubs().method(method).with(eq(param)).will(returnValue(returnValue));
    }

    protected void expectsOnce(Mock mock, String method, Object arg) {
        mock.expects(once()).method(method).with(eq(arg));
    }

    protected void expects(Mock mock, int count, String method) {
        mock.expects(new InvokeCountMatcher(count)).method(method);
    }

    protected void expects(Mock mock, String method) {
        expects(mock, 1, method);
    }

}
