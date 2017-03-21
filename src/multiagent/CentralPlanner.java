package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Nichlas on 21-03-2017.
 */
public class CentralPlanner {
    private BufferedReader in;

    //private List< Agent > agents = new ArrayList< Agent >();
    public Node initialState;
    public char[][] goals; // Taget fra node
    public char[][] agents; // Taget fra node
    public char[][] boxes;
    public static boolean[][] walls; // Taget fra node
    public static int MAX_ROW;
    public static int MAX_COL;
    static Map<Character, String> colors = new HashMap< Character, String >();

    public CentralPlanner(BufferedReader serverMessages) {
        in = serverMessages;
    }

    public void LoadMap()throws IOException {// Read lines specifying colors
        String line, color;

        while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
            line = line.replaceAll( "\\s", "" );
            color = line.split( ":" )[0];

            for ( String id : line.split( ":" )[1].split( "," ) )
                colors.put( id.charAt( 0 ), color );
        }

        int max_row = 0;
        boolean agentFound = false;


        int max_col = 0;

        ArrayList<String> store_contents = new ArrayList<String>();

        while (!line.equals("")) {

            store_contents.add(line);

            if(line.length() - 1 > max_col)
                max_col = line.length() - 1;

            line = in.readLine();
            max_row++;
        }

        MAX_COL = max_col+1;
        MAX_ROW = max_row+1;

        goals = new char[MAX_ROW][MAX_COL];
        walls = new boolean[MAX_ROW][MAX_COL];
        agents = new char[MAX_ROW][MAX_COL];
        boxes = new char[MAX_ROW][MAX_COL];

        for(int row = 0; row < store_contents.size(); row++){
            line = store_contents.get(row);
            for (int col = 0; col < line.length(); col++) {
                char chr = line.charAt(col);

                if (chr == '+') { // Wall.
                    walls[row][col] = true;
                } else if ('0' <= chr && chr <= '9') { // Agent.
                    agents[row][col] = chr;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                    boxes[row][col] = chr;
                } else if ('a' <= chr && chr <= 'z') { // Goal.
                    goals[row][col] = chr;
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
        }

        for(int j = 0; j < agents.length; j++){
            for (int i = 0; i< agents[j].length; i++){
                if(agents[j][i] !=  '\u0000')
                    System.err.println("x: " + j + "y: " + i + "agent: " + agents[j][i] + "color: " + colors.get(agents[j][i]));
            }

        }
    }

    public static void main( String[] args ) throws IOException {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        CentralPlanner c = new CentralPlanner(serverMessages);
        c.LoadMap();
        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");
        List<Client> agentList = new ArrayList<>();
        // Read level and create the initial state of the problem
        for (int ax = 0; ax < c.agents.length; ax++) {
            for (int ay = 0; ay < c.agents[0].length; ay++) {
                if ('0' <= c.agents[ax][ay] && c.agents[ax][ay] <= '9') {
                    Client agent = new Client();
                    Node initialNode = new Node(null, agent);
                    initialNode.agentRow = ax;
                    initialNode.agentCol = ay;
                    agent.initialState = initialNode;
                    agent.color = colors.get(c.agents[ax][ay]);
                    System.err.println(agent.color);
                    agentList.add(agent);
                }
            }
        }
        // Agents are going for the same boxes FIX THAT LATER
        for (Client agent : agentList) {
            char[][] aGoals = new char[MAX_ROW][MAX_COL];
            char[][] aBoxes = new char[MAX_ROW][MAX_COL];

            for (int gx = 0; gx < c.goals.length; gx++) {
                for (int gy = 0; gy < c.goals[0].length; gy++) {
                    if ('a' <= c.goals[gx][gy] && c.goals[gx][gy] <= 'z') {
                        for (int bx = 0; bx < c.boxes.length; bx++) {
                            for (int by = 0; by < c.boxes[0].length; by++) {
                                if (Character.toLowerCase(c.boxes[bx][by]) == c.goals[gx][gy]) {
                                    if (agent.color.equals(
                                            colors.get
                                                    (c.boxes[bx][by]))) {
                                        aGoals[gx][gy] = c.goals[gx][gy];
                                        aBoxes[bx][by] = c.boxes[bx][by];
                                    }
                                }
                            }
                        }
                    }

                }
            }

            agent.initialState.boxes = aBoxes;
            agent.goals = aGoals;

        }


        // Get plans from agents
        ArrayList<LinkedList<Node>> joinPlan = new ArrayList<>();
        for (Client agent : agentList) {


            // One agent
            LinkedList<Node> solution;
            Strategy strategy = new Strategy.StrategyBFS();
            try {
                solution = agent.Search(strategy);
            } catch (OutOfMemoryError ex) {
                System.err.println("Maximum memory usage exceeded.");
                solution = null;
            }

            if (solution == null) {
                System.err.println(strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + strategy.toString());
                System.err.println("Found solution of length " + solution.size());
                System.err.println(strategy.searchStatus());

                joinPlan.add(solution);
            }
        }

        while (true) {
            String joinedAction = "[";
            for (LinkedList<Node> plan : joinPlan) {
                if (joinedAction.equals("["))
                    joinedAction += plan.getFirst().action.toString();
                else
                    joinedAction += plan.getFirst().action.toString() + ",";
            }

            joinedAction = joinedAction.substring(0, joinedAction.length() - 2);
            joinedAction += "]";

            System.out.println(joinedAction);

            String response = serverMessages.readLine();
            if (response.contains("false")) {
                System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, joinedAction);
                break;
            }
        }
    }

}
