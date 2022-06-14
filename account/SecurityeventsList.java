package account;

import java.util.ArrayList;
import java.util.List;

public class SecurityeventsList {
    private static final SecurityeventsList INSTANCE = new SecurityeventsList();
    private final List<Securityevents> record= new ArrayList<>();
    private SecurityeventsList() { }

    public List<Securityevents> getRecord() {
        return record;
    }

    public static SecurityeventsList getInstance() {
        return INSTANCE;
    }
}

