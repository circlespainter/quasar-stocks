package testgrp.embedded;


import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;

public class Main {

    public static void main(String[] args) throws Exception {
        JettyServer embeddedServer = new JettyServer();
        // snippet WebActorInitializer
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        embeddedServer.addServletContextListener(WebActorInitializer.class);
        embeddedServer.addServletContextListener(StaticFilesInitializer.class);
        // end of snippet
        embeddedServer.enableWebsockets();
        System.out.println(Main.class.getClassLoader().getResource("webapp").toExternalForm());
        embeddedServer.getContext().setResourceBase(Main.class.getClassLoader().getResource("webapp").toExternalForm());
        embeddedServer.start();
        JettyServer.waitUrlAvailable("http://localhost:8080");
        System.out.println("Server is up");
    }
}
