package gov.epa.emissions.commons.io.importer;

/**
 * Type-safe enums for fields (columns) of subsets
 * 
 * @author Craig Mattocks
 * @version $Id: TemporalResolution.java,v 1.1 2005/09/13 21:18:58 rhavaldar Exp $
 */
public final class TemporalResolution extends Enum {
    /**
     * Enumeration elements are constructed once upon class loading. Order of
     * appearance here determines index value.
     */
    public static final TemporalResolution ANNUAL = new TemporalResolution("Annual");

    public static final TemporalResolution MONTHLY = new TemporalResolution("Monthly");

    public static final TemporalResolution WEEKLY = new TemporalResolution("Weekly");

    public static final TemporalResolution DAILY = new TemporalResolution("Daily");

    public static final TemporalResolution HOURLY = new TemporalResolution("Hourly");

    /** List of all NAMES in enumeration. */
    public static final java.util.List NAMES = getAllNames(TemporalResolution.class);

    private TemporalResolution(final String name) {
        super(name);
    }

}
