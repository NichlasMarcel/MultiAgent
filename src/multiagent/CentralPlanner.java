package multiagent;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
    public List<Client> agentList;
    HashMap<Client, LinkedList<Node>> joinPlan;
    static Map<Character, String> colors = new HashMap<Character, String>();
    static Map<Integer, Client> clients = new HashMap<>();
    static Boolean sameColor = false;

    public CentralPlanner(BufferedReader serverMessages) {
        in = serverMessages;
    }

    public void LoadMap() throws IOException {// Read lines specifying colors
        String line, color;

        while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
            line = line.replaceAll("\\s", "");
            color = line.split(":")[0];

            for (String id : line.split(":")[1].split(","))
                colors.put(id.charAt(0), color);
        }

        if (colors.keySet().size() == 0)
            sameColor = true;


        int max_row = 0;
        boolean agentFound = false;
        int max_col = 0;

        ArrayList<String> store_contents = new ArrayList<String>();

        while (line != null) {
            if (line.isEmpty()) {
                break;
            }
            store_contents.add(line);

            if (line.length() - 1 > max_col) {
                max_col = line.length() - 1;
            }

            line = in.readLine();
            max_row++;
        }

        MAX_COL = max_col + 1;
        MAX_ROW = max_row + 1;

        goals = new char[MAX_ROW][MAX_COL];
        walls = new boolean[MAX_ROW][MAX_COL];
        agents = new char[MAX_ROW][MAX_COL];
        boxes = new char[MAX_ROW][MAX_COL];

        for (int row = 0; row < store_contents.size(); row++) {
            line = store_contents.get(row);
            for (int col = 0; col < line.length(); col++) {
                char chr = line.charAt(col);

                if (chr == '+') { // Wall.
                    walls[row][col] = true;
                } else if ('0' <= chr && chr <= '9') { // Agent.
                    if (sameColor) {
                        colors.put(chr, "red");
                    }
                    agents[row][col] = chr;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                    if (sameColor) {
                        colors.put(chr, "red");
                    }
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
/*
        for(int j = 0; j < agents.length; j++){
            for (int i = 0; i< agents[j].length; i++){
                if(agents[j][i] !=  '\u0000')
                    System.err.println("x: " + j + "y: " + i + "agent: " + agents[j][i] + "color: " + colors.get(agents[j][i]));
            }

        }
        */
    }

    public static void main(String[] args) throws IOException {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("1");
        CentralPlanner c = new CentralPlanner(serverMessages);
        System.err.println("2");
        c.LoadMap();
        System.err.println("3");
        c.Run();
    }

    public List<Client> createAgentList() {
        List<Client> agentList = new ArrayList<>();
        for (int ax = 0; ax < agents.length; ax++) {
            for (int ay = 0; ay < agents[0].length; ay++) {
                if ('0' <= agents[ax][ay] && agents[ax][ay] <= '9') {
                    Client agent = new Client();
                    agent.color = colors.get(agents[ax][ay]);
                    agent.number = Character.getNumericValue(agents[ax][ay]);
                    Node initialNode = new Node(null, agent);
                    initialNode.agentRow = ax;
                    initialNode.agentCol = ay;
                    agent.initialState = initialNode;
                    initialNode.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    agent.goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    //agent.calculateFromThisState = initialNode;

                    System.err.println("COL " + agent.color);
                    agentList.add(agent);
                }
            }
        }

        Collections.sort(agentList, new Comparator<Client>() {
            @Override
            public int compare(Client o1, Client o2) {
                return Integer.compare(o1.getNumber(), o2.getNumber());
            }
        });

        return agentList;
    }

    public Boolean CheckIfAgentIsBoxedIn(Client client) {
        char[][] previousBoxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];

        CopyBoxes(client.initialState.boxes, previousBoxes);

        for (int bx = 0; bx < boxes.length; bx++) {
            for (int by = 0; by < boxes[0].length; by++) {
                if (boxes[bx][by] != 0) {
                    if (!client.color.equals(
                            colors.get
                                    (boxes[bx][by]))) {
                        System.err.println("Row: " + bx + " Col: " + by + "Color: " + colors.get
                                (boxes[bx][by]));
                        client.addWall(bx, by);
                    }
                }
            }
        }

        LinkedList<Node> result = GetPlanFromAgent(client);
        CopyBoxes(walls, client.walls);

        System.err.println(client.initialState);

        if (result == null)
            return true;

        return false;


    }

    public void DivideStartGoals(List<Client> agentList) {
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

            CopyBoxes(aBoxes, agent.initialState.boxes);
            CopyBoxes(aGoals, agent.goals);

            Goal goal = new Goal(aGoals);
            goal.goal = GoalTypes.BoxOnGoal;
            agent.addGoal(goal);

            agent.UpdateCurrentState(agent.initialState);

            clients.put(agent.getNumber(), agent);
        }
    }

    public LinkedList<Node> GetPlanFromAgent(Client agent) {
        LinkedList<Node> solution = new LinkedList<>();
        Strategy strategy = new Strategy.StrategyBFS();
        try {
            solution = agent.Search(strategy, agent.initialState);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
        }

        if (solution == null) {
            System.err.println(strategy.searchStatus());
            System.err.println("Unable to solve level.");
            return null;
        } else {
            System.err.println("\nSummary for " + strategy.toString());
            System.err.println("Found solution of length " + solution.size());
            System.err.println(strategy.searchStatus());
        }

        return solution;
    }

    public HashMap<Client, LinkedList<Node>> GetPlansFromAgents(List<Client> agentList) {
        HashMap<Client, LinkedList<Node>> joinPlan = new HashMap<>();

        for (Client agent : agentList) {
            // One agent
            LinkedList<Node> solution = GetPlanFromAgent(agent);
            joinPlan.put(agent, solution);

        }

        return joinPlan;
    }

    public void AddNewPlanToAgent(Client cP, HashMap<Client, LinkedList<Node>> joinPlan) {
        if (cP.goalStack.size() == 0) {
            Node h = CreateNoOp(cP.currentState);
            joinPlan.get(cP).add(h);
        } else {
            System.err.println("Finished: " + cP.goalStack.peek().goal);
            cP.goalStack.pop();
            if (cP.goalStack.size() == 0) {
                AddNewPlanToAgent(cP, joinPlan);
                return;
            }


            System.err.println("Starting: " + cP.goalStack.peek().goal);

            System.err.println("FINALLY");
            //System.err.println("Walls in the map : " + cP.walls.toString());
            System.err.println("Initial state or rather just a state : " + cP.currentState);
            System.err.println("Agent Row: " + cP.currentState.agentRow + " Col: " + cP.currentState.agentCol);
            Strategy strategy = new Strategy.StrategyBFS();
            cP.SetInitialState(cP.currentState);
            LinkedList<Node> solution = cP.Search(strategy, cP.initialState);
            System.err.println(solution);
            if (solution == null) {
                System.err.println(strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + strategy.toString());
                System.err.println("Found solution of length " + solution.size());
                System.err.println(strategy.searchStatus());
            }

            cP.SetInitialState(cP.currentState);

            joinPlan.put(cP, solution);
        }
    }

    public Boolean HaveAgentFinishedHisGoals(Client cP, HashMap<Client, LinkedList<Node>> joinPlan) {

        if (joinPlan.get(cP).size() == 0) {
            return true;
        } else
            return false;

    }

    public void ReleaseAgents(){
        for (Client agent : agentList) {
            if (CheckIfAgentIsBoxedIn(agent)) {
                Conflict conflict = new Conflict(ConflictTypes.TrappedAgent, agent);
                joinPlan.put(agent,GetPlanFromAgent(agent));
                ConflictHandler.HandleConflict(conflict,null,this,null,null,joinPlan,null);
            }

        }
    }

    public void Run() {
        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Create agents
        agentList = createAgentList();

        // Divide start goals
        DivideStartGoals(agentList);

        // Get plans from agents
        joinPlan = GetPlansFromAgents(agentList);

        // Check If Agents are blocked in
        ReleaseAgents();

        //PlanGenerator.FillWithNoOp(joinPlan);

        // Execute plans from agents
        while (true) {
            HashMap<Client, Node> cmdForClients = new HashMap<>();
            List<Node> actions = new ArrayList<>();

            for (Client cP : agentList) {
                //System.err.println(cP.initialState);

                // Check if agent has satisfied all or some of his goal
                if (HaveAgentFinishedHisGoals(cP, joinPlan)) {
                    AddNewPlanToAgent(cP, joinPlan);
                }

                Node n = joinPlan.get(cP).removeFirst();
                actions.add(n);
                // This client action is not possible to apply.
                // We continue to replan until we get a plan with a first action that can be applied
                //  System.err.println("THIS IS RETURN OF BARTEK METHOD :" + ConflictDetector.CheckIfActionCanBeApplied(actions, this));

                Conflict conflict = multiagent.ConflictDetector.CheckIfActionCanBeApplied(actions, this);
                if (conflict.IsConflict()) {
                    System.err.println("Conflict type: " + conflict.type);
                    actions.remove(n);
                    // Find New Action
                    n = ConflictHandler.HandleConflict(conflict, n, this, cmdForClients, cP, joinPlan, actions);
                }


                System.err.println();
                System.err.println("Clients: " + joinPlan.keySet().size());
                for (List<Node> l : joinPlan.values()) {
                    System.err.println(l.size());
                }
                cmdForClients.put(cP, n);

            }

            //PlanGenerator.FillWithNoOp(joinPlan);

            String joinedAction = "[";

            for (Client a : agentList) {
                joinedAction += cmdForClients.get(a).action.toString() + ",";
            }

            if (joinedAction.toCharArray()[joinedAction.length() - 1] == ',')
                joinedAction = joinedAction.substring(0, joinedAction.length() - 1);


            joinedAction += "]";

            ApplyAction(cmdForClients);

            System.err.println(joinedAction);
            System.out.println(joinedAction);

            try {
                String response = in.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, joinedAction);
                    break;
                }
            } catch (Exception e) {

            }

        }
    }

    public boolean IsCellFree(int row, int col) {
        return !CentralPlanner.walls[row][col] && this.boxes[row][col] == 0 && !('0' <= this.agents[row][col] && this.agents[row][col] <= '9');
    }

    public boolean boxAt(int row, int col) {
        return ('A' <= this.boxes[row][col] && this.boxes[row][col] <= 'Z');
    }

    public void ApplyAction(HashMap<Client, Node> cmds) {

        for (Client c : cmds.keySet()) {
            Node node = cmds.get(c);
            c.UpdateCurrentState(node);

            if (node.action.actionType == Command.Type.NoOp)
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
                    //System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);

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
/*
                        System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);
                        System.err.println("box: " + boxes[newBoxRow][newBoxCol] + " Row: " + newBoxRow + " Col: " + newBoxCol);
                        System.err.println("Oldbox: " + boxes[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);                        boxes[newBoxRow][newBoxCol] = box;
*/

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

    public static void CopyBoxes(char[][] boxesToCopy, char[][] receiver) {
        for (int i = 0; i < boxesToCopy.length; i++)
            for (int j = 0; j < boxesToCopy[i].length; j++) {
                receiver[i][j] = boxesToCopy[i][j];
                //receiver[i][j] =  boxesToCopy[i][j];
            }
    }

    public static void CopyBoxes(boolean[][] boxesToCopy, boolean[][] receiver) {
        for (int i = 0; i < boxesToCopy.length; i++)
            for (int j = 0; j < boxesToCopy[i].length; j++) {
                receiver[i][j] = boxesToCopy[i][j];
                //receiver[i][j] =  boxesToCopy[i][j];
            }
    }

    public Node CreateNoOp(Node node) {
        Node n = new Node(node, node.c);
        n.action = new Command();
        n.agentRow = node.agentRow;
        n.agentCol = node.agentCol;
        CopyBoxes(node.boxes, n.boxes);
        return n;
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
