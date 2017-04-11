package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Nichlas on 14-03-2017.
 */
public class Client {
    private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

    //private List< Agent > agents = new ArrayList< Agent >();
    public Node initialState;
    public Node currentState;
    public char[][] goals; // Taget fra node
    public boolean[][] walls;
   /* public static int MAX_ROW;
    public static int MAX_COL;*/
    public String color;
    static Map< Character, String > colors = new HashMap< Character, String >();
    public int number;
    public int getNumber(){
      return number;
    }
    public  Goal currentGoal;


    Stack<Goal> goalStack = new Stack<>();

    public void SetInitialState(Node n){
        initialState = new Node(null, this);
        initialState.agentRow = n.agentRow;
        initialState.agentCol = n.agentCol;
        initialState.g = n.g;
        CopyBoxes(n.boxes, initialState.boxes);

    }

    public void addWall(int row, int col){
        walls[row][col] = true;
    }

    public void removeWall(int row, int col){
        walls[row][col] = false;
    }


    public Goal getBestGoal()
    {   double minDistance = Double.MAX_VALUE;
        Goal bestGoal = null;
        for (Goal g: goalStack)
        {
            double distance = CentralPlanner.CalculateMathDistance(currentState.agentRow, currentState.agentCol, g.boxRow, g.boxCol) +  CentralPlanner.CalculateMathDistance(g.boxRow,g.boxCol, g.goalRow,g.goalCol);
            if (minDistance> distance)
            {
                minDistance = distance;
                bestGoal = g;
            }
        }


        Box box = new Box(bestGoal.boxRow, bestGoal.boxCol, CentralPlanner.boxes[bestGoal.boxRow][bestGoal.boxCol]);
        System.err.println("Box surrounded: "  +  box.surrounded() + " walls" +  " in total: "  + box.getSurroundingBoxes(CentralPlanner.boxes).size() );
        if (box.findBlockingBoxes(CentralPlanner.boxes).size()>0)
        {
            for (Box b: box.findBlockingBoxes(CentralPlanner.boxes))
            { System.err.println("here");
            System.err.println("Box: " + b.c + " Surrounded by: ");
            System.err.println(b.findBlockingBoxes(CentralPlanner.boxes));

                if (b.findBlockingBoxes(CentralPlanner.boxes).size()==0)
                {
                    System.err.println("Not here");

                    int boxRow =b.x;
                    int boxCol =b.y;
                    for (Goal g: goalStack)
                    {
                        if ((g.boxRow==boxRow) && (g.boxCol == boxCol)) {
                            bestGoal = g;
                            System.err.println("Last");
                            break;
                        }

                    }
                }

            }
        }
        if (goalStack.size()>1) {
            goalStack.remove(bestGoal);
            goalStack.push(bestGoal);
        }
        System.err.println("best goal");
        try {
            System.err.println("Box: " + bestGoal.boxes[bestGoal.boxRow][bestGoal.boxCol] + "Goal: " + bestGoal.goals[bestGoal.goalRow][bestGoal.goalCol] + " Distance:  " + minDistance);
        }
        catch (Exception e)
        {
            System.err.println(e);
        }


        return bestGoal;
    }

    public void UpdateCurrentState(Node n){
        currentState = new Node(n.parent,this);
        currentState.agentCol = n.agentCol;
        currentState.agentRow = n.agentRow;
        currentState.action = n.action;
        CopyBoxes(n.boxes, currentState.boxes);
    }

    public void CopyBoxes(char[][] boxesToCopy, char[][] receiver){
        for (int i=0; i<boxesToCopy.length; i++)
            for (int j=0; j<boxesToCopy[i].length; j++)
            {
                receiver[i][j] = boxesToCopy[i][j];
                //receiver[i][j] =  boxesToCopy[i][j];
            }
    }

    public Client() {
        walls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];

        for(int i = 0; i < CentralPlanner.MAX_ROW; i++){
            for(int j = 0; j < CentralPlanner.MAX_COL; j++){
                this.walls[i][j] = CentralPlanner.walls[i][j];
            }
        }
    }

    public void addGoal(Goal goal){
        goalStack.push(goal);
    }

    public LinkedList<Node> Search(Strategy strategy, Node node) {
        System.err.format("Search starting with strategy %s.\n", strategy.toString());
        strategy.addToFrontier(node);

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(strategy.searchStatus());
                iterations = 0;
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();

            if (leafNode.isGoalState()) {
                return leafNode.extractPlan();
            }

            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }


}
