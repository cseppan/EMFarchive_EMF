package gov.epa.emissions.framework.services;

public class RemoteUserServicesTest extends UserServicesTestCase {

    public RemoteUserServicesTest() {
        super("http://ben.cep.unc.edu:8080/emf/services");
    }

}
