package account;



import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "user")
public class User implements Serializable,Comparable<User> {
    public  static  long countforsignup=0;
    public  static  final String[] breachedpassword={"PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="employeeid")
    @Min(value = 0)
    private long employeeid=0;

    @Column(name="LASTNAME")
    @NotBlank
    private String lastname;

    @Column(name="NAME")
    @NotBlank
        private String name;

    @Column (name="PASSWORD")
    @NotBlank
    @Size(min=12,message= "Password length must be 12 chars minimum!")
    private String password;

    @Column (name="ROLE")
    private String role;

    @Id
    @Column (name="EMAIL")
    @NotBlank
        private String email;
    @Column(name="userstatus")
        private String userstatus="NL";
    @Column(name="timeoffailedlogin")
    @Min(value = 0)
        private long timeoffailedlogin=0;





    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return employeeid == that.employeeid && name.equals(that.name) && lastname.equals(that.lastname) && email.equals(that.email) && password.equals(that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeid);
    }

    public long getemployeeid() {
        return employeeid;
    }

    public void setemployeeid(long employeeid) {
        this.employeeid = employeeid;
    }

    public String getName() {
        return name;
    }
    public void setname(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserstatus() {
        return userstatus;
    }

    public void setUserstatus(String userstatus) {
        this.userstatus = userstatus;
    }

    public long getTimeoffailedlogin() {
        return timeoffailedlogin;
    }

    public void setTimeoffailedlogin(long timeoffailedlogin) {
        this.timeoffailedlogin = timeoffailedlogin;
    }

    public  User(String name, String lastname, String email, String password ) {
        this.name=name;
        this.lastname=lastname;
        this.email=email;
        this.password=password;
        this.role="ROLE_USER";
    }
    public User(){}
    public  static  boolean checkvaildsignup(User user)
    {
        for(String temp: User.breachedpassword){
            if(temp.equalsIgnoreCase(user.getPassword()))
                return false;
        }
        return  user.getEmail().endsWith("@acme.com");
    }
    public  static  boolean checkisbreachedpassword(String pw)
    {
        for(String temp: User.breachedpassword){
            if(temp.equalsIgnoreCase(pw))
                return true;
        }
        return  false;
    }

    @Override
    public int compareTo(User o) {
        return Long.compare(this.getemployeeid(), o.getemployeeid());
    }
}
