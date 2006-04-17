package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;

import java.util.List;

public class CaseCommonsDAOTest extends ServiceTestCase {

    private DataCommonsDAO dao;

    protected void doSetUp() throws Exception {
        dao = new DataCommonsDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllProjects() {
        int totalBeforeAdd = dao.getProjects(session).size();
        Project project = new Project("test" + Math.random());
        add(project);

        try {
            List list = dao.getProjects(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(project));
        } finally {
            remove(project);
        }
    }

}
