package server.ResImpl;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TransGenericResourceManager extends GenericResourceManager {
    class Tuple3 {
        String key;
        int operation;    // 0 - read, 1 - write
        RMItem newvalue;
    }

    private ConcurrentHashMap<Integer, ArrayList<Tuple3>> operationList =
            new ConcurrentHashMap<Integer, ArrayList<Tuple3>>();//transaction_id -> tuple2 list

    public boolean abort(int transid) {
        if (operationList.containsKey(transid)) {
            //realize the operations
            operationList.remove(transid);
            return true;
        }
        return false;
    }

    public boolean commit(int transId) {
        if (operationList.containsKey(transId)) {
            //realize the operations
            for (Tuple3 t3 : operationList.get(transId)) {
                if (t3.operation == 0) {
                    if (super.readDatafromRM(transId, t3.key) == null)
                        return false;
                } else {
                    super.writeData(transId, t3.key, t3.newvalue);
                }
            }
            return true;
        }
        return false;
    }
}
