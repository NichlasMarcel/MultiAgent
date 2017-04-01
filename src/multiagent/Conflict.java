package multiagent;

/**
 * Created by Nichlas on 01-04-2017.
 */
public class Conflict {
    ConflictTypes type;
    Client conflictingAgent;

    public Conflict(ConflictTypes type){
        this.type = type;
    }

    public Conflict(ConflictTypes type, Client conflictingAgent){
        this.type = type;
        this.conflictingAgent = conflictingAgent;
    }

    public Boolean IsConflict(){
        switch (type){
            case NoConflict:
                return false;
            default:
                return true;
        }
    }


}
