package gov.epa.emissions.commons.io.importer.orl;

/**
 * Type-safe enums for fields (columns) of datasets
 * 
 * @author Craig Mattocks
 * @version $Id: DatasetFields.java,v 1.1 2005/08/12 14:12:14 rhavaldar Exp $
 */
public final class DatasetFields extends Enum {
    /**
     * Enumeration elements are constructed once upon class loading. Order of
     * appearance here determines index value.
     */
    public static final DatasetFields ID = new DatasetFields("ID");

    public static final DatasetFields SELECT = new DatasetFields("Select");

    public static final DatasetFields NAME = new DatasetFields("Name");

    public static final DatasetFields DESCRIPTION = new DatasetFields("Description");

    public static final DatasetFields TYPE = new DatasetFields("Type");

    public static final DatasetFields META_DATA = new DatasetFields("Metadata");

    public static final DatasetFields DATA_SOURCES = new DatasetFields("Data Sources");

    public static final DatasetFields DATABASE = new DatasetFields("Database");

    public static final DatasetFields PREFIX = new DatasetFields("Prefix");

    public static final DatasetFields DATA_TABLES = new DatasetFields("Data Tables");

    public static final DatasetFields SECTORS = new DatasetFields("Sectors");

    public static final DatasetFields REGION = new DatasetFields("Region");

    public static final DatasetFields BOUNDING_BOX = new DatasetFields("Bounding Box");

    public static final DatasetFields START_DATE_TIME = new DatasetFields("Start Date Time");

    public static final DatasetFields STOP_DATE_TIME = new DatasetFields("Stop Date Time");

    public static final DatasetFields DATE_FORMAT = new DatasetFields("Date Format");

    public static final DatasetFields TEMPORAL_RESOLUTION = new DatasetFields("Temporal Resolution");

    // public static final DatasetFields YEAR = new DatasetFields ("Year");
    public static final DatasetFields TIME_ZONE = new DatasetFields("Time Zone");

    public static final DatasetFields POLLUTANTS = new DatasetFields("Pollutants");

    public static final DatasetFields UNITS = new DatasetFields("Units");

    public static final DatasetFields PROJECT = new DatasetFields("Project");

    /** List of all NAMES in enumeration. */
    public static final java.util.List NAMES = getAllNames(DatasetFields.class);

    /** List of all objects in enumeration. */
    // public static final java.util.List VALUES =
    // getInstances(DatasetFields.class);
    /**
     * Private constructor prevents construction outside of this class.
     */
    private DatasetFields(final String name) {
        super(name);
    }

    /**
     * Find an enumerated item by its name
     */
    public static DatasetFields valueOf(final String name) {
        return (DatasetFields) get(DatasetFields.class, name);
    }

    /* ########## DO NOT EDIT BELOW THIS LINE ########## */

    /**
     * Valid enumerator?
     * 
     * Not rigorous, just check for its name.
     */
    public static boolean isValid(final String name) {
        return NAMES.contains(name);
    }

    /**
     * Get a String array of all names in the enumeration.
     * 
     * @return An unmodifiable String[] Array of the names.
     */
    public static final String[] getNamesArray() {
        return (String[]) NAMES.toArray(new String[NAMES.size()]);
    }

    public static final String[] getSubsetNamesArray() {
        final String[] subset = { SELECT.getName(), NAME.getName(), TYPE.getName(), REGION.getName(),
                START_DATE_TIME.getName(), TEMPORAL_RESOLUTION.getName() };
        return subset;
    }

}
