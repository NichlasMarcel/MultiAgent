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
                    conflict = multiagent.ConflictDetector.CheckIfActionCanBeApplied(actions, centralPlanner);
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

                            result = node;

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


                for(int i = joinPlan.get(cP).size(); i < planForConflictingAgent.size(); i++){

                    joinPlan.get(cP).addLast(centralPlanner.CreateNoOp(joinPlan.get(cP).getLast()));
                }
                System.err.println("After");
                System.err.println(joinPlan.get(cP).getLast().action.actionType);

                if(joinPlan.get(cP).size() == 0)
                    result = centralPlanner.CreateNoOp(n.parent);
                    //n = centralPlanner.CreateNoOp(n.parent);
                else
                    result = joinPlan.get(cP).removeFirst();

                actions.add(result);

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
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(conflictingAgent.currentState));
                        else
                            joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(joinPlan.get(conflictingAgent).getFirst()));
                    }else{
                        joinPlan.put(conflictingAgent, new LinkedList<Node>());
                        joinPlan.get(conflictingAgent).addFirst(centralPlanner.CreateNoOp(conflictingAgent.currentState));

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
                        Node copy = new Node(a.parent,a.c);
                        copy.agentRow = a.agentRow;
                        copy.agentCol = a.agentCol;
                        copy.action = a.action;
                        copy.g = a.g;
                        centralPlanner.CopyBoxes(a.boxes, copy.boxes);
                        result = a;
                        success = true;
                        System.err.println("Check Initial State");
                        System.err.println(cP.initialState);

                        cP.SetInitialState(copy);
                        System.err.println(cP.initialState);
                        joinPlan.put(cP, centralPlanner.GetPlanFromAgent(cP));
                        break;
                    }
                    actions.remove(a);
                }

                if(!success){
                    System.err.println("Default conflict handler: Could not find a solution");
                }

                break;
        }
        return result;
    }
}
