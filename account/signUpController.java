package account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
public class signUpController {
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    PasswordEncoder encoder;


    @PostMapping("/api/auth/signup")
    public ResponseEntity<Map<String,Object>> signUp(@Valid @RequestBody User user, HttpServletRequest request)
    {
            if(User.checkvaildsignup(user))
            {
                if(userDetailsServiceImpl.emailexistornot(user.getEmail()))
                {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "User exist!");
                }
                else
                {
                    if(User.countforsignup==0){
                        user.setemployeeid(1);
                        user.setRole("ROLE_ADMINISTRATOR");
                    }
                    else {
                        user.setemployeeid(1+User.countforsignup);
                        user.setRole("ROLE_USER");
                    }
                    User.countforsignup++;
                    String[] roles = (user.getRole()+",").split(",");
                    user.setEmail(user.getEmail().toLowerCase());
                    HashMap<String,Object> map= new HashMap<>();
                    map.put("id",user.getemployeeid());
                    map.put("name",user.getName());
                    map.put("lastname",user.getLastname());
                    map.put("email",user.getEmail());
                    map.put("roles",roles);
                    user.setPassword(encoder.encode(user.getPassword()));
                    SecurityeventsList eventlist = SecurityeventsList.getInstance();
                    Securityevents events = new Securityevents();
                    events.setId(eventlist.getRecord().size()+1);
                    events.setDate(String.valueOf(Calendar.getInstance().getTime()));
                    events.setAction("CREATE_USER");
                    events.setSubject("Anonymous");
                    events.setObject(user.getEmail());
                    events.setPath(request.getRequestURI());
                    eventlist.getRecord().add(events);
                    userRepo.save(user);
                    return  ResponseEntity.status(HttpStatus.OK).body(map);
                }

            }
            else
            {
                if(!user.getEmail().endsWith("@acme.com")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "You have to use @acme.com");
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The password is in the hacker's database!");
            }
    }

}
