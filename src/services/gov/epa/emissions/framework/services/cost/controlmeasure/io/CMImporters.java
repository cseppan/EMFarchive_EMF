package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.SQLException;

public class CMImporters {

    private File[] files;

    private Record[] records;

    private CMSummaryImporter summaryImporter;

    private CMEfficiencyImporter efficiencyImporter;

    private CMSCCImporter sccImporter;

    private CMReferenceImporter referenceImporter;

    private HibernateSessionFactory sessionFactory;

    private User user;

    public CMImporters(File[] files, Record[] records, User user, HibernateSessionFactory sessionFactory) throws EmfException {
        this.files = files;
        this.records = records;
        this.user = user;
        this.sessionFactory = sessionFactory;
        summaryImporter = createSummaryImporter();
        efficiencyImporter = createEfficiencyImporter();
        sccImporter = createSCCImporter();
        referenceImporter = createReferenceImporter();
    }

    public CMSummaryImporter summaryImporter() {
        return summaryImporter;
    }

    public CMEfficiencyImporter efficiencyImporter() {
        return efficiencyImporter;
    }

    public CMSCCImporter sccImporter() {
        return sccImporter;
    }

    public CMReferenceImporter referenceImporter() {
        return referenceImporter;
    }

    private CMSummaryImporter createSummaryImporter() throws EmfException {
        CMSummaryFileFormat fileFormat = new CMSummaryFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMSummaryImporter(files[i], fileFormat, user, sessionFactory);
            }
        }

        throw new EmfException("Control Measure Summary file is required");
    }

    private CMEfficiencyImporter createEfficiencyImporter() throws EmfException {
        CMEfficiencyFileFormat fileFormat = new CMEfficiencyFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                try {
                    return new CMEfficiencyImporter(files[i], fileFormat,user, sessionFactory);
                } catch (SQLException e) {
                    throw new EmfException(e.getMessage());
                }
            }
        }

        throw new EmfException("Control Measure Efficiency file is required");

    }

    private CMSCCImporter createSCCImporter() throws EmfException {
        CMSCCsFileFormat fileFormat = new CMSCCsFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMSCCImporter(files[i], fileFormat, user, sessionFactory);
            }
        }

        throw new EmfException("Control Measure SCC file is required");

    }

    private CMReferenceImporter createReferenceImporter() {
        CMReferenceFileFormat fileFormat = new CMReferenceFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMReferenceImporter(files[i], fileFormat);
            }
        }

        return null;
    }

    private boolean matches(String[] cols, String[] tokens) {
        if (cols.length != tokens.length)
            return false;

        for (int i = 0; i < cols.length; i++) {
            if (!cols[i].equalsIgnoreCase(tokens[i]))
                return false;
        }

        return true;
    }

}
