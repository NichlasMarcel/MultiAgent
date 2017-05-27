package multiagent;


import sun.awt.image.ImageWatched;

import java.io.*;
import java.util.*;
import java.util.Map;


/**
 * Created by Nichlas on 21-03-2017.
 */
public class CentralPlanner {
    private BufferedReader in;
    public GoalCell[] temporarryWalls;
    //private List< Agent > agents = new ArrayList< Agent >();
    public static char[][] goals; // Taget fra node
    public static char[][] agents; // Taget fra node
    public static char[][] boxes;
    public static boolean[][] walls; // Taget fra node
    public static int MAX_ROW;
    public static int MAX_COL;
    public List<Client> agentList;
    String response ="";
    HashMap<Client, LinkedList<Node>> joinPlan;
    static Map<Character, String> colors = new HashMap<Character, String>();
    static Map<Integer, Client> clients = new HashMap<>();
    static Boolean sameColor = false;
    static GoalCell[][] goalsMap ;
    static PrintWriter writer;
    ArrayList<GoalCell> goalsArray  = new ArrayList<>();

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

//        in.readLine();
        MAX_COL = max_col + 1;
        MAX_ROW = max_row + 1;
        goalsMap = new GoalCell[MAX_ROW][MAX_COL];
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
                    goalsMap[row][col]  = new GoalCell(row,col,chr);
                    goalsArray.add(  goalsMap[row][col] );
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
        }

        for (GoalCell g: goalsArray)
        {
            g.findGoalsBefore();
        }


        Thread thread1 = new Thread() {
            public void run() {

            }
        };
        thread1.start();


    }


    public String createKey(int i1, int i2, int j1, int j2)
    {
        return i1 + ":" + j1+ ":" + i2+ ":" + j2;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("1");
        CentralPlanner c = new CentralPlanner(serverMessages);
        System.err.println("2");
        c.LoadMap();
        System.err.println("3");
        //System.err.println(args);

        if (args.length>0)
        {
            System.setErr(new PrintStream(new OutputStream() {
                public void write(int b) {
                }
            }));

            try{
                writer= new PrintWriter(args[0] +".txt", "UTF-8");

            } catch (IOException e) {
                // do something
            }
        }

        c.Run();
        writer.close();

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
                    agent.UpdateCurrentState(agent.initialState);
                    System.err.println("COL " + agent.color);
                    clients.put(agent.getNumber(), agent);
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

        if(client.goalStack.size() == 0)
            return false;

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



    public void DivideGoals2(List<Client> agentList){

        // Find closest reachable box to the goal!
        HashMap<Goal,Character> goals = new HashMap<>();
        Client c = new Client();
        c.goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        CopyBoxes(walls, c.walls);

        for (int i = 0; i < MAX_ROW; i++)
            for (int j = 0; j < MAX_COL; j++)
                if (CentralPlanner.goals[i][j] != 0) {
                    Goal goal = new Goal(i,j);
                    goal.goal = GoalTypes.MoveToCell;
                    goal.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    goal.goals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    goals.put(goal, CentralPlanner.goals[i][j]);
                }

        for (Goal g : goals.keySet()) {
            int distance = Integer.MAX_VALUE;
            int boxRow = -1;
            int boxCol = -1;
            for (int i = 0; i < MAX_ROW; i++)
                for (int j = 0; j < MAX_COL; j++)
                    if(Character.toLowerCase(boxes[i][j]) == goals.get(g)){
                        Node n = new Node(null,c);
                        n.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                        n.agentRow = i;
                        n.agentCol = j;
                        c.SetInitialState(n);
                        g.goal = GoalTypes.MoveToCell;
                        c.addGoal(g);

                        c.goalStack.peek().UpdateBoxes();

                        LinkedList<Node> solution = GetPlanFromAgent(c);
                        if(solution.size() > 0 && solution.size() < distance){
                            distance = solution.size();
                            boxRow = i;
                            boxCol = j;
                        }
                    }

            // Find closes reachable agent to the box
            distance = Integer.MAX_VALUE;
            Goal agent_box = new Goal(boxRow,boxCol);
            agent_box.goal = GoalTypes.MoveToCell;
            Client winner = null;
            for(Client agent : agentList){
                if(!agent.color.equals(colors.get(boxes[boxRow][boxCol])))
                    continue;

                agent.addGoal(agent_box);

                LinkedList<Node> solution = GetPlanFromAgent(agent);
                agent.goalStack.pop();
                if(solution == null){

                    continue;

                }

                if(solution.size() > 0 && solution.size() < distance){

                    distance = solution.size();
                    winner = agent;
                }
            }

            winner.initialState.boxes[boxRow][boxCol] = boxes[boxRow][boxCol];
            winner.goals[g.agentRow][g.agentCol] = this.goals[g.agentRow][g.agentCol];
            char[][] aBoxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
            char[][] aGoals = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
            aBoxes[boxRow][boxCol] = boxes[boxRow][boxCol];
            aGoals[g.agentRow][g.agentCol] = this.goals[g.agentRow][g.agentCol];

            Goal goal = new Goal(aGoals,aBoxes);
            goal.goal = GoalTypes.BoxOnGoal;
            goal.boxRow = boxRow;
            goal.boxCol = boxCol;
            goal.goalRow = g.agentRow;
            goal.goalCol = g.agentCol;
            goal.goals = aGoals;
            goal.boxes = aBoxes;

            goal.client= winner;
            winner.addGoal(goal);
            winner.UpdateCurrentState(winner.initialState);
        }

        for (Client agent: agentList)
        {
            System.err.println("size of goal stack: " +agent.getNumber()+ " : "  +agent.goalStack.size() );
        }


    }

    public  static double CalculateMathDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)* (y1-y2));
    }

    public static LinkedList<Node> GetPlanFromAgent(Client agent) {
        LinkedList<Node> solution = new LinkedList<>();

        //agent.initialState.boxes = agent.goalStack.peek().boxes ;
        Strategy strategy = null;
        try {
            if (agent.goalStack.size()!=0) {
                if(agent.goalStack.peek().goal != GoalTypes.FreeAgent) {
                    CopyBoxes(agent.goalStack.peek().boxes, agent.initialState.boxes);
                    CopyBoxes(agent.goalStack.peek().goals, agent.goals);
                }

                switch (agent.goalStack.peek().goal){
                    case BoxOnGoal:
                        System.err.println("Goal row: " + agent.goalStack.peek().goalRow + " col: " + agent.goalStack.peek().goalCol);
                        System.err.println("Agent InitialState");
                        System.err.println(agent.initialState);
                        System.err.println("Agent row: " + agent.initialState.agentRow + " col: " + agent.initialState.agentCol);
                        strategy = new Strategy.StrategyBestFirst(new Heuristic.AStar(agent.initialState));
                        break;
                    case MoveToCell:
                        strategy = new Strategy.StrategyBestFirst(new Heuristic.AStar(agent.initialState));
                        break;
                    default:
                        strategy = new Strategy.StrategyBFS();
                        break;
                }
                System.err.println("Finding solution for agent: " + agent.getNumber() + " for goal " + agent.goalStack.peek().goal);
                solution = agent.Search(strategy, agent.initialState);
            }


        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
        }


        if (solution == null) {

            System.err.println(strategy.searchStatus());
            System.err.println("Unable to solve level.");
            System.err.println(agent.initialState);
            return null;
        } else {
            System.err.println("\nSummary for " + strategy.toString());
            System.err.println("Found solution of length " + solution.size());
            System.err.println(strategy.searchStatus());
        }

//        Iterator lit = solution.listIterator();
//        System.err.println("Forward Iterations");
//        Node n = null;
//        boolean error = false;
//        int distance=0;
//        while(lit.hasNext()){
//            n = (Node)lit.next();
//            if (IsBox(n.agentRow,n.agentCol)) {
//                System.err.println("Cell not free at ");
//                System.err.println(n);
//                error =true;
//                distance = (int)CalculateMathDistance(n.agentRow,n.agentCol, agent.goalStack.peek().boxRow, agent.goalStack.peek().boxCol) + 2;
//                break;
//
//
//
//            }
//
//
//
//
//
//        }
//
//        if (error)
//        {
//
//            LinkedList<Node> halfSolution = new LinkedList<>();
//            lit = solution.listIterator();
//            boolean t =false;
//            System.err.println(agent.goalStack.peek().boxRow + " : " + agent.goalStack.peek().boxCol);
//            while(lit.hasNext()){
//                Node a = (Node)lit.next();
//                System.err.println(a.agentRow + " - " + a.agentCol);
//                if (AreNeighbours(a.agentRow,a.agentCol,agent.goalStack.peek().boxRow,agent.goalStack.peek().boxCol ))
//                    t=true;
//                if (t)
//                    halfSolution.add(a);
//
//
//            }
//
//            System.err.println("Half solution  +" + halfSolution);
//
//            System.err.println("errorrrrrrrr");
//            lit = solution.listIterator();
//            Node h = (Node)lit.next();
//            LinkedList<Node> visited = new LinkedList<>();
//            Node found = getFreeCell(h,distance,halfSolution,visited);
//            while(lit.hasNext() && found==null ){
//               Node k =  (Node)lit.next();
////               System.err.println("------------------------------------------Node tried" + k);
//               found= getFreeCell(k, distance-1,halfSolution,visited);
//               if(found!=null)
//                   break;
//            }
//
//            System.err.println(found);
//            System.err.println("second");
//            char[][] aBoxes = new char[MAX_ROW][MAX_COL];
//            char[][] aGoals = new char[MAX_ROW][MAX_COL];
//            aBoxes[n.agentRow][n.agentCol]
//                    = boxes[n.agentRow][n.agentCol];
//
//            aGoals[found.agentRow][found.agentCol]  = Character.toLowerCase(boxes[n.agentRow][n.agentCol]);
//            Goal goal = new Goal(aGoals, aBoxes);
//            goal.boxRow = n.agentRow;
//            goal.boxCol = n.agentCol;
//            goal.goalRow = found.agentRow;
//            goal.goalCol = found.agentCol;
//            goal.goal = GoalTypes.BoxOnGoal;
//
//            agent.addGoal(goal);
//            //agent.removeAllWalls();
//            solution = GetPlanFromAgent(agent);
//
//
//            System.err.println("Agent initial state before:" + agent.initialState);
//            System.err.println("Final solution: " + solution);
//        }








        return solution;
    }

    public HashMap<Client, LinkedList<Node>> GetPlansFromAgents(List<Client> agentList) {
        HashMap<Client, LinkedList<Node>> joinPlan = new HashMap<>();

        for (Client agent : agentList) {

            if(agent.goalStack.size() > 0) {
                // One agent
                CopyBoxes(agent.goalStack.peek().boxes, agent.initialState.boxes);
                CopyBoxes(agent.goalStack.peek().goals, agent.goals);

                LinkedList<Node> solution = GetPlanFromAgent(agent);
                joinPlan.put(agent, solution);
            }
            else{
                LinkedList<Node> solution = new LinkedList<>();
                solution.add(CreateNoOp(agent.currentState));
                joinPlan.put(agent,solution);
            }
        }

        return joinPlan;
    }

    public void AddNewPlanToAgent(Client cP, HashMap<Client, LinkedList<Node>> joinPlan) {

        if (cP.goalStack.size() == 0) {
            Node h = CreateNoOp(cP.currentState);
            joinPlan.get(cP).add(h);
        } else {

            System.err.println("Finished: " + cP.goalStack.peek().goal);
            if(cP.goalStack.peek().goal==GoalTypes.BoxOnGoal) {
                System.err.println("Agent: " + cP.getNumber() + " Finished: " + cP.goalStack.peek().goal);

                for (Client c : agentList) {
                    c.addWall(cP.goalStack.peek().goalRow, cP.goalStack.peek().goalCol);
                    for (Goal go : cP.goalStack) {
                        go.UpdateBoxes();
                    }
                    System.err.println("ADDING WALL: " + c.initialState);
                }
            }

            Goal g= cP.goalStack.pop();

            System.err.println("Goal fullfilled: "  + g.goals[g.goalRow][g.goalCol] + "- Type: " + g.goal  + "Box:" + g.boxRow +" :" + g.boxCol +" Goal:" + g.goalRow + ": "+ g.goalCol );
            System.err.println("Goals remaining");

            for (Goal go: cP.goalStack)
            {
                System.err.println("Goal:"  + go.goals[go.goalRow][go.goalCol] + "- Type: " + go.goal + "Box:" + go.boxRow +" :" + go.boxCol +" Goal:" + go.goalRow + ": "+ go.goalCol );
            }

            if (cP.goalStack.size() == 0) {
                AddNewPlanToAgent(cP, joinPlan);
                return;
            }

            // Get closes goal
         //   if (cP.goalStack.size()>1)
           //     cP.getBestGoal();


            System.err.println("New goal initialState: " );
            System.err.println(cP.currentState);
            cP.goalStack.peek().UpdateBoxes();
            if(cP.goalStack.peek().goal == GoalTypes.MoveToEmptyCell)
                CopyBoxes(cP.currentState.boxes,cP.goalStack.peek().boxes);
            else
                CopyBoxes(cP.goalStack.peek().boxes,cP.currentState.boxes);

            while(cP.goalStack.size() > 0 && cP.currentState.isGoalState()){
                System.err.println("popping");
                System.err.println("Agent: " + cP.getNumber() + " Finished: " + cP.goalStack.peek().goal);
                System.err.println(cP.currentState);
                cP.goalStack.pop();
                //AddNewPlanToAgent(cP, joinPlan);
            }

            if(cP.goalStack.size() == 0){
                AddNewPlanToAgent(cP, joinPlan);
                return;
            }


            System.err.println(cP.currentState);
            System.err.println("Agent: " + cP.getNumber());
            System.err.println("Agent Row: " + cP.currentState.agentRow + " Col: " + cP.currentState.agentCol);
            cP.SetInitialState(cP.currentState);

            System.err.println("Current goal: " + cP.goalStack.peek().goal);
            LinkedList<Node> solution = GetPlanFromAgent(cP);


            //check if the plan is ok


            System.err.println(solution);

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
        DivideGoals2(agentList);

        // Get plans from agents
        joinPlan = GetPlansFromAgents(agentList);


        // Check If Agents are blocked in
        ReleaseAgents();

        //PlanGenerator.FillWithNoOp(joinPlan);

        // Execute plans from agents
        while (!response.contains("sucess")) {
            HashMap<Client, Node> cmdForClients = new HashMap<>();
            List<Node> actions = new ArrayList<>();
            int count = 1;
            for (Client cP : agentList) {
                // Check if agent has satisfied all or some of his goal
                if (HaveAgentFinishedHisGoals(cP, joinPlan)) {
                    AddNewPlanToAgent(cP, joinPlan);
                }

                Node n = joinPlan.get(cP).removeFirst();
                actions.add(n);
                // This client action is not possible to apply.

                Conflict conflict = multiagent.ConflictDetector.CheckIfActionCanBeApplied(actions, this);
                if (conflict.IsConflict()) {
                    System.err.println("Conflict type: " + conflict.type);
                    System.err.println("count: " + count);
                    System.err.println("Agent: " + cP.getNumber());
                    System.err.println("Node " + n.agentRow + ":" + n.agentCol);
                    System.err.println("ParentNode " + n.parent.agentRow + ":" + n.parent.agentCol);

                    System.err.println(n.action.actionType);
                    actions.remove(n);
                    // Find New Action
                    n = ConflictHandler.HandleConflict(conflict, n, this, cmdForClients, cP, joinPlan, actions);
                }

                actions.remove(n);
                actions.add(n);

                System.err.println();
                System.err.println("Clients: " + joinPlan.keySet().size());
//                for (List<Node> l : joinPlan.values()) {
//                    System.err.println(l.size());
//                }
                cmdForClients.put(cP, n);
                count++;
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
            if(writer!=null){
            writer.write(joinedAction+"\n");
            writer.flush();}
            System.out.flush();

            try {
                response = in.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, joinedAction);
                    break;
                }

            } catch (Exception e) {

            }

        }

        System.err.println("Finished");
    }

    public boolean IsCellFree(int row, int col) {

        return !CentralPlanner.walls[row][col] && this.boxes[row][col] == 0 && !('0' <= this.agents[row][col] && this.agents[row][col] <= '9');
    }
    public boolean IsBox(int row, int col) {
        return  this.boxes[row][col] != 0;
    }


    public boolean boxAt(int row, int col) {
        return ('A' <= this.boxes[row][col] && this.boxes[row][col] <= 'Z');
    }

    public boolean AgentAt(int row, int col) {
        return ('0' <= this.agents[row][col] && this.agents[row][col] <= '9');
    }

    static public Boolean BlockedPath(LinkedList<Node> path, Node n){
        for(Node node : path){
            if(node.agentCol == n.agentCol && node.agentRow == n.agentRow || n.boxes[node.agentRow][node.agentCol] != 0)
                return true;

        }
        return false;
    }

    public void ApplyAction(HashMap<Client, Node> cmds) {

        for (Client c : cmds.keySet()) {
            Node node = cmds.get(c);
            c.UpdateCurrentState(node);
            c.nodesVisited.add(node);
            if (node.action.actionType == Command.Type.NoOp)
                continue;
            // Determine applicability of action
            int newAgentRow = node.agentRow + Command.dirToRowChange(node.action.dir1);
            int newAgentCol = node.agentCol + Command.dirToColChange(node.action.dir1);

            if (node.action.actionType == Command.Type.Move) {

                // Check if there's a wall or box on the cell to which the agent is moving
                if (IsCellFree(node.agentRow, node.agentCol)) {


                    char agent = agents[node.parent.agentRow][node.parent.agentCol];
                    agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                    agents[node.agentRow][node.agentCol] = agent;

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

            }
    }

    public static void CopyBoxes(boolean[][] boxesToCopy, boolean[][] receiver) {
        for (int i = 0; i < boxesToCopy.length; i++)
            for (int j = 0; j < boxesToCopy[i].length; j++) {
                receiver[i][j] = boxesToCopy[i][j];
            }
    }

    public Node CreateNoOp(Node node) {
        Node n = new Node(node,
                node.c);
        n.action = new Command();
        n.agentRow = node.agentRow;
        n.agentCol = node.agentCol;
        CopyBoxes(node.boxes, n.boxes);
        return n;
    }

    static public void CreateWallsAtAgentPosition(Client cp){
        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
                if(agents[i][j] != 0)
                    cp.addWall(i,j);
            }
        }
    }

    public static String PrintNode(Node node) {
        StringBuilder s = new StringBuilder();

        for (int row = 0; row < CentralPlanner.MAX_ROW; row++) {
            if (!node.c.walls[row][0]) {
                break;
            }
            for (int col = 0; col < CentralPlanner.MAX_COL; col++) {
                if (node.boxes[row][col] > 0) {
                    System.err.print(node.boxes[row][col]);
                } else if (node.c.goals[row][col] > 0) {
                    System.err.print(node.c.goals[row][col]);
                    //s.append(node.c.goals[row][col]);
                } else if (node.c.walls[row][col]) {
                    System.err.print("+");
                } else if (row == node.agentRow && col == node.agentCol) {
                    System.err.print("0");
                } else {
                    System.err.print(" ");
                }
            }
            s.append("\n");
            System.err.println("Node toString length: " + s.length());
        }
        return s.toString();
    }

    public static char[][] GetBoxesOfSpecificColor(Client agent){
        char[][] boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
                if(CentralPlanner.boxes[i][j] != 0){
                    if(colors.get(CentralPlanner.boxes[i][j]).equals(agent.color)){
                        boxes[i][j] = CentralPlanner.boxes[i][j];
                    }
                }
            }
        }

        return boxes;
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
