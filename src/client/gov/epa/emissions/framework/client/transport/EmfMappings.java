package gov.epa.emissions.framework.client.transport;

import org.apache.axis.client.Call;

public class EmfMappings extends Mappings {

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    public EmfMappings() {
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    public void register(Call call) {
        dataMappings.register(call);
        caseMappings.register(call);
    }

}
