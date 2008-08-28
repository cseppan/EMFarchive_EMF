package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CaseAssistanceService {
    private static Log log = LogFactory.getLog(ManagedCaseService.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private CaseDAO caseDao;

    private HibernateSessionFactory sessionFactory;

    private String eol = System.getProperty("line.separator");

    public CaseAssistanceService(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.caseDao = new CaseDAO();

        if (DebugLevels.DEBUG_9)
            System.out.println("In ManagedCaseService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        myTag();

        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + myTag());

        log.info("ManagedCaseService");
        log.info("Session factory null? " + (sessionFactory == null));
    }

    public synchronized void importCase(String[] files, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String[][] cases = sortCaseFiles(files);

        Case newCase = null;

        try {
            for (int i = 0; i < cases.length; i++) {
                CaseFileParser caseParser = new CaseFileParser(cases[i][0], cases[i][1], cases[i][2]);
                newCase = caseParser.getCase();

                if (newCase == null)
                    throw new EmfException("Case not properly parsed.");
                
                if (newCase.getName() == null || newCase.getName().trim().isEmpty())
                    throw new EmfException("Case name not specified.");

                if (newCase.getModel() == null || newCase.getModel().getName().isEmpty())
                    throw new EmfException("Case run model not specified.");

                Case existedCase = caseDao.getCaseFromName(newCase.getName(), session);

                if (existedCase != null)
                    throw new EmfException("Case (" + newCase.getName()
                            + ") to import has a duplicate name in cases table.");

                resetCaseValues(newCase, session);
                caseDao.add(newCase, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not inmport case", e);
            throw new EmfException("Could not import case: " + newCase.getName() + eol);
        } finally {
            session.close();
        }
    }

    private String[][] sortCaseFiles(String[] files) throws EmfException {
        int len = files.length;
        int numCases = len / 3;

        if (len % 3 != 0)
            throw new EmfException("Incomplete files for importing cases.");

        String[][] caseFiles = new String[numCases][3];

        // NOTE: needs to expand to support multiple cases import
        for (int i = 0; i < numCases; i++) {
            caseFiles[i][0] = files[0];
            caseFiles[i][1] = files[1];
            caseFiles[i][2] = files[2];
        }

        return caseFiles;
    }

    private void resetCaseValues(Case newCase, Session session) {
        Abbreviation abbr = newCase.getAbbreviation();
        
        if (abbr == null)
            abbr = new Abbreviation(newCase.getName());
        
        loadNSetObject(newCase, abbr, Abbreviation.class, abbr.getName(), session);
        
        CaseCategory cat = newCase.getCaseCategory();
        loadNSetObject(newCase, cat, CaseCategory.class, cat == null ? "" : cat.getName(), session);

        Project proj = newCase.getProject();
        loadNSetObject(newCase, proj, Project.class, proj == null ? "" : proj.getName(), session);
        
        ModelToRun model = newCase.getModel();
        loadNSetObject(newCase, model, ModelToRun.class, model == null ? "" : model.getName(), session);

        Region modRegion = newCase.getModelingRegion();
        loadNSetObject(newCase, modRegion, Region.class, modRegion == null ? "" : modRegion.getName(), session);

        Grid grid = newCase.getGrid();
        loadNSetObject(newCase, grid, Grid.class, grid == null ? "" : grid.getName(), session);

        GridResolution gResltn = newCase.getGridResolution();
        loadNSetObject(newCase, gResltn, GridResolution.class, gResltn == null ? "" : gResltn.getName(), session);

        AirQualityModel airMod = newCase.getAirQualityModel();
        loadNSetObject(newCase, airMod, AirQualityModel.class, airMod == null ? "" : airMod.getName(), session);

        Speciation spec = newCase.getSpeciation();
        loadNSetObject(newCase, spec, Speciation.class, spec == null ? "" : spec.getName(), session);

        MeteorlogicalYear metYear = newCase.getMeteorlogicalYear();
        loadNSetObject(newCase, metYear, MeteorlogicalYear.class, metYear == null ? "" : metYear.getName(), session);
    }

    private synchronized void loadNSetObject(Case newCase, Object obj, Class<?> clazz, String name, Session session) {
        if (obj == null)
            return;

        Object temp = caseDao.load(clazz, name, session);
        
        if (temp != null && temp instanceof Abbreviation) {
            String uniqueName = name + "_" + Math.random();
            ((Abbreviation)obj).setName(uniqueName);
            caseDao.addObject(obj, session);
            temp = caseDao.load(clazz, uniqueName, session);
        }

        if (temp == null) {
            caseDao.addObject(obj, session);
            temp = caseDao.load(clazz, name, session);
        }
        
        if (temp instanceof Abbreviation) {
            newCase.setAbbreviation((Abbreviation)temp);
            return;
        }
        
        if (temp instanceof CaseCategory) {
            newCase.setCaseCategory((CaseCategory)temp);
            return;
        }
        
        if (temp instanceof Project) {
            newCase.setProject((Project)temp);
            return;
        }
        
        if (temp instanceof ModelToRun) {
            newCase.setModel((ModelToRun)temp);
            return;
        }
        
        if (temp instanceof Region) {
            newCase.setModelingRegion((Region)temp);
            return;
        }
        
        if (temp instanceof Grid) {
            newCase.setGrid((Grid)temp);
            return;
        }
        
        if (temp instanceof GridResolution) {
            newCase.setGridResolution((GridResolution)temp);
            return;
        }
        
        if (temp instanceof AirQualityModel) {
            newCase.setAirQualityModel((AirQualityModel)temp);
            return;
        }
        
        if (temp instanceof Speciation) {
            newCase.setSpeciation((Speciation)temp);
            return;
        }
        
        if (temp instanceof MeteorlogicalYear) {
            newCase.setMeteorlogicalYear((MeteorlogicalYear)temp);
            return;
        }
    }
}
