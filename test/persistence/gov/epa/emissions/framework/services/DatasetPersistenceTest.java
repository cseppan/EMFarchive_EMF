package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.PersistenceTestCase;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;

public class DatasetPersistenceTest extends PersistenceTestCase {

    public void testVerifySimplePropertiesAreStored() throws Exception {
        String ds_name = "A1" + new Random().nextLong();

        EmfDataset ds = new EmfDataset();
        ds.setAccessedDateTime(new Date());
        ds.setCountry("FR");
        ds.setCreatedDateTime(new Date());
        ds.setCreator("CFD");
        ds.setDescription("DESCRIPTION");
        ds.setModifiedDateTime(new Date());
        ds.setName(ds_name);
        ds.setProject("P1");
        ds.setRegion("USA");
        ds.setSector("S1");
        ds.setStartDateTime(new Date());
        ds.setStatus("imported");
        ds.setYear(42);
        ds.setUnits("orl");
        ds.setTemporalResolution("t1");
        ds.setStopDateTime(new Date());

        DatasetType type = load("ORL Nonpoint Inventory");
        ds.setDatasetType(type);

        KeyVal kv = new KeyVal();
        kv.setValue("bar-1");
        kv.setKeyword(new Keyword("bar-key"));
        ds.addKeyVal(kv);

        save(ds);

        kv.setValue(null);
        update(ds);
    }

    private DatasetType load(String name) {
        Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }
    
    private void save(Object element) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(element);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void update(Object element) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(element);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}
