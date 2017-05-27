package multiagent;

public class Conflict {
    ConflictTypes type;
    public Command action = null;
    Client conflictingAgent;
    public char[][] agents;
    public char[][] boxes;
    public Conflict(ConflictTypes type){
        this.type = type;
    }

    public Conflict(ConflictTypes type, Client conflictingAgent){
        this.type = type;
        this.conflictingAgent = conflictingAgent;
    }

    public Conflict(ConflictTypes type, char[][] agents, char[][] boxes){
        this.type = type;
        this.agents = agents;
        this.boxes = boxes;
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
