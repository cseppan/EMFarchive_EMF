package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastRunInventory;

public class FastRunInventoryTableData extends AbstractMPSDTTableData<FastRunInventory> {

    private static final String[] COLUMNS = { "Dataset", "Version" };

    public static final String DEFAULT_TIME = "N/A";

    public FastRunInventoryTableData(FastRunInventory[] inventories) {
        super(inventories);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastRunInventory inventory) {

        String[] rowValues = new String[0];

        rowValues = new String[] { inventory.getDataset().getName(), Integer.toString(inventory.getVersion()) };

        return rowValues;
    }

    // protected String getNameWithDefault(FastDatasetWrapper wrapper) {
    //
    // String name = DEFAULT_VALUE;
    // if (wrapper != null) {
    // name = this.getValueWithDefault(wrapper.getName());
    // }
    //
    // return name;
    // }
    //
    // protected String getTypeWithDefault(FastDatasetWrapper wrapper) {
    //
    // String type = DEFAULT_VALUE;
    // if (wrapper != null) {
    // type = this.getValueWithDefault(wrapper.getType());
    // }
    //
    // return type;
    // }
    //
    // protected String getBaseNonPointNameWithDefault(FastDatasetWrapper wrapper) {
    //
    // String name = DEFAULT_VALUE;
    // if (wrapper != null) {
    // name = this.getValueWithDefault(wrapper.getBaseNonPointName());
    // }
    //
    // return name;
    // }
    //
    // protected String getGriddedSMOKENameWithDefault(FastDatasetWrapper wrapper) {
    //
    // String name = DEFAULT_VALUE;
    // if (wrapper != null) {
    // name = this.getValueWithDefault(wrapper.getGriddedSMOKEName());
    // }
    //
    // return name;
    // }
    //
    // protected String getGridNameWithDefault(FastDatasetWrapper wrapper) {
    //
    // String name = DEFAULT_VALUE;
    // if (wrapper != null) {
    // name = this.getValueWithDefault(wrapper.getGridName());
    // }
    //
    // return name;
    // }
    //
    // protected String formatAddedDate(FastDatasetWrapper wrapper) {
    //
    // String date = DEFAULT_VALUE;
    // if (wrapper != null) {
    // date = this.format(wrapper.getAddedDate());
    // }
    //
    // return date;
    // }
}
