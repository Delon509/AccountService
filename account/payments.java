package account;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name="payments")
public class payments implements Serializable{
    public  static  final String[] MonthName={"January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"};
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name="paymentID")
    @Min(value = 0)
    private long paymentID=0;

    @Column(name="period")
    @NotBlank
    @Pattern(regexp = "[0-9]{2}-[0-9]{4}")
    private String period;

    @Column(name="salary")
    @Min(value = 0)
    private long salary=0;

    @Column(name="User_EMAIL")
    @NotBlank
    private String employee;

    public long getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(long paymentID) {
        this.paymentID = paymentID;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String USER_EMAIL) {
        this.employee = USER_EMAIL;
    }
    public String salaryToString(){
        long temp=this.getSalary();
        return (temp/100)+" dollar(s) "+(temp%100)+" cent(s)";
    }
    public String periodToString(){
        int month= Character.getNumericValue(this.getPeriod().charAt(0))*10 + Character.getNumericValue(this.getPeriod().charAt(1));
        return payments.MonthName[month-1]+this.getPeriod().substring(2);
    }

}
