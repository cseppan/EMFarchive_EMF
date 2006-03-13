package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.data.Keyword;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class KeywordsDAO {

    public List getKeywords(Session session) {
        return session.createCriteria(Keyword.class).addOrder(Order.asc("name")).list();
    }

}
