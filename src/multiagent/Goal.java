package multiagent;

import java.util.LinkedList;

/**
 * Created by Nichlas on 01-04-2017.
 */
public class Goal {
    GoalTypes goal;

    int agentRow;
    int agentCol;
    char[][] boxes;
    char[][] goals;
    Client client;
    LinkedList<Node> path;
    // Default goal constructor
    public Goal(){}

    public Goal(char[][] goals, char[][] boxes){
        this.goals = goals;
        this.boxes = boxes;
    }

    // MoveToCell constructor
    public Goal(int agentRow, int agentCol){
        this.agentRow = agentRow;
        this.agentCol = agentCol;
    }

    // MoveToEmptyCell constructor
    public Goal(LinkedList<Node> path){
        this.path = path;
    }

    public Boolean IsGoal(Node n){
        switch (goal){
            case MoveToCell:
                if(this.agentRow == n.agentRow && this.agentCol == n.agentCol)
                    return true;
                else
                    return false;
            case MoveToEmptyCell:
                for(Node node : path){
                    if(node.agentCol == n.agentCol && node.agentRow == n.agentRow)
                        return false;
                }
                return true;
            case FreeAgent:
                System.err.println("FreeAgent: Size " + path.size());
                System.err.println("InitialState: Test Boxes");
                System.err.println(n);

                for(Node node : path){
                    System.err.println("AgentRow: " + node.agentRow + " Col: " + node.agentCol);
                    System.err.println("Box: " + n.boxes[node.agentRow][node.agentCol]);
                    if(n.boxes[node.agentRow][node.agentCol] != 0 || (n.agentRow == node.agentRow && n.agentCol == node.agentCol))
                        return false;
                }

                return true;
            default:
                for (int row = 1; row < CentralPlanner.MAX_ROW - 1; row++) {
                    for (int col = 1; col < CentralPlanner.MAX_COL - 1; col++) {
                        char g = goals[row][col];
                        char b = Character.toLowerCase(n.boxes[row][col]);
                        if (g > 0 && b != g) {
                            return false;
                        }
                    }
                }
                return true;
        }
    }
}
