package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ProjectsDAO {

    private HibernateFacade hibernateFacade;

    public ProjectsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(Project.class).addOrder(Order.asc("name")).list();
    }
    
    public Project getProject(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return (Project)hibernateFacade.load(Project.class, criterion, session);
    }
    
    public Project addProject(Project project, Session session) {
        hibernateFacade.add(project, session);
        return getProject(project.getName(), session);
    }

}