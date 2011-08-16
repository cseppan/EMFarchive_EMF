package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class DebugLevels {
    
    private static final HibernateSessionFactory sessionFactory  = HibernateSessionFactory.get();
    private static final EmfPropertiesDAO propDAO = new EmfPropertiesDAO(sessionFactory);
    private static boolean getProperty(String name) {
        return propDAO.getProperty(name).getValue().trim().equalsIgnoreCase("true");
    }

    public static final boolean DEBUG_0() { return getProperty("DEBUG_0"); }
	public static final boolean DEBUG_1() { return getProperty("DEBUG_1"); }
	public static final boolean DEBUG_2() { return getProperty("DEBUG_2"); }
	public static final boolean DEBUG_3() { return getProperty("DEBUG_3"); }
	public static final boolean DEBUG_4() { return getProperty("DEBUG_4"); } 
    public static final boolean DEBUG_5() { return getProperty("DEBUG_5"); }
    public static final boolean DEBUG_6() { return getProperty("DEBUG_6"); }
    public static final boolean DEBUG_7() { return getProperty("DEBUG_7"); }
    public static final boolean DEBUG_8() { return getProperty("DEBUG_8"); }
    public static final boolean DEBUG_9() { return getProperty("DEBUG_9"); } // very detailed job run messages
    public static final boolean DEBUG_10() { return getProperty("DEBUG_10"); } //to trace import start messages
    public static final boolean DEBUG_11() { return getProperty("DEBUG_11"); } //to trace pattern matching of files
    public static final boolean DEBUG_12() { return getProperty("DEBUG_12"); } //to trace DAO classes
    public static final boolean DEBUG_13() { return getProperty("DEBUG_13"); }
    public static final boolean DEBUG_14() { return getProperty("DEBUG_14"); } //to trace job run issues
    public static final boolean DEBUG_15() { return getProperty("DEBUG_15"); } //to trace export job submitter issues
    public static final boolean DEBUG_16() { return getProperty("DEBUG_16"); } //to trace dataset deletion
    public static final boolean DEBUG_17() { return getProperty("DEBUG_17"); } //to trace new method for case output registration
    public static final boolean DEBUG_18() { return getProperty("DEBUG_18"); } //to trace timing in various functions
    public static final boolean DEBUG_19() { return getProperty("DEBUG_19"); } //to trace get page issues
    public static final boolean DEBUG_20() { return getProperty("DEBUG_20"); } //to trace case ModelToRun issues
    public static final boolean DEBUG_21() { return getProperty("DEBUG_21"); }
    public static final boolean DEBUG_22() { return getProperty("DEBUG_22"); }  //to trace enhancing flat file 2010 point QA program
    public static final boolean DEBUG_23() { return getProperty("DEBUG_23"); } // DEBUG_CMIMPORT
    public static final boolean DEBUG_24() { return getProperty("DEBUG_24"); } // debug ExportTask

}
