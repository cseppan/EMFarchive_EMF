package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class ReferenceTable extends Table {

    private ReferenceTable(String tableType, String tablename) {
        super(tableType, tablename);
    }

    public static final ReferenceTable REF_CONTROL_DEVICE_CODES = new ReferenceTable("Reference Control Device Codes",
            "control_device_codes");

    public static final ReferenceTable REF_CONVERSION_FACTORS = new ReferenceTable("Reference Conversion Factors",
            "conversion_factors");

    public static final ReferenceTable REF_EMISSION_TYPES = new ReferenceTable("Reference Emission Types",
            "emission_types");

    public static final ReferenceTable REF_EMISSION_UNITS_CODES = new ReferenceTable("Reference Emissions Units Codes",
            "emission_units_codes");

    public static final ReferenceTable REF_FIPS = new ReferenceTable(
            "Reference Facility Identification Data Standard (FIPS)", "fips");

    public static final ReferenceTable REF_MACT_CODES = new ReferenceTable(
            "Reference Maximum Achievable Control Technology (MACT) Codes", "mact_codes");

    public static final ReferenceTable REF_MATERIAL_CODES = new ReferenceTable("Reference Material Codes",
            "material_codes");

    public static final ReferenceTable REF_NAICS_CODES = new ReferenceTable(
            "Reference North American Industrial Classification System (NAICS) Codes", "naics_codes");

    public static final ReferenceTable REF_POLLUTANT_CODES = new ReferenceTable("Reference Pollutant Codes",
            "pollutant_codes");

    public static final ReferenceTable REF_SCC = new ReferenceTable("Reference Source Classification Codes (SCC)",
            "scc");

    public static final ReferenceTable REF_SIC_CODES = new ReferenceTable(
            "Reference Standard Industrial Classification (SIC) Codes", "sic_codes");

    public static final ReferenceTable REF_TIME_ZONES = new ReferenceTable("Reference Time Zones", "time_zones");

    public static final ReferenceTable REF_TRIBAL_CODES = new ReferenceTable("Reference Tribal Codes", "tribal_codes");

    public static List list() {
        List list = new ArrayList();

        list.add(REF_CONTROL_DEVICE_CODES);
        list.add(REF_CONVERSION_FACTORS);
        list.add(REF_EMISSION_TYPES);
        list.add(REF_EMISSION_UNITS_CODES);
        list.add(REF_FIPS);
        list.add(REF_MACT_CODES);
        list.add(REF_MATERIAL_CODES);
        list.add(REF_NAICS_CODES);
        list.add(REF_POLLUTANT_CODES);
        list.add(REF_SCC);
        list.add(REF_SIC_CODES);
        list.add(REF_TIME_ZONES);
        list.add(REF_TRIBAL_CODES);

        return list;
    }

    // FIXME: why not use the TableType object ?. Is this table type different
    // from TableType ?
    public static final String getTableType(String datasetType, String filename) {
        // FIXME: dataset type ??
        if (!DatasetTypes.REFERENCE.equals(datasetType))
            return null;

        for (Iterator iter = list().iterator(); iter.hasNext();) {
            ReferenceTable table = (ReferenceTable) iter.next();
            if (filename.indexOf(table.getTableName()) != -1) //i.e. contains
                return table.getTableType();
        }

        return null;
    }

    public static String[] types() {
        List types = new ArrayList();

        for (Iterator iter = list().iterator(); iter.hasNext();) {
            ReferenceTable element = (ReferenceTable) iter.next();
            types.add(element.getTableType());
        }

        return (String[]) types.toArray(new String[0]);
    }

}
