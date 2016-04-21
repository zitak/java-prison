package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.prison.backend.CellManagerImpl;
import cz.muni.fi.pv168.prison.backend.Main;
import cz.muni.fi.pv168.prison.backend.PrisonerManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.time.Clock;

@WebListener
public class StartListener implements ServletContextListener {

    final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("aplikace inicializována");
        ServletContext servletContext = ev.getServletContext();
        DataSource dataSource = Main.createMemoryDatabase();
        PrisonerManagerImpl prisonerManager = new PrisonerManagerImpl(Clock.systemDefaultZone());
        prisonerManager.setDataSource(dataSource);
        CellManagerImpl cellManager = new CellManagerImpl();
        cellManager.setDataSource(dataSource);
        servletContext.setAttribute("prisonerManager", prisonerManager);
        servletContext.setAttribute("cellManager", cellManager);
        log.info("vytvořeny manažery a uloženy do atributů servletContextu");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("aplikace končí");
    }
}