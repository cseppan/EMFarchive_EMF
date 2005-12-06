package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.HibernateTestCase;
import gov.epa.emissions.framework.db.PostgresDbUpdate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatasetTypePersistenceTest extends HibernateTestCase {

    public void testVerifySimplePropertiesAreStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.setMaxfiles(1);
        type.setMinfiles(1);

        save(type);
        DatasetType loadedType = load("NAME");
        assertNotNull("DatasetType with name - 'NAME' should have been persisted ", loadedType);

        drop(loadedType);
    }

    public void testVerifyKeywordsAreStored() throws Exception {
        DatasetType loadedType = null;
        try{
            DatasetType type = new DatasetType();
            type.setDescription("TEST");
            type.setName("NAME");
            type.addKeyword(new Keyword("key1"));
            type.addKeyword(new Keyword("key2"));

            save(type);
            loadedType = load("NAME");
            System.out.println(loadedType==null);
            assertNotNull(loadedType);
            System.out.println(loadedType.getKeywords().length);
            assertEquals(2, loadedType.getKeywords().length);
            assertEquals("key1", loadedType.getKeywords()[0].getName());
            assertEquals("key2", loadedType.getKeywords()[1].getName());
            
        }finally{
            System.out.println("FINALLY CALLED");
            drop(loadedType);
                       
        }

    }

    public void FIXME_testVerifyUpdatedKeywordIsStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.addKeyword(new Keyword("key1"));
        type.addKeyword(new Keyword("key2"));

        save(type);

        DatasetType loadedType = load("NAME");
        assertEquals(2, loadedType.getKeywords().length);
        Keyword key1 = loadedType.getKeywords()[0];
        assertEquals("key1", key1.getName());
        key1.setName("updated-key1");

        update(type);

        DatasetType updatedType = load("NAME");
        Keyword updatedKey = updatedType.getKeywords()[0];
        assertEquals("updated-key1", updatedKey.getName());

        drop(loadedType);
    }

    private DatasetType load(String name) {
        Session session = session();
        try {
            Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
            List list = query.list();
            return list.size() == 1 ? (DatasetType) list.get(0) : null;
        } finally {
            session.close();
        }
    }

    private void drop(DatasetType loadedType) throws Exception {
        PostgresDbUpdate update = new PostgresDbUpdate();
        update.delete("emf.datasettypes", "dataset_type_id", loadedType.getDatasettypeid() + "");
        update.deleteAll("emf.emf_keywords");
    }

    private void save(DatasetType type) {
        Transaction tx = null;
        Session session = session();
        try {
            tx = session.beginTransaction();
            session.save(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    private void update(DatasetType type) {
        Transaction tx = null;
        Session session = session();
        try {
            tx = session.beginTransaction();
            session.update(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
