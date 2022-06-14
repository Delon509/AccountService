package account;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objNode1 = mapper.createObjectNode();
        objNode1.put("timestamp", String.valueOf(Calendar.getInstance().getTime()));
        objNode1.put("status", 403);
        objNode1.put("error", "Forbidden");
        objNode1.put("message", "Access Denied!");
        objNode1.put("path", request.getRequestURI() );
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        Securityevents events = new Securityevents();
        events.setId(eventlist.getRecord().size()+1);
        events.setDate(String.valueOf(Calendar.getInstance().getTime()));
        events.setAction("ACCESS_DENIED");
        String name=request.getUserPrincipal().getName();
        if(name==null){
            events.setSubject("Anonymous");
        }
        else {
            events.setSubject(name);
        }
        events.setObject(request.getRequestURI());
        events.setPath(request.getRequestURI());
        eventlist.getRecord().add(events);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(objNode1.toString());
        response.getWriter().flush();
        response.getWriter().close();

    }

}
