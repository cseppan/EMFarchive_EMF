package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutput;

public class FastOutputTableData extends AbstractMPSDTTableData<FastAnalysisOutput> {

    private static final String[] COLUMNS = { "Type", "Output" };

    public FastOutputTableData(FastAnalysisOutput[] outputs) {
        super(outputs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastAnalysisOutput output) {
        return new String[] { this.getValueWithDefault(output.getType().getName()),
                this.getValueWithDefault(output.getOutputDataset().getName()) };
    }
}
