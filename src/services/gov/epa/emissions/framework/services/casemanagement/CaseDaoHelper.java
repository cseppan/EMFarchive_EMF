package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

public class CaseDaoHelper {

    private int currentCaseId = -1; //NOTE: an non-exist case id for initial value
    
    private List<CaseParameter> parameters;

    private List<ParameterName> parameterNames;

    private List<ParameterEnvVar> parameterEnvtVars;

    private List<ValueType> parameterValueTypes;

    private List<Sector> sectors;

    private List<CaseProgram> programs;
    
    private List<CaseInput> inputs;
    
    private List<InputName> inputNames;
    
    private List<DatasetType> dataSetTypes;
    
    private List<SubDir> subDirs;
    
    private List<InputEnvtVar> inputEnvVars;

    // up to here

    private CaseDAO caseDao = null;
    
    private DataCommonsDAO dataDao = null;

    private HibernateSessionFactory sessionFactory;

    public CaseDaoHelper(HibernateSessionFactory sessionFactory, CaseDAO caseDao, DataCommonsDAO dataDao) {
        this.sessionFactory = sessionFactory;
        this.caseDao = caseDao;
        this.dataDao = dataDao;
    }
    
    private synchronized void resetCaseId(int caseId) {
        this.currentCaseId = caseId;
    }
    
