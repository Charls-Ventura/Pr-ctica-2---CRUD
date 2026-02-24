package edu.pucmm.icc352.util;

public class PersistenceSmokeTest {
    public static void main(String[] args) {
        H2Server.start(); // levanta H2 TCP (9092) + Web Console (8082)

        HibernateUtil.getSessionFactory().openSession().close();
        System.out.println(" Hibernate + H2 Server ");
        HibernateUtil.getSessionFactory().close();
    }
}
