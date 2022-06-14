package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepo;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(temp -> users.add(temp));
        for(User temp: users) {
            if(username.equalsIgnoreCase(temp.getEmail()))
            {
                return new UserDetailsImpl(temp);
            }
        }
        throw new UsernameNotFoundException("Not found: " + username);
    }
    public  boolean emailexistornot(String email){
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(temp -> users.add(temp));
        for(User temp: users) {
            if(email.equalsIgnoreCase(temp.getEmail()))
            {
                return true;
            }
        }
        return false;
    }
    public User getUserfromemail(String email) throws UsernameNotFoundException{
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(temp -> users.add(temp));
        for(User temp: users) {
            if(email.equalsIgnoreCase(temp.getEmail()))
            {
                return temp;
            }
        }
        throw new UsernameNotFoundException("Not found: " + email);
    }
}
