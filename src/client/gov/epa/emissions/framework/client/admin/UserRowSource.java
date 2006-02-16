package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ui.RowSource;

public class UserRowSource implements RowSource {

    private User source;

    public UserRowSource(User source) {
        this.source = source;
    }

    public Object[] values() {
        return new Object[] { source.getUsername(), source.getName(), source.getEmail(), new Boolean(source.isAdmin()) };
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}