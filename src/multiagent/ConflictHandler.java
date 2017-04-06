package multiagent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nipe on 06-04-2017.
 */
public class ConflictHandler {
    public static Node HandleConflict(Conflict conflict,Node n, CentralPlanner centralPlanner,HashMap<Client, Node> cmdForClients, Client cP, HashMap<Client,LinkedList<Node>> joinPlan,List<Node> actions){
        Node result = null;
        LinkedList<Node> planForConflictingAgent;
        switch (conflict.type){
            case AgentsBlockEachother:
                Client conflictingAgent = conflict.conflictingAgent;
                planForConflictingAgent = joinPlan.get(conflict.conflictingAgent);
                boolean step1Succes = false;
                System.err.println("Conflict: " + conflictingAgent.color + " A: " + cP.color);
                for (int i=0; i<centralPlanner.agents.length; i++)
                    for (int j=0; j<centralPlanner.agents[i].length; j++) {
                        if(('0' <= centralPlanner.agents[i][j] && centralPlanner.agents[i][j] <= '9')){
                            System.err.println("Row: " + i + " Col: " + j + " Agent: " + centralPlanner.agents[i][j]);
                        }
                    }
                System.err.println("Current Agents: Row " + n.parent.agentRow + " Col " + n.parent.agentCol + "Agent: " + centralPlanner.agents[n.parent.agentRow][n.parent.agentCol]);
                // 1
                for(Node node : n.parent.getExpandedNodes()){

                    actions.add(node);
                    //node.action.actionType != Command.Type.NoOp &&
//                              System.err.println("Actions: " + actions.size());
                    conflict = multiagent.ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);

                    //System.err.println(node.c.color);
                    if(!node.equals(n) && !conflict.IsConflict()){

                        boolean ConflictWithPlan = false;
                        System.err.println("size: " + planForConflictingAgent.size());
                        //System.err.println("This is a plan of ConflictingAgent : \n" + planForConflictingAgent);
                        for(Node caNode : planForConflictingAgent){
                            if(node.agentRow == caNode.agentRow && node.agentCol == caNode.agentCol){
                                ConflictWithPlan = true;
                                System.err.println("check");
                                break;
                            }
                        }

                        if(!ConflictWithPlan){


                            System.err.println("Actions Type: " + node.action.actionType + " Dir1: " + node.action.dir1 + " Dir 2: " + node.action.dir2);
                            System.err.println("CP - Current Agents: Row " + node.parent.agentRow + " Col " + node.parent.agentCol + "Agent: " + centralPlanner.agents[node.parent.agentRow][node.parent.agentCol]);
                            System.err.println("CP - New Agents: Row " + node.agentRow + " Col " + node.agentCol + "Agent: " + centralPlanner.agents[node.agentRow][node.agentCol]);
                            System.err.println("CP - Conflict Agents: Row " + n.agentRow + " Col " + n.agentCol + "Agent: " + centralPlanner.agents[n.agentRow][n.agentCol]);
                            System.err.println("Check Agent is here: " + centralPlanner.agents[3][9]);
                            System.err.println("Check Agent is here: " + centralPlanner.agents[4][10]);
                                        /*
                                        cP.initialState.agentRow = node.agentRow;
                                        cP.initialState.agentCol = node.agentCol;
                                        cP.initialState.boxes = node.boxes;
                                        cP.initialState.g = node.g;
                                        */
                            n.parent = node.parent;
                            n.action = node.action;
                            n.agentRow = node.agentRow;
                            n.agentCol = node.agentCol;
                            n.g = node.g;
                            System.err.println(n);
                            joinPlan.get(cP).clear();
                            //System.err.println("Node: " + node.agentRow + node.agentCol);

/*
                                        joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS(), node));
                                        System.err.println("node in new plan");
                                        Node test = joinPlan.get(cP).getFirst();
                                        System.err.println(test);
                                        System.err.println("test parent");
                                        System.err.println(test.parent);
*/
                            step1Succes = true;
                            break;
                        }


                    }else{
                        if(!node.equals(n)) {
                            System.err.println();
                            System.err.println("Conflict parent: ");
                            System.err.println("Current Agents: Row " + node.parent.agentRow + " Col " + node.parent.agentCol + "Agent: " + centralPlanner.agents[node.parent.agentRow][node.parent.agentCol]);
                            System.err.println("Recalculate move: " + conflict.type);
                        }
                    }

                    actions.remove(node);
                }

                if(step1Succes){
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
                cP.initialState.agentRow = n.parent.agentRow;
                cP.initialState.agentCol = n.parent.agentCol;
                cP.initialState.boxes = n.parent.boxes;
                cP.initialState.g = n.parent.g;
                joinPlan.put(cP, cP.Search(new Strategy.StrategyBFS(), cP.initialState));

                cP.removeWall(n.agentRow, n.agentCol);
                if(joinPlan.get(cP).size() == 0)
                    result = centralPlanner.CreateNoOp(n.parent);
                    //n = centralPlanner.CreateNoOp(n.parent);
                else
                    result = joinPlan.get(cP).removeFirst();
                    //n = joinPlan.get(cP).removeFirst();
                actions.add(n);

                if(cmdForClients.get(conflictingAgent) != null){
                    Node noop = centralPlanner.CreateNoOp(cmdForClients.get(conflictingAgent));
                    joinPlan.get(conflictingAgent).addFirst(cmdForClients.get(conflictingAgent));
                    cmdForClients.put(conflictingAgent, noop);
                    actions.remove(cmdForClients.get(conflictingAgent));
                    actions.add(noop);
                }
                else{
                    System.err.println("Plan for conflicting agent: ");
                    //System.err.println(joinPlan.get(conflictingAgent));
                    if(joinPlan.get(conflictingAgent) != null){
                        if(joinPlan.get(conflictingAgent).size() == 0)
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(n.parent));
                        else
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(joinPlan.get(conflictingAgent).getFirst()));
                    }else{
                        joinPlan.put(conflictingAgent, new LinkedList<Node>());
                        joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(joinPlan.get(conflictingAgent).getFirst()));

                    }



                }

                break;

            default:

                System.err.println("Enter default conflict handling");
                actions.remove(n);
                boolean success = false;
                for (Node a : n.parent.getExpandedNodes()) {
                    actions.add(a);
                    conflict = ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);

                    if (!conflict.IsConflict()) {

                        joinPlan.get(cP).addFirst(n);
                        n.parent = a.parent;
                        n.action = a.action;
                        n.agentRow = a.agentRow;
                        n.agentCol = a.agentCol;
                        n.g = a.g;
                        result = a;
                        success = true;
                        break;
                    }
                    actions.remove(a);
                }

                if(success)
                    break;



                Node f = n.parent.ChildNode();
                f.action = new Command(); // Adding NoOp
                f.agentRow = n.parent.agentRow;
                f.agentCol = n.parent.agentCol;
                result = f;
                joinPlan.get(cP).addFirst(n);


                break;



        }
        return result;
    }
}
