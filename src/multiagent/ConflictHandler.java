package multiagent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
                planForConflictingAgent = joinPlan.get(conflict.conflictingAgent);
                // Check if conflicting agent has finished his goals and tell him to get the fuck away.

//                System.err.println(conflictingAgent.goalStack.size());

                planForConflictingAgent = joinPlan.get(conflict.conflictingAgent);
                if (planForConflictingAgent.size() == 0 && conflictingAgent.goalStack.size() == 0) {
                    LinkedList<Node> planForAgent = joinPlan.get(cP);
                    planForAgent.addFirst(n);
                    System.err.println("Plan for the agent");
                    System.err.println(planForAgent);
                    Goal goal = new Goal(joinPlan.get(cP));
                    goal.goal = GoalTypes.MoveToEmptyCell;
                    conflictingAgent.goalStack.push(goal);
                    System.err.println("PlanToMoveToEmptyCell");
                    System.err.println(conflictingAgent.currentState);
                    conflictingAgent.SetInitialState(conflictingAgent.currentState);
                    System.err.println(conflictingAgent.initialState);
                    System.err.println(conflictingAgent.currentState.agentRow + " : " + conflictingAgent.currentState.agentCol);
                    LinkedList<Node> moveToEmptyCell = centralPlanner.GetPlanFromAgent(conflictingAgent);
                    System.err.println(moveToEmptyCell);
                    joinPlan.put(conflictingAgent, moveToEmptyCell);

                    return centralPlanner.CreateNoOp(n.parent);
                    //return joinPlan.get(conflictingAgent).removeFirst();

                }


                boolean step1Succes = false;
                System.err.println("Conflict: " + conflictingAgent.color + " A: " + cP.color);
                for (int i = 0; i < centralPlanner.agents.length; i++)
                    for (int j = 0; j < centralPlanner.agents[i].length; j++) {
                        if (('0' <= centralPlanner.agents[i][j] && centralPlanner.agents[i][j] <= '9')) {
                            System.err.println("Row: " + i + " Col: " + j + " Agent: " + centralPlanner.agents[i][j]);
                        }
                    }
                System.err.println("Current Agents: Row " + n.parent.agentRow + " Col " + n.parent.agentCol + "Agent: " + centralPlanner.agents[n.parent.agentRow][n.parent.agentCol]);
                // 1
                for (Node node : n.parent.getExpandedNodes()) {

                    actions.add(node);
                    conflict = multiagent.ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
                    if (!node.equals(n) && !conflict.IsConflict()) {
                        boolean ConflictWithPlan = false;
                        System.err.println("size: " + planForConflictingAgent.size());
                        //System.err.println("This is a plan of ConflictingAgent : \n" + planForConflictingAgent);
                        for (Node caNode : planForConflictingAgent) {
                            if (node.agentRow == caNode.agentRow && node.agentCol == caNode.agentCol) {
                                ConflictWithPlan = true;
                                System.err.println("check");
                                break;
                            }
                        }

                        if (!ConflictWithPlan) {

                            result = node.Copy();

                            joinPlan.get(cP).clear();
                            cP.SetInitialState(result);
                            joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS(), cP.initialState));
                            //System.err.println("Node: " + node.agentRow + node.agentCol);
                            step1Succes = true;
                            break;
                        }


                    }

                    actions.remove(node);
                }

                if (step1Succes) {
                    System.err.println("Step 1 was a success");
                    //System.err.println("Conflicting agent: " + conflictingAgent.color);
                    //System.err.println("Path of conflicting agent: ");
                    //System.err.println(planForConflictingAgent);
                    break;
                }


                // 2
                System.err.println("Step 2");
                Goal moveToEmptyCell = new Goal(planForConflictingAgent);
                //System.err.println("Plan for agent: ");
                //System.err.println(planForConflictingAgent);

                moveToEmptyCell.goal = GoalTypes.MoveToEmptyCell;
                cP.addGoal(moveToEmptyCell);
                cP.addWall(n.agentRow, n.agentCol);
                cP.SetInitialState(n.parent);

                System.err.println("Check initial state");
                System.err.println(cP.initialState);
                LinkedList<Node> solution = cP.Search(new Strategy.StrategyBFS(), cP.initialState);
                System.err.println("Solution");
                System.err.println(solution);


                joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS(), cP.initialState));

                cP.removeWall(n.agentRow, n.agentCol);
                System.err.println("Plan of Agent: " + joinPlan.get(cP).size());
                System.err.println("Plan of conflicting Agent: " + planForConflictingAgent.size());

                System.err.println(joinPlan.get(cP).getLast().action.actionType);


                for (int i = joinPlan.get(cP).size(); i < planForConflictingAgent.size(); i++) {

                    joinPlan.get(cP).addLast(centralPlanner.CreateNoOp(joinPlan.get(cP).getLast()));
                }
                System.err.println("After");
                System.err.println(joinPlan.get(cP).getLast().action.actionType);

                if (joinPlan.get(cP).size() == 0)
                    result = centralPlanner.CreateNoOp(n.parent);
                    //n = centralPlanner.CreateNoOp(n.parent);
                else
                    result = joinPlan.get(cP).removeFirst();

                actions.add(result);

                if (cmdForClients.get(conflictingAgent) != null) {
                    Node noop = centralPlanner.CreateNoOp(cmdForClients.get(conflictingAgent));
                    joinPlan.get(conflictingAgent).addFirst(cmdForClients.get(conflictingAgent));
                    cmdForClients.put(conflictingAgent, noop);
                    actions.remove(cmdForClients.get(conflictingAgent));
                    actions.add(noop);
                } else {
                    System.err.println("Plan for conflicting agent: ");
                    //System.err.println(joinPlan.get(conflictingAgent));
                    if (joinPlan.get(conflictingAgent) != null) {
                        if (joinPlan.get(conflictingAgent).size() == 0)
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(conflictingAgent.currentState));
                        else
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(joinPlan.get(conflictingAgent).getFirst()));
                    } else {
                        joinPlan.put(conflictingAgent, new LinkedList<Node>());
                        joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(conflictingAgent.currentState));

                    }


                }

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
                            joinPlan.get(trappedAgent).addFirst(centralPlanner.CreateNoOp(joinPlan.get(trappedAgent).getFirst()));
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
                System.err.println("Enter push conflict handling");
                Conflict test;
                boolean[][] tmpWalls;
                tmpWalls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                centralPlanner.CopyBoxes(cP.walls, tmpWalls);
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
                result = solution.removeFirst();
                if (result.action.actionType == Command.Type.Push) {
                    System.err.println("Adding Wall: " + (result.agentRow + Command.dirToRowChange(result.action.dir2)) + ":" + (result.agentCol + Command.dirToColChange(result.action.dir2)));
//                    cP.addWall(n.agentRow + Command.dirToRowChange(n.action.dir2), n.agentCol + Command.dirToColChange(n.action.dir2));
                } else {
                    System.err.println("Adding Wall: " + result.agentRow + ":" + result.agentCol);
//                    cP.addWall(n.agentRow,n.agentCol);
                }
                joinPlan.put(cP, solution);
                break;
            /*
            default:
                System.err.println("Enter default conflict handling");
                actions.remove(n);

                System.err.println("Current State");
                System.err.println(cP.currentState);
                cP.SetInitialState(cP.currentState);
                tmpWalls = new boolean[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
                centralPlanner.CopyBoxes(cP.walls,tmpWalls);
                cP.addWall(n.agentRow,n.agentCol);

                solution = centralPlanner.GetPlanFromAgent(cP);
                cP.removeWall(n.agentRow,n.agentCol);
                result = solution.removeFirst();
                joinPlan.put(cP,solution);
                break;
                */

        }

        return result;

    }
}
