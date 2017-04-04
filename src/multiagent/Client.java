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

    Stack<Goal> goalStack = new Stack<>();
    public void addWall(int row, int col){
        walls[row][col] = true;
    }

    public void removeWall(int row, int col){
        walls[row][col] = false;
    }

    public void UpdateCurrentState(Node n){
        currentState = n.ChildNode();
        currentState.agentCol = n.agentCol;
        currentState.agentRow = n.agentRow;
        currentState.action = n.action;
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
