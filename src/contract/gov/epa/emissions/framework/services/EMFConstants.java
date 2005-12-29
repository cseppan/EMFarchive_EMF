package gov.epa.emissions.framework.services;

// FIXME: why need this ? Constants are not a good idea
public final class EMFConstants {

    public static final String DATASET_STATUS_IMPORTED = "Imported";
    public static final String DATASET_STATUS_START_IMPORT = "Start Import";

    public static final String URI_FILENAME_PREFIX = "file:///";

    public static final String EMF_DATA_ROOT_FOLDER = "emf.data.root.folder";

    public static final String EMF_DATA_IMPORT_FOLDER = "emf.data.import.folder";

    public static final String EMF_DATA_EXPORT_FOLDER = "emf.data.export.folder";

    public static final String EMF_REFERENCE_SCHEMA = "reference";

    public static final String EMF_EMISSIONS_SCHEMA = "emissions";

    // DatasetType Names constants
    public static final String DATASETTYPE_NAME_ORL = "ORL";

    public static final String DATASETTYPE_NAME_SHAPEFILES = "Shapefile";

    public static final String DATASETTYPE_NAME_EXTERNALFILES = "External File";

    public static final String DATASETTYPE_NAME_METEOROLOGYFILES = "Meteorology File";

    public static final String DATASETTYPE_NAME_MODELREADYEMISSIONSFILES = "Model Ready Emissions";

    public static final String DATASETTYPE_NAME_TEMPORAL = "Temporal";

    public static final String DATASETTYPE_NAME_TEMPORALCROSSREFERENCE = "Temporal Cross Reference";

    public static final String DATASETTYPE_NAME_TEMPORALPROFILE = "Temporal Profile";

    public static final int PAGE_SIZE = 10;

    public static final long EMF_LOCK_TIMEOUT_VALUE = 12*60*60*1000;
}
