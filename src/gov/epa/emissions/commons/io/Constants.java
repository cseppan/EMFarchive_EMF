package gov.epa.emissions.commons.io;

import java.util.ResourceBundle;

/**
 * EmisView constants
 * @author Craig Mattocks
 * @version $Id: Constants.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public final class Constants
{
   /**
   * Resource bundle for auto-configuring GUI components:
   * gov/epa/emissions/emisview/constants/resources/widget.properties
   */
  private static final String BUNDLE_NAME = "resources/widget";

  public  static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  /** Global debug switch */
  public static final boolean DEBUG = false;

  /** CONCEPT NIF field definitions file */
  public static final String NIF_FIELD_DEFS_FILE = 
   UserPreferences.getInstance().getProperty(UserPreferences.DATASET_NIF_FIELD_DEFS);//Utility.getString

  /** Database loaded on startup */
  public static final String DEFAULT_DB = null;

  public static final boolean USE_PREP_STATEMENT = false;

  /** Display messages showing progress status */
  public static final boolean SHOW_STATUS = true;
}
