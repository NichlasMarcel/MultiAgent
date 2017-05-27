package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
public class Client {
    private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
    public ArrayList<GoalCell> temporaryWalls = new ArrayList<>();
    public Node initialState;
    public Node currentState;
    public char[][] goals; // Taget fra node
    public boolean[][] walls;

    public String color;
    static Map< Character, String > colors = new HashMap< Character, String >();
    public int number;
    public int getNumber(){
      return number;
    }
    List<Node> nodesVisited = new ArrayList<>();
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



    public void addTemporraryWall(int row, int col)
    {
        walls[row][col] = true;
        temporaryWalls.add(new GoalCell(row,col,'c'));
    }


    public void removeWall(int row, int col){
        walls[row][col] = false;
    }

    public void AddTemporarryWalls()
    {
        for (GoalCell g: temporaryWalls)
        {
            addWall(g.x,g.y);
        }
    }

    public void removeAllWalls()
    {
        for (GoalCell g: temporaryWalls)
        {
            removeWall(g.x,g.y);
        }
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
