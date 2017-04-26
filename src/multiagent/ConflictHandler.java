package multiagent;

import java.util.*;

/**
 * Created by nipe on 06-04-2017.
 */
public class ConflictHandler {
    public static Node HandleConflict(Conflict conflict, Node n, CentralPlanner centralPlanner, HashMap<Client, Node> cmdForClients, Client cP, HashMap<Client, LinkedList<Node>> joinPlan, List<Node> actions) {
        Node result = null;
        LinkedList<Node> planForConflictingAgent;
        switch (conflict.type) {
            case AgentsBlockEachother:

                Client conflictingAgent = conflict.conflictingAgent;
                System.err.println("Plan sizes: "  );
                System.err.println("agent: " + conflictingAgent.getNumber() + " goals: " + conflictingAgent.goalStack.size() + " conflict: " + joinPlan.get(conflict.conflictingAgent).size() );
                System.err.println("agent: " + cP.getNumber() + " goals: " + cP.goalStack.size() +  " my: " + joinPlan.get(cP).size());
                planForConflictingAgent = joinPlan.get(conflict.conflictingAgent);
                // Check if conflicting agent has finished his goals and tell him to get the fuck away.
                planForConflictingAgent = joinPlan.get(conflict.conflictingAgent);
                if (planForConflictingAgent.size() == 0 && conflictingAgent.goalStack.size() == 0) {
                    LinkedList<Node> planForAgent = joinPlan.get(cP);
                    planForAgent.addFirst(n);
                    joinPlan.put(cP,planForAgent);
                    planForAgent.addFirst(cP.currentState);
                    System.err.println("Plan for the agent");
                    System.err.println(planForAgent);
                    Goal goal = new Goal(planForAgent);
                    goal.goal = GoalTypes.MoveToEmptyCell;

                    conflictingAgent.goalStack.push(goal);

                    System.err.println("conflict currentstate");
                    System.err.println(conflictingAgent.currentState);
                    conflictingAgent.SetInitialState(conflictingAgent.currentState);
                    System.err.println("conflict initial");
                    System.err.println(conflictingAgent.initialState);
                    //System.err.println(conflictingAgent.currentState.agentRow + " : " + conflictingAgent.currentState.agentCol);
                    Conflict test;
                    boolean[][] wallsForC = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    CentralPlanner.CopyBoxes(conflictingAgent.walls,wallsForC);

                    for (Node children : conflictingAgent.initialState.getExpandedNodes()) {
                        actions.add(children);
                        test = ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
                        if (test.IsConflict()) {
                            if (children.action.actionType == Command.Type.Push) {
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                                conflictingAgent.addWall(children.agentRow + Command.dirToRowChange(children.action.dir2), children.agentCol + Command.dirToColChange(children.action.dir2));
                            } else {
//                            System.err.println("Adding Wall: " + n.agentRow + ":" + n.agentCol);
                                conflictingAgent.addWall(children.agentRow, children.agentCol);
                            }
                        }
                        actions.remove(children);
                    }
                    LinkedList<Node> moveToEmptyCell = centralPlanner.GetPlanFromAgent(conflictingAgent);
                    CentralPlanner.CopyBoxes(wallsForC, conflictingAgent.walls);

                    System.err.println("PlanToMoveToEmptyCell");
                    System.err.println(moveToEmptyCell);
                    joinPlan.put(conflictingAgent, moveToEmptyCell);

                    if(cmdForClients.containsKey(conflictingAgent))
                        if(joinPlan.get(conflictingAgent).size() > 0)
                            cmdForClients.put(conflictingAgent,joinPlan.get(conflictingAgent).removeFirst());

                    planForAgent.remove(cP.currentState);
                    return centralPlanner.CreateNoOp(cP.currentState);
                    //return joinPlan.get(conflictingAgent).removeFirst();
                }
                /*
                LinkedList<Node> myPlan = joinPlan.get(cP);
                // if I have finished my goal, I should get the fuck away
                if (myPlan.size() == 0 && cP.goalStack.size() == 0) {
                    LinkedList<Node> planForAgent = joinPlan.get(conflictingAgent);
                    if(cmdForClients.containsKey(conflictingAgent)){
                        planForAgent.add(cmdForClients.get(conflictingAgent));
                        cmdForClients.put(conflictingAgent,centralPlanner.CreateNoOp(conflictingAgent.currentState));
                    }

                    System.err.println("Plan for the agent");
                    System.err.println(planForAgent);
                    Goal goal = new Goal(planForAgent);
                    goal.goal = GoalTypes.MoveToEmptyCell;
                    cP.goalStack.push(goal);
                    System.err.println("PlanToMoveToEmptyCell");
                    System.err.println(cP.currentState);
                    cP.SetInitialState(cP.currentState);
                    System.err.println(cP.initialState);
                    System.err.println(cP.currentState.agentRow + " : " + cP.currentState.agentCol);
                    LinkedList<Node> moveToEmptyCell = centralPlanner.GetPlanFromAgent(cP);
                    System.err.println(moveToEmptyCell);
                    joinPlan.put(cP, moveToEmptyCell);


                    return joinPlan.get(cP).removeFirst();
                    //return joinPlan.get(conflictingAgent).removeFirst();
                }
*/


                Conflict test;
                boolean[][] tmpWalls;
                tmpWalls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                centralPlanner.CopyBoxes(cP.walls, tmpWalls);
                System.err.println("Agent: " + cP.getNumber());
                cP.goalStack.peek().UpdateBoxes();
                CentralPlanner.CopyBoxes(cP.goalStack.peek().boxes, cP.currentState.boxes);
                // Add walls on the cells which is conflicting.
                cP.SetInitialState(cP.currentState);
                for (Node children : cP.initialState.getExpandedNodes()) {
                    actions.add(children);
                    test = ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
                    if (test.IsConflict()) {
                        if (children.action.actionType == Command.Type.Push) {
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                            cP.addWall(children.agentRow + Command.dirToRowChange(children.action.dir2), children.agentCol + Command.dirToColChange(children.action.dir2));
                        } else {
//                            System.err.println("Adding Wall: " + n.agentRow + ":" + n.agentCol);
                            cP.addWall(children.agentRow, children.agentCol);
                        }
                    }
                    actions.remove(children);
                }

                cP.SetInitialState(cP.currentState);
                System.err.println(cP.initialState);
                LinkedList<Node> solution;
                if(cP.walls[cP.goalStack.peek().goalRow][cP.goalStack.peek().goalCol] == true)
                    solution = null;
                else
                    solution = centralPlanner.GetPlanFromAgent(cP);

                if(solution == null){
                    System.err.println("It was not possible to calculate a new path, Im boxed in");
                    // At this point it is not possible to calculate a new path.
                    // Check if the agent can wait it out ;-D

                    if(joinPlan.get(conflictingAgent).size() > 0){

                        Node nCA = joinPlan.get(conflictingAgent).getFirst();
                        for(Node node : joinPlan.get(cP)) {
                            if(node.action.actionType == nCA.action.actionType &&
                                    node.agentRow == nCA.agentRow &&
                                    node.agentCol == nCA.agentCol &&
                                    node.action.dir1 == nCA.action.dir1){
                                    centralPlanner.CopyBoxes(tmpWalls, cP.walls);
                                    joinPlan.put(cP, centralPlanner.GetPlanFromAgent(cP));
                                    return centralPlanner.CreateNoOp(cP.currentState);
                            }

                        }
                    }
                    System.err.println("The two agents are not going in the same direction");

                    // Tell the conflicting agent to get the fuck away.
                    result = cP.currentState.Copy();
                    result.action = new Command();
                    centralPlanner.CopyBoxes(tmpWalls, cP.walls);
                    cP.SetInitialState(cP.currentState);
                    solution = centralPlanner.GetPlanFromAgent(cP);

                    System.err.println("Conflicting agent should avoid the following path: ");
                    System.err.println(solution);
                    Goal freepath = new Goal();
                    freepath.path = new LinkedList<>();
                    freepath.path.addAll(solution);
                    freepath.goal = GoalTypes.MoveToEmptyCell;
                    // Fix this later with boxes of multiple colors and etc.
                    freepath.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                    CentralPlanner.CopyBoxes(conflictingAgent.currentState.boxes,freepath.boxes);

                    conflictingAgent.addGoal(freepath);
                    conflictingAgent.addWall(cP.currentState.agentRow,cP.currentState.agentCol);
                    conflictingAgent.SetInitialState(conflictingAgent.currentState);

                    System.err.println("ConflictingAgent: Start State");
                    System.err.println(conflictingAgent.initialState);
                    LinkedList<Node> replan = centralPlanner.GetPlanFromAgent(conflictingAgent);
                    conflictingAgent.removeWall(cP.currentState.agentRow,cP.currentState.agentCol);

                    joinPlan.put(conflictingAgent,replan);
                    System.err.println("Plan Found");
                    System.err.println(replan);

                    if(replan.size() != 0)
                    {


                    for (int i = 0; i < (solution.size() - replan.size()) + 4; i++) {
                        joinPlan.get(conflictingAgent).addLast(centralPlanner.CreateNoOp(joinPlan.get(conflictingAgent).getLast()));
                    }
                    if(cmdForClients.containsKey(conflictingAgent)){
                        cmdForClients.put(conflictingAgent,joinPlan.get(conflictingAgent).removeFirst());
                    }

                    if(conflictingAgent.goalStack.size() <= 1){
                        joinPlan.put(cP,solution);
                        return result;
                    }
                    // Find empty cell which is not blocking with conflicting agent path to his 2nd goal

                    // Conflicting Agents 2nd path
                    System.err.println("LastNode: " + joinPlan.get(conflictingAgent).getLast());
                    conflictingAgent.SetInitialState(joinPlan.get(conflictingAgent).getLast());
                    // First goal of conflictingAgent
                    Goal originalGoal = conflictingAgent.goalStack.pop();
                    CentralPlanner.CopyBoxes(joinPlan.get(conflictingAgent).getLast().boxes,conflictingAgent.goalStack.peek().boxes);
                    CentralPlanner.CopyBoxes(conflictingAgent.goalStack.peek().goals,conflictingAgent.goals);
                    System.err.println("InitialNode: " +  conflictingAgent.initialState);
                    // Check if path of conflicting agents 2nd goal will conflict with the end position of non-conflicting agent.
                    System.err.println("Calculating path for conflicting Agents 2nd goal");
                    System.err.println("Searching from: ");
                    System.err.println(conflictingAgent.initialState);
                    conflictingAgent.addWall(solution.getLast().agentRow,solution.getLast().agentCol);
                    conflictingAgent.addWall(cP.goalStack.peek().goalRow, cP.goalStack.peek().goalCol);
                    LinkedList<Node> cAgentGoal2 = centralPlanner.GetPlanFromAgent(conflictingAgent);
                    conflictingAgent.removeWall(solution.getLast().agentRow,solution.getLast().agentCol);
                    conflictingAgent.removeWall(cP.goalStack.peek().goalRow, cP.goalStack.peek().goalCol);
                    System.err.println("Solution lastNode: ");
                    System.err.println(solution.getLast());

                    if(cAgentGoal2 == null)
                        cAgentGoal2 = centralPlanner.GetPlanFromAgent(conflictingAgent);

                        System.err.println("Goal 2 solution");
                        System.err.println(cAgentGoal2);

                    if(CentralPlanner.BlockedPath(cAgentGoal2,solution.getLast())){
                        System.err.println("Agent is blocking for conflicting agent 2nd goal");
                        Goal move = new Goal(cAgentGoal2);
                        move.goal = GoalTypes.MoveToEmptyCell;
                        cP.addWall(joinPlan.get(conflictingAgent).getLast().agentRow,joinPlan.get(conflictingAgent).getLast().agentCol);
                        CentralPlanner.CopyBoxes(solution.getLast().boxes,move.boxes);
                        cP.goalStack.push(move);
                        cP.SetInitialState(solution.getLast());

                        System.err.println("New initialstate for agent");
                        System.err.println(cP.initialState);

                        System.err.println("Find empty cell which is not blocking for conflicting agents 2nd goal");
                        Node endNode = centralPlanner.GetPlanFromAgent(cP).getLast();
                        System.err.println(endNode);
                        cP.goalStack.peek().UpdateBoxes();
                        char[][] goalsForGoal = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                        for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
                            for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
                                if(endNode.boxes[i][j] != 0){
                                    System.err.println("Move box to row: " + i + " col: " + j);
                                    goalsForGoal[i][j] = Character.toLowerCase(endNode.boxes[i][j]);
                                    break;
                                }

                            }
                        }
                        cP.removeWall(joinPlan.get(conflictingAgent).getFirst().agentRow,joinPlan.get(conflictingAgent).getFirst().agentCol);
                        cP.goalStack.pop();
                        Goal positionForAgent = new Goal(goalsForGoal, cP.goalStack.peek().boxes);
                        positionForAgent.goal = GoalTypes.BoxOnGoal;
                        positionForAgent.UpdateBoxes();
                        cP.goalStack.push(positionForAgent);
                        cP.SetInitialState(cP.currentState);
                        solution = centralPlanner.GetPlanFromAgent(cP);
                        for (int i = 0; i < 3; i++) {
                            solution.addLast(centralPlanner.CreateNoOp(solution.getLast()));
                        }
                        result = centralPlanner.CreateNoOp(cP.currentState);

                        System.err.println("Agents new plan to avoid collision with agent2 2nd goal");
                        System.err.println(solution);

                    }
                    conflictingAgent.SetInitialState(conflictingAgent.currentState);
                    conflictingAgent.goalStack.push(originalGoal);
                    conflictingAgent.goalStack.peek().UpdateBoxes();
                    CentralPlanner.CopyBoxes(conflictingAgent.goalStack.peek().boxes,conflictingAgent.initialState.boxes);
                    }
                }else{

                    int countVisited = 0;
                    for(Node node : cP.nodesVisited){
                        if(node.equals(solution.getFirst())){
                            countVisited++;
                            if(countVisited > 2){
                                Random r = new Random();
                                Node randomNode = cP.currentState.getExpandedNodes().get(r.nextInt(cP.currentState.getExpandedNodes().size() - 1));
                                cP.SetInitialState(randomNode);
                                joinPlan.put(cP,centralPlanner.GetPlanFromAgent(cP));
                                centralPlanner.CopyBoxes(tmpWalls, cP.walls);

                                return randomNode;
                            }
                        }

                    }

                    result = solution.removeFirst();
                }

