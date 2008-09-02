package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;

public class CaseAssistanceServiceTest extends ServiceTestCase {

    private CaseAssistanceService service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;
    
    private User user;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new CaseAssistanceService(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        user = userService.getUser("emf");
    }

    protected void doTearDown() throws Exception {
        dropAll(CaseParameter.class);
        dropAll(CaseInput.class);
        dropAll(Case.class);
        dropAll(Abbreviation.class);
        dropAll(MeteorlogicalYear.class);
        dropAll(Project.class);
        dropAll(Region.class);
        dropAll(Grid.class);
        dropAll(GridResolution.class);
        dropAll(AirQualityModel.class);
        dropAll(Speciation.class);
        dropAll(CaseCategory.class);
        
        service = null;
        userService = null;
        sessionFactory = null;
        System.gc();
    }

    public void testShouldImportACase() throws Exception {
        String folder = "test/data/case-management";
        String inputsFile = new File(folder, "2002ac V3 CAP for EMF training_2002acT_Inputs.csv").getAbsolutePath();
        String jobsFile = new File(folder, "2002ac V3 CAP for EMF training_2002acT_Jobs.csv").getAbsolutePath();
        String sumParamsFile = new File(folder, "2002ac V3 CAP for EMF training_2002acT_Summary_Parameters.csv").getAbsolutePath();
        
        try {
            service.importCase(new String[] {sumParamsFile, inputsFile, jobsFile}, user);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doTearDown();
        }
    }
}
