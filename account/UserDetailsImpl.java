package account;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class UserDetailsImpl implements UserDetails {
    private final String Username;
    private final String password;
    private final List<GrantedAuthority> rolesAndAuthorities= new ArrayList<>();
    private String userstatus;

    public UserDetailsImpl(User user) {
        Username= user.getEmail().toLowerCase();
        password = user.getPassword();
        rolesAndAuthorities.add(new SimpleGrantedAuthority(user.getRole()));
        userstatus= user.getUserstatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername(){
        return Username;
    }



    // 4 remaining methods that just return true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userstatus.equals("NL");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