                centralPlanner.CopyBoxes(tmpWalls, cP.walls);
                joinPlan.put(cP, solution);

                break;

            case TrappedAgent:
                Client trappedAgent = conflict.conflictingAgent;
                LinkedList<Node> plan = centralPlanner.GetPlanFromAgent(trappedAgent);
                System.err.println("Trapped Agent Plan");
                System.err.println(plan);
                int row = -1;
                int col = -1;

                for (Node node : plan) {
                    if (centralPlanner.boxes[node.agentRow][node.agentCol] != 0 && !CentralPlanner.colors.get(centralPlanner.boxes[node.agentRow][node.agentCol]).equals(trappedAgent.color)) {
                        row = node.agentRow;
                        col = node.agentCol;
                    }
                }

                for (Client client : centralPlanner.agentList) {
                    if (client.color.equals(CentralPlanner.colors.get(centralPlanner.boxes[row][col]))) {
                        Goal boxedAgent = new Goal(plan);
                        boxedAgent.goal = GoalTypes.FreeAgent;

                        client.goalStack.push(boxedAgent);
                        client.SetInitialState(client.currentState);
                        client.initialState.boxes[row][col] = centralPlanner.boxes[row][col];
                        client.addWall(trappedAgent.currentState.agentRow, trappedAgent.currentState.agentCol);
                        System.err.println("Savior Initial State: ");
                        System.err.println(client.initialState);
                        joinPlan.put(client, centralPlanner.GetPlanFromAgent(client));
                        client.removeWall(trappedAgent.currentState.agentRow, trappedAgent.currentState.agentCol);

                        // If the client could not find a solution for freeing the agent.

                        int count = 1;
                        for (Node node : joinPlan.get(client)) {
                            if (node.boxes[row][col] == 0) {
                                break;
                            }
                            count++;
                        }

                        int count2 = 2;
                        for (Node node : joinPlan.get(trappedAgent)) {
                            if (node.agentRow == row && node.agentCol == col) {
                                break;
                            }
                            count2++;
                        }

                        for (int i = 0; i < count; i++) {
                            joinPlan.get(trappedAgent).addFirst(centralPlanner.CreateNoOp(trappedAgent.currentState));
                        }

                        for (int i = 0; i < count2; i++) {
                            joinPlan.get(client).addLast(centralPlanner.CreateNoOp(joinPlan.get(client).getLast()));
                        }
                        System.err.println("Savior Plan: ");

                        System.err.println(centralPlanner.GetPlanFromAgent(client));
                    }
                }

