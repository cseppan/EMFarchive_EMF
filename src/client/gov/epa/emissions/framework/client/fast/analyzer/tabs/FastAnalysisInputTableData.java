package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastAnalysisInput;

public class FastAnalysisInputTableData extends AbstractMPSDTTableData<FastAnalysisInput> {

    private static final String[] COLUMNS = { "Name", "SMOKE Report/ORL", "Ancillary ORL", "Derived ORL Point" };

    public FastAnalysisInputTableData(FastAnalysisInput[] inputs) {
        super(inputs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastAnalysisInput input) {
        return new String[] { this.getValueWithDefault(input.getName()),
                this.getValueWithDefault(input.getOrlInventory()),
                this.getValueWithDefault(input.getAncillaryORLInventory()),
                this.getValueWithDefault(input.getDerivedORLPointInventory()) };
    }
}
