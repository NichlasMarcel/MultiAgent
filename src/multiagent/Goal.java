package multiagent;

import java.util.LinkedList;

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
                    if(node.agentCol == n.agentCol && node.agentRow == n.agentRow || n.boxes[node.agentRow][node.agentCol] != 0 )
                        return false;

                    if (node.action.actionType == Command.Type.Push) {
                        int row = node.agentRow + Command.dirToRowChange(node.action.dir2);
                        int col = node.agentCol + Command.dirToColChange(node.action.dir2);
                        if (row == n.agentRow && col == n.agentCol || n.boxes[row][col] != 0)
                            return false;
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                    }

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
        boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        outerloop:
        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
//                if(Character.toLowerCase(CentralPlanner.boxes[i][j]) == goal_c  && CentralPlanner.goals[i][j] != goal_c){
//                    row = i;
//                    col = j;
//                    distance = 0;
//                }

                if(Character.toLowerCase(CentralPlanner.boxes[i][j]) == goal_c  && CentralPlanner.goals[i][j] != goal_c){
//                    if(distance > CentralPlanner.CalculateMathDistance(i,j,grow,gcol)){
//                        distance = CentralPlanner.CalculateMathDistance(i,j,grow,gcol);
//                    row = i;
//                    col = j;}

                        Goal goal = new Goal(grow,gcol);
                        goal.goal = GoalTypes.MoveToCell;
                        goal.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                        goal.goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];


                        Client c = new Client();
                        c.goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                        CentralPlanner.CopyBoxes(CentralPlanner.walls, c.walls);
                        Node n = new Node(null,c);
                        n.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                        n.agentRow = i;
                        n.agentCol = j;
                        c.SetInitialState(n);
                        c.addGoal(goal);

                        //c.goalStack.peek().UpdateBoxes();

                        LinkedList<Node> solution = CentralPlanner.GetPlanFromAgent(c);

                        if(solution == null)
                            continue;

                        if(solution.size() > 0){
                            boxes[i][j] = CentralPlanner.boxes[i][j];
                            break outerloop;
                        }
                    }

                }
            }
        }

//        System.err.println("BoxRow " + row);
//        System.err.println("BoxCol " + col);
//        if(row!=-1) {
//
//
//            boxRow = row;
//            boxCol= col;
//        }

    }

