package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class CaseServiceTransport implements CaseService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public CaseServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Case Service");
    }

    public Case[] getCases() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.setReturnType(mappings.cases());

        return (Case[]) call.requestResponse(new Object[] {});
    }

}
