package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.io.importer.Enum;

/**
 * The type of columns to insert into FileImportDetails.
 */
public final class ColumnType extends Enum {
    public static final ColumnType INT = new ColumnType("I");

    public static final ColumnType REAL = new ColumnType("N");

    public static final ColumnType CHAR = new ColumnType("C");

    private ColumnType(String name) {
        super(name);
    }
}