                break;
/*
            case Pull:
                System.err.println("Enter pull conflict handling");
                Conflict test;
                boolean[][] tmpWalls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                centralPlanner.CopyBoxes(cP.walls,tmpWalls);
                cP.goalStack.peek().UpdateBoxes();
                CentralPlanner.CopyBoxes(cP.goalStack.peek().boxes, cP.currentState.boxes);
                cP.SetInitialState(cP.currentState);
                for(Node children : cP.initialState.getExpandedNodes()){
                    actions.add(children);
                    test = ConflictDetector.CheckIfActionCanBeApplied(actions,centralPlanner);
                    if(test.IsConflict()){
                        if(children.action.actionType == Command.Type.Push){
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                            cP.addWall(n.agentRow + Command.dirToRowChange(n.action.dir2), n.agentCol + Command.dirToColChange(n.action.dir2));
                        }else{
//                            System.err.println("Adding Wall: " + n.agentRow + ":" + n.agentCol);
                            cP.addWall(n.agentRow,n.agentCol);
                        }
                    }
                    actions.remove(children);
                }




                solution = centralPlanner.GetPlanFromAgent(cP);
                centralPlanner.CopyBoxes(tmpWalls,cP.walls);
                result = solution.removeFirst();
                joinPlan.put(cP,solution);


/*
                int wallRow = n.parent.agentRow + 1;
                int wallCol = n.parent.agentCol;
                if(!ConflictDetector.IsCellFree(wallRow, wallCol, conflict.agents, conflict.boxes) && Character.toLowerCase(CentralPlanner.boxes[wallRow][wallCol]) != CentralPlanner.goals[n.c.goalStack.peek().goalRow][n.c.goalStack.peek().goalCol])
                    cP.addWall(wallRow,wallCol);
                wallRow = n.parent.agentRow;
                wallCol = n.parent.agentCol + 1;
                if(!ConflictDetector.IsCellFree(wallRow, wallCol, conflict.agents, conflict.boxes) && Character.toLowerCase(CentralPlanner.boxes[wallRow][wallCol]) != CentralPlanner.goals[n.c.goalStack.peek().goalRow][n.c.goalStack.peek().goalCol])
                    cP.addWall(wallRow,wallCol);
                wallRow = n.parent.agentRow - 1;
                wallCol = n.parent.agentCol;
                if(!ConflictDetector.IsCellFree(wallRow, wallCol, conflict.agents, conflict.boxes) && Character.toLowerCase(CentralPlanner.boxes[wallRow][wallCol]) != CentralPlanner.goals[n.c.goalStack.peek().goalRow][n.c.goalStack.peek().goalCol])
                    cP.addWall(wallRow,wallCol);
                wallRow = n.parent.agentRow;
                wallCol = n.parent.agentCol - 1;
                if(!ConflictDetector.IsCellFree(wallRow, wallCol, conflict.agents, conflict.boxes) && Character.toLowerCase(CentralPlanner.boxes[wallRow][wallCol]) != CentralPlanner.goals[n.c.goalStack.peek().goalRow][n.c.goalStack.peek().goalCol])
                    cP.addWall(wallRow,wallCol);



                actions.add(result);
                Conflict conflictT = ConflictDetector.CheckIfActionCanBeApplied(actions,centralPlanner);
                if(conflictT.IsConflict())
                {
                    solution.addFirst(result);
                    solution.addFirst(centralPlanner.CreateNoOp(result.parent));
                }
                result = solution.removeFirst();

                break;
              */

