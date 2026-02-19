package edu.pucmm.icc352.util;

import org.h2.tools.Server;
import java.sql.SQLException;

public class H2Server {
    private static Server tcpServer;
    private static Server webServer;

    public static void start() {
        try {
            tcpServer = Server.createTcpServer(
                    "-tcp", "-tcpAllowOthers", "-tcpPort", "9092"
            ).start();

            webServer = Server.createWebServer(
                    "-web", "-webAllowOthers", "-webPort", "8082"
            ).start();

            System.out.println("✅ H2 TCP: " + tcpServer.getURL());
            System.out.println("✅ H2 WEB: " + webServer.getURL());
        } catch (SQLException e) {
            throw new RuntimeException("Error iniciando H2", e);
        }
    }
}
