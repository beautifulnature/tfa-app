package tfa.tickets.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import tfa.tickets.base.IGenericDao;
import tfa.tickets.gui.HitCountBean;

/**
 * My web application initializer
 */
@WebListener
public class Initialize implements ServletContextListener, ServletRequestListener
{
  // Standard SLF4J logger
  private static Logger log = LoggerFactory.getLogger(Initialize.class);

  public Initialize()
  {
    // Created by web server
    super();       
  }

  // ---------------------------------------------------- starting

  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    log.info("contextInitialized start");
           
    // JAAS Configuration
    if ( System.getProperty("java.security.auth.login.config") == null )
        System.setProperty("java.security.auth.login.config", "./etc/jaas.conf" );
    
    // Force log level to DEBUG for all application (tfa.*) in development
    if (Configuration.isDeveloppement() && log.getClass().getName().equals("ch.qos.logback.classic.Logger"))
      ((ch.qos.logback.classic.Logger) log).getLoggerContext().getLogger("tfa")
          .setLevel(ch.qos.logback.classic.Level.DEBUG);

    // JULI logger ( Tomcat, JSF ) on logback
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    // Context params
    ServletContext sc = sce.getServletContext();

    // JSF adaptations

    if (Configuration.isDeveloppement())
      sc.setInitParameter("javax.faces.PROJECT_STAGE", "Development");

    if (Configuration.isProduction())
      sc.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");

    sc.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
    sc.setInitParameter("javax.faces.DEFAULT_SUFFIX", ".xhtml");

    // Jetty option to not browse server file's directories
    if (sc.getServerInfo().contains("jetty"))
      sc.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    
    // Security roles for Resteasy
    sc.setInitParameter("resteasy.role.based.security", "true");
 
    // init and Factory with config file
    IGenericDao.init("TicketsPU");

    log.info("contextInitialized end");
  }

  @Override
  public void requestInitialized(ServletRequestEvent sre)
  {
  }

  // ------------------------------------------------------- ending

  @Override
  public void requestDestroyed(ServletRequestEvent sre)
  {
    // close eventual EntityManager created for thread/request
    IGenericDao.lazyCloseEm();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
    // term EntityManager and Factory
    IGenericDao.term();

    // Save hit count at end of application
    HitCountBean.end();

    // JULI logger on logback : end
    SLF4JBridgeHandler.uninstall();

    // To avoid error at hibernate closing (let's time to process to close)
    try { Thread.sleep(100); } catch (InterruptedException e) { }

    log.info("contextDestroyed");
  }
  
}