            case Pull:
            case Push:
            default:
                System.err.println("Default conflict handling");
                System.err.println("CurrentState: ");
                System.err.println(cP.currentState);
                cP.goalStack.peek().UpdateBoxes();
                CentralPlanner.CopyBoxes(cP.goalStack.peek().boxes,cP.currentState.boxes);
                System.err.println("Agent: " + CentralPlanner.agents[cP.currentState.agentRow][cP.currentState.agentCol]);
                tmpWalls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                centralPlanner.CopyBoxes(cP.walls, tmpWalls);
                System.err.println("Agent: " + cP.getNumber());
                cP.goalStack.peek().UpdateBoxes();
                CentralPlanner.CopyBoxes(cP.goalStack.peek().boxes, cP.currentState.boxes);


                cP.SetInitialState(cP.currentState);
                for (Node children : cP.initialState.getExpandedNodes()) {
                    actions.add(children);
                    test = ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
                    if (test.IsConflict()) {
                        if (children.action.actionType == Command.Type.Push) {
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                            cP.addWall(children.agentRow + Command.dirToRowChange(children.action.dir2), children.agentCol + Command.dirToColChange(children.action.dir2));
                        } else {
//                            System.err.println("Adding Wall: " + n.agentRow + ":" + n.agentCol);
                            cP.addWall(children.agentRow, children.agentCol);
                        }
                    }
                    actions.remove(children);
                }


