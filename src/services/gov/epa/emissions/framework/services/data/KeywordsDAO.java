package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

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
