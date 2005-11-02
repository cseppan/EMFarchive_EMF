import org.hibernate.Session;

public class EventManager {
   
    public static void main(String[] args) {
        Session s = HibernateUtils.currentSession();
        HibernateUtils.closeSession();
        System.exit(0);    
    }

}

