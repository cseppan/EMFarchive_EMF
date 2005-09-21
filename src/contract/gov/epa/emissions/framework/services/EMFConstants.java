/*
 * Created on Aug 9, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFConstants.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

/**
 * @author Conrad F. D'Cruz
 * 
 */
// FIXME: why need this ? Constants are a very bad idea
public final class EMFConstants {

	public static final String emfServicesNamespace = "urn:gov.epa.services.EmfServices";
    public static final String IMPORT_MESSAGE_TYPE = "Import";

    public static final String EXPORT_MESSAGE_TYPE = "Export";

    public static final String START_IMPORT_MESSAGE_Prefix = "Started import for ";

    public static final String END_IMPORT_MESSAGE_Prefix = "Completed import for ";

    public static final String START_EXPORT_MESSAGE_Prefix = "Started export for ";

    public static final String END_EXPORT_MESSAGE_Prefix = "Completed export for ";

    public static final String URI_FILENAME_PREFIX = "file:///";

    //public static final String EMF_REPOSITORIES = "conf/emf/emf_repositories.mount";
    public static final String EMF_DATA_ROOT_FOLDER="emf.data.root.folder";
    public static final String EMF_DATA_IMPORT_FOLDER="emf.data.import.folder";
    public static final String EMF_DATA_EXPORT_FOLDER="emf.data.export.folder";

    //public static final String EMF_REPOSITORIES = "emf_repositories.mount";
    public static final String EMF_REFERENCE_SCHEMA="reference";
    public static final String EMF_EMISSIONS_SCHEMA="emissions";
}
