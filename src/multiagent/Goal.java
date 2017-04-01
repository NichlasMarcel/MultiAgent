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
    public Goal(char[][] goals){
        this.goals = goals;
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

    public Boolean IsGoal(int agentRow, int agentCol, char[][] boxes){
        switch (goal){
            case MoveToCell:
                if(this.agentRow == agentRow && this.agentCol == agentCol)
                    return true;
                else
                    return false;
            case MoveToEmptyCell:
                for(Node node : path){
                    if(node.agentCol == agentCol && node.agentRow == agentRow)
                        return false;
                }
                return true;
            default:
                for (int row = 1; row < CentralPlanner.MAX_ROW - 1; row++) {
                    for (int col = 1; col < CentralPlanner.MAX_COL - 1; col++) {
                        char g = goals[row][col];
                        char b = Character.toLowerCase(boxes[row][col]);
                        if (g > 0 && b != g) {
                            return false;
                        }
                    }
                }
                return true;
        }
    }
}
