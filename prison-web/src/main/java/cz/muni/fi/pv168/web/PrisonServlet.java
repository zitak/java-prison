package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.prison.backend.Prisoner;
import cz.muni.fi.pv168.prison.backend.PrisonerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Servlet for managing prison.
 */
@WebServlet(PrisonServlet.URL_MAPPING + "/*")
public class PrisonServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/prison";

    private final static Logger log = LoggerFactory.getLogger(PrisonServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        showPrisonersList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //aby fungovala čestina z formuláře
        request.setCharacterEncoding("utf-8");
        //akce podle přípony v URL
        String action = request.getPathInfo();
        switch (action) {
            case "/add":
                //načtení POST parametrů z formuláře
                String name = request.getParameter("name");
                String surname = request.getParameter("surname");
                String born = request.getParameter("born");
                //kontrola vyplnění hodnot
                if (name == null || name.length() == 0 || surname == null || surname.length() == 0 || born == null ) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty !");
                    showPrisonersList(request, response);
                    return;
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-mm-dd");
                    LocalDate dt = LocalDate.parse(born, dtf);

                    Prisoner prisoner = new Prisoner(name, surname, dt);
                    getPrisonerManager().createPrisoner(prisoner);
                    log.debug("created {}",prisoner);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (IllegalEntityException e) {
                    log.error("Cannot add prisoner", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getPrisonerManager().deletePrisoner(getPrisonerManager().getPrisonerById(id));
                    log.debug("deleted prisoner {}",id);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (IllegalEntityException e) {
                    log.error("Cannot delete prisoner", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":

                return;
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets PrisonerManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return PrisonerManager instance
     */
    private PrisonerManager getPrisonerManager() {
        return (PrisonerManager) getServletContext().getAttribute("prisonerManager");
    }

    /**
     * Stores the list of prisoners to request attribute "prisoners" and forwards to the JSP to display it.
     */
    private void showPrisonersList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("prisoners", getPrisonerManager().findAllPrisoners());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (IllegalEntityException e) {
            log.error("Cannot show prisoners", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}