package multiagent;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Nichlas on 22-03-2017.
 */
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

        for (LinkedList<Node> p : joinPlan.values()){
            int s = p.size();
            System.err.println("max: " + maximumLength + "p: " + p.size());

            while (p.size() < maximumLength){
                Node copy = p.get(s-1).ChildNode();
                Command c = new Command();
                copy.action = c;
                p.addLast(copy);
            }
        }
    }
}
