package interfaces;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public interface StatementInterface extends Serializable {

    int getAccountnum();  // returns account number associated with this statement
    Date getStartDate(); // returns start Date of StatementInterface
    Date getEndDate(); // returns end Date of StatementInterface
    String getAccoutName(); // returns name of account holder
    List getTransations(); // returns list of Transaction objects that encapsulate details about each transaction
    //String heartBeat(); // returns "live" if success
}