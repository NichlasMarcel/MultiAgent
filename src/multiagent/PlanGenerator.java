package multiagent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PlanGenerator {

    public static Node getNewStateForClient(CentralPlanner centralPlanner, Client c){
        return new Node(null,c);
    }

    public static char[][] GoalStateForPositionOfAgent(CentralPlanner centralPlanner, Client c, int row, int col){
        return new char[1][1];
    }

    public static char[][] GoalStateForPositionOfBox(CentralPlanner centralPlanner, Client c, int row, int col){
        return new char[1][1];
    }

    public static void FillWithNoOp(HashMap<Client,LinkedList<Node>> joinPlan){
        int maximumLength = 0;
        for (LinkedList<Node> p : joinPlan.values())
            maximumLength = Integer.max(maximumLength, p.size());

        for (Client client : joinPlan.keySet()){
            LinkedList<Node> p = joinPlan.get(client);
            int s = p.size();

                {
                while (p.size() < maximumLength){
                    Node copy = null;
                    if(s == 0){
                        copy = new Node(client.initialState, client);
                    }
                    else{
                        copy = p.get(s-1).ChildNode();
                    }

                    copy.agentRow = client.currentState.agentRow;
                    copy.agentCol = client.currentState.agentCol;

                    Command c = new Command();
                    copy.action = c;
                    p.addLast(copy);
                }
            }

        }
    }

    public static ConflictTypes IdentifyConflictType(HashMap<Client, LinkedList<Node>> plans){
        LinkedList<Node> sumOfPlans = new LinkedList<>();
        for(Client client : plans.keySet()){
            LinkedList<Node> plan = plans.get(client);
            for(Node sNode : sumOfPlans){
                for(Node aNode : plan){
                    if(sNode.agentRow == aNode.agentRow && sNode.agentCol == aNode.agentCol)
                        if(sNode.extractPlan().size() == aNode.extractPlan().size())
                            return ConflictTypes.AgentsBlockEachother;
                }
            }
            sumOfPlans.addAll(plan);
        }

        return ConflictTypes.NoConflict;
    }
}