    public synchronized List<CaseParameter> getCaseParameters(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getCaseParameters(caseId, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseParameter objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter addCaseParameter(CaseParameter param) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(param, session);
            return caseDao.loadCaseParameter(param, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseParameter objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void insertCaseParameter(CaseParameter selected) throws EmfException {
        if (selected == null)
            return;

        int caseId = selected.getCaseID();
        
        if (parameters == null || parameters.isEmpty() || this.currentCaseId != caseId) {
            resetCaseId(caseId);
            parameters = getCaseParameters(selected.getCaseID()); // make sure Parameters have been retrieved
        }

        if (parameters.contains(selected))
            return;

        // the parameter was not found in the list
        CaseParameter newParam = addCaseParameter(selected);
        parameters.add(newParam);
    }

    public synchronized List<ParameterName> getParameterNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getParameterNames(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all parameter objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized ParameterName addParameterName(ParameterName param) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(param, session);
            
            return (ParameterName) caseDao.loadParameterName(param, session);
        } catch (Exception e) {
            e.printStackTrace();
            Throwable error = e.getCause();
            throw new EmfException("Couldn't get all parameter name objects from database -- " + (error == null ? "cuase unknown." : error.getMessage()));
        } finally {
            session.close();
        }
    }

    public synchronized ParameterName getParameterName(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ParameterName param = null;
        if (selected instanceof String) {
            param = new ParameterName(selected.toString());
        } else if (selected instanceof ParameterName) {
            param = (ParameterName) selected;
        }
        
        if (param.getName() == null || param.getName().trim().isEmpty())
            return null;

        if (parameterNames == null || parameterNames.isEmpty())
            parameterNames = getParameterNames(); // make sure Parameters have been retrieved

        if (parameterNames.contains(param))
            return parameterNames.get(parameterNames.indexOf(param));

        // the parameter was not found in the list
        ParameterName newParam = addParameterName(param);
        parameterNames.add(newParam);

        return newParam;
    }
    
    public synchronized List<ParameterEnvVar> getParameterEnvVars() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getParameterEnvVars(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all ParameterEnvVar objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(envVar, session);
            return caseDao.loadParamEnvVar(envVar, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all ParameterEnvVar objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized ParameterEnvVar getParameterEnvVar(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ParameterEnvVar envVar = null;
        if (selected instanceof String) {
            envVar = new ParameterEnvVar(selected.toString());
        } else if (selected instanceof ParameterEnvVar) {
            envVar = (ParameterEnvVar) selected;
        }
        
        if (envVar.getName() == null || envVar.getName().trim().isEmpty())
            return null;
        
        if (parameterEnvtVars == null || parameterEnvtVars.isEmpty())
            parameterEnvtVars = getParameterEnvVars(); // make sure ParameterEnvVar have been retrieved

        if (parameterEnvtVars.contains(envVar))
            return parameterEnvtVars.get(parameterEnvtVars.indexOf(envVar));

        // the ParameterEnvVar was not found in the list
        ParameterEnvVar newEnvVar = addParameterEnvVar(envVar);
        parameterEnvtVars.add(newEnvVar);

        return newEnvVar;
    }
    
    public synchronized List<ValueType> getValueTypes() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getValueTypes(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all ValueType objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(type, session);
            return (ValueType) caseDao.load(ValueType.class, type.getName(), session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all ValueType objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized ValueType getValueType(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ValueType type = null;
        if (selected instanceof String) {
            type = new ValueType(selected.toString());
        } else if (selected instanceof ValueType) {
            type = (ValueType) selected;
        }
        
        if (type.getName() == null || type.getName().trim().isEmpty())
            return null;

        if (parameterValueTypes == null || parameterValueTypes.isEmpty())
            parameterValueTypes = getValueTypes(); // make sure ValueType have been retrieved

        if (parameterValueTypes.contains(type))
            return parameterValueTypes.get(parameterValueTypes.indexOf(type));

        // the ValueType was not found in the list
        ValueType newType = addValueType(type);
        parameterValueTypes.add(newType);

        return newType;
    }
    
    public synchronized List<Sector> getSectors() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dataDao.getSectors(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all Sector objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Sector addSector(Sector sector) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dataDao.add(sector, session);
            return (Sector) caseDao.load(Sector.class, sector.getName(), session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all Sector objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Sector getSector(Object selected) throws EmfException {
        if (selected == null || selected.toString().equalsIgnoreCase("All Sectors"))
            return null;

        Sector sector = null;
        if (selected instanceof String) {
            sector = new Sector(selected.toString(), selected.toString());
        } else if (selected instanceof Sector) {
            sector = (Sector) selected;
        }
        
        if (sector.getName() == null || sector.getName().trim().isEmpty())
            return null;

        if (sectors == null || sectors.isEmpty())
            sectors = getSectors(); // make sure Sector have been retrieved

        if (sectors.contains(sector))
            return sectors.get(sectors.indexOf(sector));

        // the Sector was not found in the list
        Sector newSector = addSector(sector);
        sectors.add(newSector);

        return newSector;
    }
    
    public synchronized List<CaseProgram> getCasePrograms() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getPrograms(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseProgram objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseProgram addCaseProgram(CaseProgram prog) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(prog, session);
            return caseDao.loadCaseProgram(prog, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseProgram objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseProgram getCaseProgram(Object selected) throws EmfException {
        if (selected == null)
            return null;

        CaseProgram prog = null;
        if (selected instanceof String) {
            prog = new CaseProgram(selected.toString());
        } else if (selected instanceof CaseProgram) {
            prog = (CaseProgram) selected;
        }

        if (prog.getName() == null || prog.getName().trim().isEmpty())
            return null;
        
        if (programs == null || programs.isEmpty())
            programs = getCasePrograms(); // make sure CaseProgram have been retrieved

        if (programs.contains(prog))
            return programs.get(programs.indexOf(prog));

        // the CaseProgram was not found in the list
        CaseProgram newProg = addCaseProgram(prog);
        programs.add(newProg);

        return newProg;
    }
    
    public synchronized List<InputName> getInputNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getInputNames(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all InputName objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized InputName addInputName(InputName inputName) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(inputName, session);
            return caseDao.loadInputName(inputName, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all InputName objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized InputName getInputName(Object selected) throws EmfException {
        if (selected == null)
            return null;

        InputName name = null;
        if (selected instanceof String) {
            name = new InputName(selected.toString());
        } else if (selected instanceof InputName) {
            name = (InputName) selected;
        }
        
        if (name.getName() == null || name.getName().trim().isEmpty())
            return null;

        if (inputNames == null || inputNames.isEmpty())
            inputNames = getInputNames(); // make sure InputName have been retrieved

        if (inputNames.contains(name))
            return inputNames.get(inputNames.indexOf(name));

        // the InputName was not found in the list
        InputName newName = addInputName(name);
        inputNames.add(newName);

        return newName;
    }

    public synchronized List<InputEnvtVar> getInputEnvtVars() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getInputEnvtVars(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all InputEnvtVar objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(envVar, session);
            return caseDao.loadInputEnvtVar(envVar, session);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Couldn't get all InputEnvtVar objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized InputEnvtVar getInputEnvVar(Object selected) throws EmfException {
        if (selected == null)
            return null;

        InputEnvtVar envVar = null;
        if (selected instanceof String) {
            envVar = new InputEnvtVar(selected.toString());
        } else if (selected instanceof InputEnvtVar) {
            envVar = (InputEnvtVar) selected;
        }
        
        if (envVar.getName() == null || envVar.getName().trim().isEmpty())
            return null;
        
        if (inputEnvVars == null || inputEnvVars.isEmpty())
            inputEnvVars = getInputEnvtVars(); // make sure InputEnvtVar have been retrieved

        if (inputEnvVars.contains(envVar))
            return inputEnvVars.get(inputEnvVars.indexOf(envVar));

        // the InputEnvtVar was not found in the list
        InputEnvtVar newEnvVar = addInputEnvtVar(envVar);
        inputEnvVars.add(newEnvVar);

        return newEnvVar;
    }

    public synchronized List<DatasetType> getDatasetTypes() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dataDao.getDatasetTypes(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all DatasetType objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    public synchronized DatasetType getDatasetType(Object selected) throws EmfException {
        if (selected == null)
            return null;

        DatasetType type = null;
        if (selected instanceof String) {
            type = new DatasetType(selected.toString());
        } else if (selected instanceof DatasetType) {
            type = (DatasetType) selected;
        }
        
        if (type.getName() == null || type.getName().trim().isEmpty())
            return null;
        
        if (dataSetTypes == null || dataSetTypes.isEmpty())
            dataSetTypes = getDatasetTypes(); // make sure DatasetType have been retrieved


        for (Iterator<DatasetType> iter = dataSetTypes.iterator(); iter.hasNext();) {
            DatasetType temp = iter.next();

            if (temp.getName().equals(type.getName()))
                return temp;
        }
        
        return null;
    }
    
    public synchronized List<SubDir> getSubDirs() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getSubDirs(session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all SubDir objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized SubDir addSubDir(SubDir subDir) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(subDir, session);
            return caseDao.loadCaseSubdir(subDir, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all SubDir objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized SubDir getSubDir(Object selected) throws EmfException {
        if (selected == null)
            return null;

        SubDir subdir = null;
        if (selected instanceof String) {
            subdir = new SubDir(selected.toString());
        } else if (selected instanceof SubDir) {
            subdir = (SubDir) selected;
        }
        
        if (subdir.getName() == null || subdir.getName().trim().isEmpty())
            return null;

        if (subDirs == null || subDirs.isEmpty())
            subDirs = getSubDirs(); // make sure SubDir have been retrieved

        if (subDirs.contains(subdir))
            return subDirs.get(subDirs.indexOf(subdir));

        // the SubDir was not found in the list
        SubDir newSubdir = addSubDir(subdir);
        subDirs.add(newSubdir);

        return newSubdir;
    }

    public synchronized List<CaseInput> getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return caseDao.getCaseInputs(caseId, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseInput objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput addCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            caseDao.addObject(input, session);
            return (CaseInput) caseDao.loadCaseInput(input, session);
        } catch (Exception e) {
            throw new EmfException("Couldn't get all CaseInput objects from database -- " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void insertCaseInput(CaseInput selected) throws EmfException {
        if (selected == null)
            return;

        int caseId = selected.getCaseID();
        
        if (inputs == null || inputs.isEmpty() || this.currentCaseId != caseId) {
            resetCaseId(caseId);
            inputs = getCaseInputs(caseId); // make sure CaseInput have been retrieved
        }

        if (inputs.contains(selected))
            return;

        // the CaseInput was not found in the list
        CaseInput newInput = addCaseInput(selected);
        inputs.add(newInput);
    }
    

}
