package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastRunOutput;

public class FastRunOutputTableData extends AbstractMPSDTTableData<FastRunOutput> {

    private static final String[] COLUMNS = { "Type", "Output" };

    public FastRunOutputTableData(FastRunOutput[] outputs) {
        super(outputs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastRunOutput output) {
        return new String[] { this.getValueWithDefault(output.getType().getName()),
                this.getValueWithDefault(output.getOutputDataset().getName()) };
    }
}