                solution = centralPlanner.GetPlanFromAgent(cP);
                centralPlanner.CopyBoxes(tmpWalls, cP.walls);

                if(solution == null){

                    for (Node children : cP.initialState.getExpandedNodes()) {
                        actions.add(children);
                        test = ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
                        if (test.IsConflict()) {
                            if(test.type == ConflictTypes.AgentsBlockEachother){

                                System.err.println("inside here");
                                cP.SetInitialState(children);
                                joinPlan.put(cP,centralPlanner.GetPlanFromAgent(cP));

                                return ConflictHandler.HandleConflict(test,children,centralPlanner,cmdForClients,cP,joinPlan,actions);

                            }
                            if (children.action.actionType == Command.Type.Push) {
//                            System.err.println("Adding Wall: " + (n.agentRow + Command.dirToRowChange(n.action.dir2)) + ":" + (n.agentCol + Command.dirToColChange(n.action.dir2)));
                               // cP.addWall(children.agentRow + Command.dirToRowChange(children.action.dir2), children.agentCol + Command.dirToColChange(children.action.dir2));
                            } else {
//                            System.err.println("Adding Wall: " + n.agentRow + ":" + n.agentCol);
                               // cP.addWall(children.agentRow, children.agentCol);
                            }
                        }
                        actions.remove(children);
                    }

                    Conflict trapped = new Conflict(ConflictTypes.TrappedAgent, cP);
                    solution = centralPlanner.GetPlanFromAgent(cP);
                    joinPlan.put(cP,solution);
                    System.err.println(cP.goalStack.peek().goal);
                    System.err.println("Trapped Agent Plan");
                    System.err.println(solution);
                    ConflictHandler.HandleConflict(trapped,null,centralPlanner,null,null,joinPlan,null);

                }
                result = solution.removeFirst();
                joinPlan.put(cP, solution);
                break;
        }

        return result;

    }
}
