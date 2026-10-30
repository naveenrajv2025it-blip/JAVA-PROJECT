package com.campus.lostfound;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class EmbeddedServer {
    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.trim().isEmpty()) ? Integer.parseInt(portEnv.trim()) : 8080;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Triggers initialization of default HTTP connector

        String webappDir = new File("src/main/webapp").getAbsolutePath();
        File classesDir = new File("target/classes");

        // 1. Primary Root Context ("") so the home domain loads directly without 404
        StandardContext rootCtx = (StandardContext) tomcat.addWebapp("", webappDir);
        rootCtx.setParentClassLoader(EmbeddedServer.class.getClassLoader());
        WebResourceRoot rootResources = new StandardRoot(rootCtx);
        rootResources.setCachingAllowed(false);
        rootResources.addPreResources(new DirResourceSet(rootResources, "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
        rootCtx.setResources(rootResources);

        // 2. Also support /campus-lost-and-found path for backward compatibility
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/campus-lost-and-found", webappDir);
        ctx.setParentClassLoader(EmbeddedServer.class.getClassLoader());
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.setCachingAllowed(false);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        System.out.println("==================================================================");
        System.out.println(">>> CAMPUS LOST & FOUND SYSTEM IS RUNNING!");
        System.out.println(">>> PRIMARY URL:      http://localhost:" + port + "/campus-lost-and-found/");
        System.out.println(">>> BROWSE ITEMS:     http://localhost:" + port + "/campus-lost-and-found/browse.html");
        System.out.println(">>> REPORT ITEM:      http://localhost:" + port + "/campus-lost-and-found/report.html");
        System.out.println(">>> ADMIN CONSOLE:    http://localhost:" + port + "/campus-lost-and-found/admin.html");
        System.out.println(">>> MY ITEMS:         http://localhost:" + port + "/campus-lost-and-found/my-items.html");
        System.out.println("==================================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}
