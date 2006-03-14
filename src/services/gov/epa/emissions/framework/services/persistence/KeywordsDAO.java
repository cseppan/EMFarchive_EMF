package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Keyword;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class KeywordsDAO {

    private HibernateFacade hibernateFacade;

    public KeywordsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getKeywords(Session session) {
        return hibernateFacade.getAll(Keyword.class, Order.asc("name"), session);
    }

}
