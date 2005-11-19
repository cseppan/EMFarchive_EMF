package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.User;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class UserMappings extends Mappings {

    public void register(Call call) {
        bean(call, User.class, "User");
        array(call, User[].class, "Users");
    }

    public QName user() {
        return qname("User");
    }

    public QName users() {
        return qname("Users");
    }

}
