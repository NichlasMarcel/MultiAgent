package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Nichlas on 14-03-2017.
 */
public class Client {
    private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

    //private List< Agent > agents = new ArrayList< Agent >();
    public Node initialState;
    public char[][] goals; // Taget fra node
   /* public static int MAX_ROW;
    public static int MAX_COL;*/
    public String color;
    static Map< Character, String > colors = new HashMap< Character, String >();

    public Client() {
    }

    public LinkedList<Node> Search(Strategy strategy) {
        System.err.format("Search starting with strategy %s.\n", strategy.toString());
        strategy.addToFrontier(this.initialState);

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
