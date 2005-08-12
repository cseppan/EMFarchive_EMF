package gov.epa.emissions.commons.io.importer.orl;

/**
 * Type-safe enums for fields (columns) of subsets
 *
 * @author	Craig Mattocks
 * @version $Id: TemporalResolution.java,v 1.1 2005/08/12 14:12:14 rhavaldar Exp $
 */
public final class TemporalResolution
	extends Enum
{
	/**
	 * Enumeration elements are constructed once upon class loading.
	 * Order of appearance here determines index value.
	 */
	public static final TemporalResolution ANNUAL	= new TemporalResolution ("Annual");
	public static final TemporalResolution MONTHLY	= new TemporalResolution ("Monthly");
	public static final TemporalResolution WEEKLY	= new TemporalResolution ("Weekly");
	public static final TemporalResolution DAILY	= new TemporalResolution ("Daily");
	public static final TemporalResolution HOURLY	= new TemporalResolution ("Hourly");

	/** List of all NAMES in enumeration. */
	public static final java.util.List NAMES = getAllNames(TemporalResolution.class);

	/** List of all objects in enumeration. */
//	public static final java.util.List VALUES = getInstances(TemporalResolution.class);

	/**
	 * Private constructor prevents construction outside of this class.
	 */
	private TemporalResolution(final String name)
	{
		super(name);
	}

	/**
	 * Find an enumerated item by its name
	 */
	public static TemporalResolution valueOf(final String name)
	{
		return (TemporalResolution)get(TemporalResolution.class, name);
	}

	/* ########## DO NOT EDIT BELOW THIS LINE ########## */

	/**
	 * Valid enumerator?
	 *
	 * Not rigorous, just check for its name.
	 */
	public static boolean isValid(final String name)
	{
		return NAMES.contains(name);
	}

	/** 
	 * Get a String array of all names in the enumeration.
	 *
	 * @return	An unmodifiable String[] Array of the names.
	 */
	public static final String[] getNamesArray()
	{
		return (String[])NAMES.toArray(new String[NAMES.size()]);
	}

}
