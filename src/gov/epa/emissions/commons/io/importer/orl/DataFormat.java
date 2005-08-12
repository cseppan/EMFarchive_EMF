package gov.epa.emissions.commons.io.importer.orl;

/**
 * Base DataFormat object used to construct FileImportDetails object.
 */
public abstract class DataFormat {
    public static final String FIPS_NAME = "FIPS";

    protected static final ColumnType FIPS_TYPE = ColumnType.INT;

    protected static final int FIPS_WIDTH = 5;

    protected SqlTypeMapper sqlTypeMapper;

    /** TODO should this be moved to FileColumnsMetadata ? */
    public final void addDetail(FileColumnsMetadata details, String name, ColumnType type, int width) {
        details.add(name);
        try {
            details.setType(name, type.getName());
            details.setWidth(name, String.valueOf(width));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract FileColumnsMetadata getFileImportDetails();

    protected DataFormat(SqlTypeMapper sqlTypeMapper) {
        this.sqlTypeMapper = sqlTypeMapper;
    }
}
