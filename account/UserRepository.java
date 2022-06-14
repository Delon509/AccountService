package account;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    User findByemail(String email);
    Boolean existsByemail(String email);
    @Query(value = "SELECT max(employeeid) FROM user")
    int getMaxemployeeid();
    long deleteByemployeeid(long employeeid);
}
