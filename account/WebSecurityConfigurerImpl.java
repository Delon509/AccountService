package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import javax.servlet.http.HttpServletResponse;


@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public static  final  String[] ACCOUNTANTGROUP={"ACCOUNTANT","ACCOUNTANT,ROLE_AUDITOR","ACCOUNTANT,ROLE_AUDITOR,ROLE_USER","ACCOUNTANT,ROLE_USER"}; // A, AB, ABC,AC
    public static  final  String[] AUDITORGROUP={"AUDITOR","ACCOUNTANT,ROLE_AUDITOR","ACCOUNTANT,ROLE_AUDITOR,ROLE_USER","AUDITOR,ROLE_USER"}; // A,AB,ABC,AC
    public  static  final  String[] Bussiness={"ACCOUNTANT","ACCOUNTANT,ROLE_AUDITOR","ACCOUNTANT,ROLE_AUDITOR,ROLE_USER","ACCOUNTANT,ROLE_USER","AUDITOR","AUDITOR,ROLE_USER","USER"};
    public static  final  String[] all={"USER","ADMINISTRATOR","ACCOUNTANT","ACCOUNTANT,ROLE_AUDITOR","ACCOUNTANT,ROLE_AUDITOR,ROLE_USER",
            "ACCOUNTANT,ROLE_USER","AUDITOR","ACCOUNTANT,ROLE_AUDITOR","AUDITOR,ROLE_USER"};
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handle auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .antMatchers(HttpMethod.POST, "/api/signup").permitAll()
                .antMatchers(HttpMethod.POST,"/api/auth/changepass").hasAnyRole(all)
                .antMatchers(HttpMethod.GET,"/api/security/events/").hasAnyRole(AUDITORGROUP)
                .mvcMatchers("/api/acct/payments").hasAnyRole(ACCOUNTANTGROUP)
                .mvcMatchers("/api/acct/payments/**").hasAnyRole(ACCOUNTANTGROUP)
                .mvcMatchers("/api/empl/payment").hasAnyRole(Bussiness)
                .mvcMatchers("/api/admin/user").hasRole("ADMINISTRATOR")
                .mvcMatchers("/api/admin/user/**").hasRole("ADMINISTRATOR")

                .and().httpBasic()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()



                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);; // no session
    }
    // creating a PasswordEncoder that is needed in two places
    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(13);
    }
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
