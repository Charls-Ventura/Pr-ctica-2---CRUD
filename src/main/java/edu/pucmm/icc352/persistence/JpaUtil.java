package edu.pucmm.icc352.persistence;

import edu.pucmm.icc352.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.function.Function;

public class JpaUtil {

    public static <T> T tx(Function<Session, T> work) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T result = work.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
