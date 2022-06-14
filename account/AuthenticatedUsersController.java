package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import java.util.*;

@RestController
public class AuthenticatedUsersController {
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    paymentsRepository paymentsRepos;

    public void setzero(String email){
        User target = userRepo.findByemail(email);
        target.setTimeoffailedlogin(0);
        userRepo.save(target);
    }
    @GetMapping("/api/empl/payment")
    public ResponseEntity<Object> payment(@AuthenticationPrincipal UserDetails details, @RequestParam(value = "period",required = false) String period){

        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        List<Map<String,Object>> store = new ArrayList<>();
        if(period==null)
        {

            List<payments> record = paymentsRepos.findByemployee(details.getUsername());
            Collections.reverse(record);
            for( payments temp : record){
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("name",user.getName());
                map.put("lastname",user.getLastname());
                map.put("period",temp.periodToString());
                map.put("salary",temp.salaryToString());
                store.add(map);
            }
            return  ResponseEntity.status(HttpStatus.OK).body(store);
        }
        int temp = Character.getNumericValue(period.charAt(0)) * 10 + Character.getNumericValue(period.charAt(1));
        if ( temp> 12 || temp<1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Month should be in 01 to 12");
        }
        payments record= paymentsRepos.findByemailAndperiod(user.getEmail(),period);
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name",user.getName());
        map.put("lastname",user.getLastname());
        map.put("period",record.periodToString());
        map.put("salary",record.salaryToString());

        return  ResponseEntity.status(HttpStatus.OK).body(map);
    }
    @PostMapping("/api/auth/changepass")
    public ResponseEntity<Map<String,Object>> changepass(@AuthenticationPrincipal UserDetails details,@RequestBody HashMap<String,String> map,HttpServletRequest request){
        String newpw = map.get("new_password");

        if(newpw.length()<12)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password length must be 12 chars minimum!");
        if(User.checkisbreachedpassword(newpw))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "The password is in the hacker's database!");

        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        if(encoder.matches(newpw,user.getPassword()))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The passwords must be different!");
        }
        else
        {
            user.setPassword(encoder.encode(newpw));
            userRepo.save(user);
            Map<String,Object> returnmessage = new HashMap<>();
            returnmessage.put("email",details.getUsername().toLowerCase());
            returnmessage.put("status","The password has been updated successfully");
            SecurityeventsList eventlist = SecurityeventsList.getInstance();
            Securityevents events = new Securityevents();
            events.setId(eventlist.getRecord().size()+1);
            events.setDate(String.valueOf(Calendar.getInstance().getTime()));
            events.setAction("CHANGE_PASSWORD");
            events.setSubject(details.getUsername());
            events.setObject(details.getUsername());
            events.setPath(request.getRequestURI());
            eventlist.getRecord().add(events);
            return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
        }

    }
    @Transactional
    @PostMapping("/api/acct/payments")
    public ResponseEntity<Map<String,Object>> uploadpayrolls(@Valid @RequestBody payments[] pay,@AuthenticationPrincipal UserDetails details ) {
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        for (int i = 0; i < pay.length; ++i) {
            int temp = Character.getNumericValue(pay[i].getPeriod().charAt(0)) * 10 + Character.getNumericValue(pay[i].getPeriod().charAt(1));
            if ( temp> 12 || temp<1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Month should be in 01 to 12");
            }
            if(!userRepo.existsByemail(pay[i].getEmployee().toLowerCase())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "employee["+i+"] doesn't exist in database");
            }
            if(pay[i].getSalary()<0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Wrong salary in payment list");
            }
            for (int j = i + 1; j < pay.length; ++j) {
                if (Objects.equals(pay[i].getPeriod(), pay[j].getPeriod()) && Objects.equals(pay[i].getEmployee(), pay[j].getEmployee())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Duplicate payment for same employee in same period");
                }
            }
        }
        for(payments temp:pay){
            paymentsRepos.save(temp);
        }
        Map<String,Object> returnmessage = new HashMap<>();
        returnmessage.put("status","Added successfully!");
        return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
    }
    @Transactional
    @PutMapping("/api/acct/payments")
    public ResponseEntity<Map<String,Object>> uploadpayrolls(@Valid @RequestBody payments pay,@AuthenticationPrincipal UserDetails details )
    {
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        int temp = Character.getNumericValue(pay.getPeriod().charAt(0)) * 10 + Character.getNumericValue(pay.getPeriod().charAt(1));
        if ( temp> 12 || temp<1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Month should be in 01 to 12");
        }
        if(!userRepo.existsByemail(pay.getEmployee())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "employee doesn't exist in database");
        }
        if(pay.getSalary()<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Wrong salary in request body!");
        }
        payments target= paymentsRepos.findByemailAndperiod(pay.getEmployee(),pay.getPeriod());
        paymentsRepos.setsalaryForpayments(pay.getSalary(),target.getPaymentID());
        Map<String,Object> returnmessage = new HashMap<>();
        returnmessage.put("status","Updated successfully!");
        return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
    }
    @PutMapping("/api/admin/user/role")
    public ResponseEntity<Map<String,Object>> changeuserrole(@RequestBody HashMap<String,String> map,HttpServletRequest request,@AuthenticationPrincipal UserDetails details){
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        if(!(map.containsKey("user")&&map.containsKey("role")&& map.containsKey("operation"))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "not include enough information for this operation");
        }
        if(map.get("user")==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "wrong format");
        }

        if(!userRepo.existsByemail(map.get("user").toLowerCase())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User not found!");
        }
        if(!(map.get("role").equals("USER")||map.get("role").equals("ACCOUNTANT")||map.get("role").equals("ADMINISTRATOR")||map.get("role").equals("AUDITOR"))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Role not found!");
        }

        User target = userRepo.findByemail(map.get("user").toLowerCase());
        String targetrole = target.getRole() + ",";
        String[] store = targetrole.split(",");
        Arrays.sort(store);
        if(!(Arrays.asList(store).contains("ROLE_"+map.get("role")))&&map.get("operation").equals("REMOVE")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user does not have a role!");
        }

        if(map.get("role").equals("ADMINISTRATOR")&&map.get("operation").equals("REMOVE")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can't remove ADMINISTRATOR role!");
        }
        if(store.length==1 && store[0].equals("ROLE_"+map.get("role"))&&map.get("operation").equals("REMOVE")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user must have at least one role!");
        }
        if(map.get("role").equals("ADMINISTRATOR")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        }
        if(store[0].equals("ROLE_ADMINISTRATOR")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        }
        String tempstore="";
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        Securityevents events = new Securityevents();
        events.setId(eventlist.getRecord().size()+1);
        events.setDate(String.valueOf(Calendar.getInstance().getTime()));
        if(map.get("operation").equals("GRANT")){
            events.setAction("GRANT_ROLE");
            events.setObject("Grant role "+map.get("role")+" to "+map.get("user").toLowerCase());
            String [] newstore = new String[store.length+1];
            System.arraycopy(store, 0, newstore, 0, store.length);
            newstore[store.length]= "ROLE_"+map.get("role");

            Arrays.sort(newstore);
            for(String temp: newstore){
                if(tempstore.length()==0){
                    tempstore+=temp;
                }
                else {
                    tempstore= tempstore+","+temp;
                }
            }

        }
        else {
            events.setAction("REMOVE_ROLE");
            events.setObject("Remove role "+map.get("role")+" from "+map.get("user").toLowerCase());
            for(String temp: store){
                if(!(temp.equals("ROLE_"+map.get("role")))){
                    if(tempstore.length()==0){
                        tempstore+=temp;
                    }
                    else {
                        tempstore= tempstore+","+temp;
                    }

                }
            }

        }
        events.setSubject(details.getUsername());

        events.setPath(request.getRequestURI());
        eventlist.getRecord().add(events);
        target.setRole(tempstore);
        userRepo.save(target);
        Map<String,Object> returnmessage = new HashMap<>();
        targetrole = target.getRole() + ",";
        String[] returnmessagerole= targetrole.split(",");
        returnmessage.put("id",target.getemployeeid());
        returnmessage.put("name",target.getName());
        returnmessage.put("lastname",target.getLastname());
        returnmessage.put("email",target.getEmail());
        returnmessage.put("roles",returnmessagerole);
        return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
    }
    @Transactional
    @DeleteMapping ("/api/admin/user/{email}")
    public ResponseEntity<Map<String,Object>> deleteuser(@PathVariable String email,HttpServletRequest request,@AuthenticationPrincipal UserDetails details){
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        if(!userRepo.existsByemail(email.toLowerCase())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User not found!");
        }
        User target = userRepo.findByemail(email);
        if(target.getRole().equals("ROLE_ADMINISTRATOR")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can't remove ADMINISTRATOR role!");
        }
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        Securityevents events = new Securityevents();
        events.setId(eventlist.getRecord().size()+1);
        events.setDate(String.valueOf(Calendar.getInstance().getTime()));
        userRepo.deleteByemployeeid(target.getemployeeid());
        Map<String,Object> returnmessage = new HashMap<>();
        returnmessage.put("user",target.getEmail());
        returnmessage.put("status","Deleted successfully!");
        events.setAction("DELETE_USER");
        events.setSubject(details.getUsername());
        events.setObject(email);
        events.setPath(request.getRequestURI());
        eventlist.getRecord().add(events);
        return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
    }
    @GetMapping ("/api/admin/user")
    public ResponseEntity<Object> getalluserinfo(@AuthenticationPrincipal UserDetails details){
        User a =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(a.getEmail());
        List<User> alluser = userRepo.findAll();
        Collections.sort(alluser);
        List<Map<String,Object>> store = new ArrayList<>();
        for(User user: alluser){
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("id",user.getemployeeid());
            map.put("name",user.getName());
            map.put("lastname",user.getLastname());
            map.put("email",user.getEmail());
            String[] role = (user.getRole()+",").split(",");
            Arrays.sort(role);
            map.put("roles",role);
            store.add(map);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(store);
    }
    @PutMapping("/api/admin/user/access")
    public ResponseEntity<Map<String,Object>> lockorunlock(@AuthenticationPrincipal UserDetails details,HttpServletRequest request,@RequestBody HashMap<String,String> map ){
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        if(!(map.containsKey("user")&&map.containsKey("operation"))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "not include enough information for this operation");
        }
        if(map.get("user")==null || map.get("operation")==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "wrong format");
        }

        if(!userRepo.existsByemail(map.get("user").toLowerCase())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User not found!");
        }
        if(!(map.get("operation").equals("LOCK")||map.get("operation").equals("UNLOCK"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No such Operation!");
        }
        User target=userRepo.findByemail(map.get("user"));
        Map<String,Object> returnmessage = new HashMap<>();
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        Securityevents events = new Securityevents();
        events.setId(eventlist.getRecord().size()+1);
        events.setDate(String.valueOf(Calendar.getInstance().getTime()));

        if(target.getRole().equals("ROLE_ADMINISTRATOR")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can't lock the ADMINISTRATOR!");
        }
        if(map.get("operation").equals("LOCK")){
            if(target.getUserstatus().equals("L")){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You cannot lock a locked user!");
            }
            target.setUserstatus("L");
            returnmessage.put("status","User "+target.getEmail()+" locked!");
            events.setAction("LOCK_USER");
            events.setObject("Lock user "+map.get("user").toLowerCase());

        }
        else {
            if(target.getUserstatus().equals("NL")){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You cannot unlock a non-locked user!");
            }
            target.setUserstatus("NL");
            target.setTimeoffailedlogin(0);
            returnmessage.put("status","User "+target.getEmail()+" unlocked!");
            events.setAction("UNLOCK_USER");
            events.setObject("Unlock user "+map.get("user").toLowerCase());
        }
        events.setSubject(details.getUsername());

        events.setPath(request.getRequestURI());
        eventlist.getRecord().add(events);
        userRepo.save(target);
        return  ResponseEntity.status(HttpStatus.OK).body(returnmessage);
    }
    @GetMapping("/api/security/events")
    public ResponseEntity<Object> printsecurityevents(@AuthenticationPrincipal UserDetails details){
        User user =userDetailsServiceImpl.getUserfromemail(details.getUsername());
        setzero(user.getEmail());
        SecurityeventsList eventlist = SecurityeventsList.getInstance();
        List<Map<String,Object>> store = new ArrayList<>();

            for( Securityevents temp : eventlist.getRecord()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", temp.getId());
                map.put("date", temp.getDate());
                map.put("action", temp.getAction());
                map.put("subject",temp.getSubject());
                map.put("object", temp.getObject());
                map.put("path", temp.getPath());
                store.add(map);
            }
            return  ResponseEntity.status(HttpStatus.OK).body(store);
    }



}
