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
        c.Run();
    }


    public void Run(){
        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");
        List<Client> agentList = new ArrayList<>();
        // Read level and create the initial state of the problem
        for (int ax = 0; ax < agents.length; ax++) {
            for (int ay = 0; ay < agents[0].length; ay++) {
                if ('0' <= agents[ax][ay] && agents[ax][ay] <= '9') {
                    Client agent = new Client();
                    agent.color = colors.get(agents[ax][ay]);
                    agent.number = (int)agents[ax][ay];
                    Node initialNode = new Node(null, agent);
                    initialNode.agentRow = ax;
                    initialNode.agentCol = ay;
                    agent.initialState = initialNode;

                    System.err.println("COL " + agent.color);
                    agentList.add(agent);


                }
            }
        }
        System.err.println("Trying: "  + (int)'1');
        //sorting Clients by their number
        for  (Client agent : agentList)
            System.err.println("number:" + agent.getNumber());
        Collections.sort(agentList, new Comparator<Client>() {
            @Override
            public int compare(Client o1, Client o2) {
                return Integer.compare(o1.getNumber(),o2.getNumber());
            }
        });

        for  (Client agent : agentList)
            System.err.println("numberAfter:" + agent.getNumber());



        // Agents are going for the same boxes FIX THAT LATER
        for (Client agent : agentList) {
            char[][] aGoals = new char[MAX_ROW][MAX_COL];
            char[][] aBoxes = new char[MAX_ROW][MAX_COL];

            for (int gx = 0; gx < goals.length; gx++) {
                for (int gy = 0; gy < goals[0].length; gy++) {
                    if ('a' <= goals[gx][gy] && goals[gx][gy] <= 'z') {
                        for (int bx = 0; bx < boxes.length; bx++) {
                            for (int by = 0; by < boxes[0].length; by++) {
                                if (Character.toLowerCase(boxes[bx][by]) == goals[gx][gy]) {
                                    if (agent.color.equals(
                                            colors.get
                                                    (boxes[bx][by]))) {
                                        aGoals[gx][gy] = goals[gx][gy];
                                        aBoxes[bx][by] = boxes[bx][by];
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
        HashMap<Client,LinkedList<Node>> joinPlan = new HashMap<>();
        //for (Client agent: joinPlan.setKeys())
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

                joinPlan.put(agent,solution);
                //System.err.println(solution.toString());
            }
        }

        PlanGenerator.FillWithNoOp(joinPlan);

        // Execute plans from agents
        while (true) {


            String joinedAction = "[";
            HashMap<Client, Node> cmdForClients = new HashMap<>();
            List<Node> actions = new ArrayList<>();

            for (Client cP : agentList) {

                if(joinPlan.get(cP).size() == 0)
                    continue;

                Node n = joinPlan.get(cP).removeFirst();
                //actions.add(n);



                // This client action is not possible to apply.
                // We continue to replan until we get a plan with a first action that can be applied
              //  System.err.println("THIS IS RETURN OF BARTEK METHOD :" + CheckWhetherActionPossible.CheckIfActionCanBeApplied(actions, this));
                //while(CheckWhetherActionPossible.CheckIfActionCanBeApplied(actions, this)){
                //while(true){
                //while(count == 2 || count == 5 || count == 7 ){ was used for testing
                    //actions.remove(n);
                    // Recalculate a new goal for the client and make the client replan.
                    // or we can give the client a state where the cell which the client tried to move it, now would be a wall
                    // or we could add the NoOp operation to the agent

                    // Determine what the agent should plan for
                    // 1. Tell the agent to find a new plan.
                    // 2. Tell the agent to stay and continue with their plan afterwards
                    //System.err.println("CHECK ACTION WHILE INSIDE");
                    if(true) {
                        // 1. Tell the agent to find a new plan

                        //joinPlan.clear();
                        //System.err.println("¤¤¤¤¤¤¤¤¤¤ "+cP.Search(new Strategy.StrategyBFS()));
                        //joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS()));
                        //n = joinPlan.get(cP).removeFirst();
                        //System.err.println();
                        if(n.parent != null){
                            cP.initialState = n.parent;
                            //joinPlan.remove(cP);
                            //joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS()));
                            //n = joinPlan.get(cP).removeFirst();
                            System.err.println("AR: " + n.agentRow + "AC: " + n.agentCol);
                            //break;
                        }

                    } else {
                        // 2. Tell the agent to stay and continue with their plan afterwards
                        Node nnode = n.parent.ChildNode();
                        nnode.action = new Command(); // Adding NoOp
                        joinPlan.get(cP).addFirst(n);
                        n = nnode;
                        actions.add(nnode);
                    }

                //}




                PlanGenerator.FillWithNoOp(joinPlan);
                System.err.println();
                System.err.println("Clients: " + joinPlan.keySet().size());
                for (List<Node> l : joinPlan.values()){
                    System.err.println(l.size());
                }

                cmdForClients.put(cP, n);
                joinedAction += n.action.toString() + ",";
            }
            if(joinedAction.toCharArray()[joinedAction.length() - 1] == ',')
                joinedAction = joinedAction.substring(0, joinedAction.length() - 1);


            joinedAction += "]";

            ApplyAction(cmdForClients);
            System.err.println(joinedAction);

            //System.err.println(this);

            System.out.println(joinedAction);
            try{
                String response = in.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, joinedAction);
                    break;
                }
            }catch (Exception e){

            }

        }
    }

    public boolean IsCellFree(int row, int col){
        return !CentralPlanner.walls[row][col] && this.boxes[row][col] == 0 && !('0' <= this.agents[row][col] && this.agents[row][col] <= '9');
    }

    public boolean boxAt(int row, int col) {
        return ('A' <= this.boxes[row][col] && this.boxes[row][col] <= 'Z');
    }

    public void ApplyAction(HashMap<Client,Node> cmds){

        for (Client c : cmds.keySet()) {
            Node node = cmds.get(c);

            if(node.action.actionType == Command.Type.NoOp)
                continue;
            // Determine applicability of action
            int newAgentRow = node.agentRow + Command.dirToRowChange(node.action.dir1);
            int newAgentCol = node.agentCol + Command.dirToColChange(node.action.dir1);

            if (node.action.actionType == Command.Type.Move) {
                //if(node.parent.agentRow == 1 && node.parent.agentCol == 13){
                //    System.err.println("Agent found! " + agents[node.agentRow][node.agentCol]);
                //}
                // Check if there's a wall or box on the cell to which the agent is moving
                if (IsCellFree(node.agentRow, node.agentCol)) {
                    // System.err.println("Row: " + node.agentRow);
                    //System.err.println("Col: " + node.agentCol);


                    char agent = agents[node.parent.agentRow][node.parent.agentCol];
                    agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                    agents[node.agentRow][node.agentCol] = agent;
                    System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);

                    //System.err.println("AGE " + agent);
                    //System.err.println("NRow: " + newAgentRow);
                    //System.err.println("NCol: " + newAgentCol);
                }
                //else
                //System.err.println(this);
            } else if (node.action.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (boxAt(node.agentRow, node.agentCol)) {
                    int newBoxRow = node.agentRow + Command.dirToRowChange(node.action.dir2);
                    int newBoxCol = node.agentCol + Command.dirToColChange(node.action.dir2);
                    // .. and that new cell of box is free
                    if (IsCellFree(newBoxRow, newBoxCol)) {
                        char agent = agents[node.parent.agentRow][node.parent.agentCol];
                        char box = boxes[node.agentRow][node.agentCol];

                        agents[node.agentRow][node.agentCol] = agent;
                        boxes[newBoxRow][newBoxCol] = box;

                        agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                        boxes[node.agentRow][node.agentCol] = 0;

                        System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);
                        System.err.println("box: " + boxes[newBoxRow][newBoxCol] + " Row: " + newBoxRow + " Col: " + newBoxCol);
                        System.err.println("Oldbox: " + boxes[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);                        boxes[newBoxRow][newBoxCol] = box;


                    }
                }
            }
            //
            // Check this code;
            else if (node.action.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (IsCellFree(node.agentRow, node.agentCol)) {
                    int boxRow = node.parent.agentRow + Command.dirToRowChange(node.action.dir2);
                    int boxCol = node.parent.agentCol + Command.dirToColChange(node.action.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (boxAt(boxRow, boxCol)) {
                        char agent = agents[node.parent.agentRow][node.parent.agentCol];
                        agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                        agents[node.agentRow][node.agentCol] = agent;

                        char box = boxes[boxRow][boxCol];
                        boxes[boxRow][boxCol] = 0;
                        boxes[node.parent.agentRow][node.parent.agentCol] = box;
                    }
                }
                //else if (node.action.actionType == Command.Type.NoOp)
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < MAX_ROW; row++) {
            if (!this.walls[row][0]) {
                break;
            }
            for (int col = 0; col < MAX_COL; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (this.goals[row][col] > 0) {
                    s.append(this.goals[row][col]);
                } else if (this.walls[row][col]) {
                    s.append("+");
                } else if ('0' <= this.agents[row][col] && this.agents[row][col] <= '9') {
                    s.append(this.agents[row][col]);
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
