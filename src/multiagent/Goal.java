package multiagent;

import java.util.LinkedList;

/**
 * Created by Nichlas on 01-04-2017.
 */
public class Goal {
    GoalTypes goal;

    int agentRow;
    int agentCol;
    char[][] boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
    char[][] goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
    Client client;
    LinkedList<Node> path;
    // Default goal constructor
    public Goal(){}

    public Goal(char[][] goals, char[][] boxes){
        this.goals = goals;
        this.boxes = boxes;
    }

    int boxRow, boxCol, goalRow, goalCol;

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
                    if(node.agentCol == n.agentCol && node.agentRow == n.agentRow || n.boxes[node.agentRow][node.agentCol] != 0)
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

    public void UpdateBoxes(){
        char goal_c = 0;
        boolean finished = false;
        int grow = -1;
        int gcol = -1;
        int row = -1;
        int col = -1;
        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
            if(finished)
                break;
            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
                if(goals[i][j] != 0){
                    goal_c = goals[i][j];
                    grow = i;
                    gcol = j;
                    finished = true;
                    break;
                }
            }
        }

        System.err.println("grow: " + grow);
        System.err.println("gcow: " + gcol);
        System.err.println("goal_c: " + goal_c);

        double distance = Double.MAX_VALUE;

        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
                if(Character.toLowerCase(CentralPlanner.boxes[i][j]) == goal_c  && CentralPlanner.goals[i][j] != goal_c){
                    if(distance > CentralPlanner.CalculateMathDistance(i,j,grow,gcol))
                        distance = CentralPlanner.CalculateMathDistance(i,j,grow,gcol);
                    row = i;
                    col = j;
                }
            }
        }

        System.err.println("BoxRow " + row);
        System.err.println("BoxCol " + col);
        if(row!=-1) {
            boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
            boxes[row][col] = CentralPlanner.boxes[row][col];
        }
       // boxRow = row;
      //  boxCol= col;
    }
}
