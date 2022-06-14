package account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface paymentsRepository extends JpaRepository<payments,Integer> {
    List<payments> findByemployee(String email);
    @Query("SELECT pay FROM payments pay WHERE pay.employee = ?1 AND pay.period = ?2")
    payments findByemailAndperiod(String email, String period);
    @Modifying
    @Query("update payments pay set pay.salary = ?1 where pay.paymentID = ?2")
    int setsalaryForpayments(Long salary, Long paymentID);
}
