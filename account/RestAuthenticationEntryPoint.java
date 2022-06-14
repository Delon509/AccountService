package account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    UserRepository userRepo;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        Securityevents events = new Securityevents();
        events.setId(eventlist.getRecord().size()+1);
        events.setDate(String.valueOf(Calendar.getInstance().getTime()));
        events.setAction("LOGIN_FAILED");
        String name="";
        Boolean exist=false;
        if(request.getHeader("Authorization")!=null){
            String[] store= request.getHeader("Authorization").split(" ");
            byte[] decoded = Base64.decodeBase64(store[1]);
            name =new String(decoded, "UTF-8").split(":")[0] ;
            events.setSubject(name);
            exist = userRepo.existsByemail(name.toLowerCase());
            events.setObject(request.getRequestURI());
            events.setPath(request.getRequestURI());
            if(!exist){
                eventlist.getRecord().add(events);
            }


            if(exist){
                if(userRepo.findByemail(name.toLowerCase()).getUserstatus().equals("NL")){
                    eventlist.getRecord().add(events);
                }
                User target = userRepo.findByemail(name);
                target.setTimeoffailedlogin(target.getTimeoffailedlogin()+1);
                if(target.getTimeoffailedlogin()==5&& !target.getRole().equals("ROLE_ADMINISTRATOR")){
                    target.setUserstatus("L");
                    target.setTimeoffailedlogin(0);

                    Securityevents bruteevent = new Securityevents();
                    bruteevent.setId(eventlist.getRecord().size()+1);
                    bruteevent.setDate(String.valueOf(Calendar.getInstance().getTime()));
                    bruteevent.setAction("BRUTE_FORCE");
                    bruteevent.setSubject(name);
                    bruteevent.setObject(request.getRequestURI());
                    bruteevent.setPath(request.getRequestURI());
                    eventlist.getRecord().add(bruteevent);

                    Securityevents lockevent = new Securityevents();
                    lockevent.setId(eventlist.getRecord().size()+1);
                    lockevent.setDate(String.valueOf(Calendar.getInstance().getTime()));
                    lockevent.setAction("LOCK_USER");
                    lockevent.setSubject(name);
                    lockevent.setObject("Lock user "+name);
                    lockevent.setPath("/api/admin/user/access");
                    eventlist.getRecord().add(lockevent);
                }
                userRepo.save(target);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objNode1 = mapper.createObjectNode();
        objNode1.put("timestamp", String.valueOf(Calendar.getInstance().getTime()));
        objNode1.put("status", 401);
        objNode1.put("error", "Unauthorized");

        if(exist){
            User target = userRepo.findByemail(name);
            if(target.getUserstatus().equals("L")){
                objNode1.put("message", "User account is locked");
            }
            else {
                objNode1.put("message", "Wrong Password!");
            }

        }
        else{
            objNode1.put("message", "Wrong Password!");
        }
        objNode1.put("path", request.getRequestURI() );
        response.setStatus(401);
        response.getWriter().write(objNode1.toString());
        response.getWriter().flush();
        response.getWriter().close();
    }
}
