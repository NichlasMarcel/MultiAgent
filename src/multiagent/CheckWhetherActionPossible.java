package multiagent;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Nichlas on 22-03-2017.
 */
public class CheckWhetherActionPossible {

    public static Boolean IncludeNegativeEffect = false;

    public static boolean IsCellFree(int row, int col,char[][] agents, char[][] boxes){
        //System.err.println("Box: " + boxes[row][col]);
        //System.err.println("Agent: " + agents[row][col]);
        //System.err.println("Walls: " + CentralPlanner.walls[row][col]);


        return !CentralPlanner.walls[row][col] && boxes[row][col] == 0 && !('0' <= agents[row][col] && agents[row][col] <= '9');
    }

    public static boolean boxAt(int row, int col, char[][] boxes) {
        return ('A' <= boxes[row][col] && boxes[row][col] <= 'Z');
    }


    public static Boolean CheckIfActionCanBeApplied(List<Node> nodes, CentralPlanner cp){
        char[][] agents = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        char[][] boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];

        for (int i=0; i<agents.length; i++)
            for (int j=0; j<agents[i].length; j++)
            {
                agents[i][j] = cp.agents[i][j];
                boxes[i][j] =  cp.boxes[i][j];
            }



        /*for (int i = 0; i < agents.length; i++){
            for (int j = 0; j < agents[i].length; j++ ){
                System.err.println(agents[i][j]);
            }
        }*/
        //System.err.println("-------------------------------------------------");
        for (Node node : nodes) {
            //System.err.println("AT: " + node.action.actionType);

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
                if (IsCellFree(node.agentRow, node.agentCol,agents,boxes)) {
                    // System.err.println("Row: " + node.agentRow);
                    //System.err.println("Col: " + node.agentCol);


                    char agent = agents[node.parent.agentRow][node.parent.agentCol];
                    //System.err.println("AgentAt: " + agents[node.agentRow][node.agentCol]);
                    //System.err.println("AgentAt: " + agents[node.parent.agentRow][node.parent.agentCol]);
                    if(IncludeNegativeEffect)
                        agents[node.parent.agentRow][node.parent.agentCol] = ' ';

                    agents[node.agentRow][node.agentCol] = agent;
                    //System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);

                    //System.err.println("AGE " + agent);
                    //System.err.println("NRow: " + newAgentRow);
                    //System.err.println("NCol: " + newAgentCol);
                }
                else
                    return false;
                //else
                //System.err.println(this);
            } else if (node.action.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (boxAt(node.agentRow, node.agentCol,boxes)) {
                    int newBoxRow = node.agentRow + Command.dirToRowChange(node.action.dir2);
                    int newBoxCol = node.agentCol + Command.dirToColChange(node.action.dir2);
                    // .. and that new cell of box is free
                    if (IsCellFree(newBoxRow, newBoxCol,agents,boxes)) {
                        char agent = agents[node.parent.agentRow][node.parent.agentCol];
                        char box = boxes[node.agentRow][node.agentCol];

                        agents[node.agentRow][node.agentCol] = agent;
                        boxes[newBoxRow][newBoxCol] = box;
                        if(IncludeNegativeEffect)
                            agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                        if(IncludeNegativeEffect)
                            boxes[node.agentRow][node.agentCol] = 0;

                        //System.err.println("agent: " + agents[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);
                        //System.err.println("box: " + boxes[newBoxRow][newBoxCol] + " Row: " + newBoxRow + " Col: " + newBoxCol);
                        //System.err.println("Oldbox: " + boxes[node.agentRow][node.agentCol] + " Row: " + node.agentRow + " Col: " + node.agentCol);
                        boxes[newBoxRow][newBoxCol] = box;


                    }else
                        return false;
                }else
                    return false;
            }
            //
            // Check this code;
            else if (node.action.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (IsCellFree(node.agentRow, node.agentCol,agents,boxes)) {
                    int boxRow = node.parent.agentRow + Command.dirToRowChange(node.action.dir2);
                    int boxCol = node.parent.agentCol + Command.dirToColChange(node.action.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (boxAt(boxRow, boxCol,boxes)) {
                        char agent = agents[node.parent.agentRow][node.parent.agentCol];
                        if(IncludeNegativeEffect)
                            agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                        agents[node.agentRow][node.agentCol] = agent;

                        char box = boxes[boxRow][boxCol];
                        //boxes[boxRow][boxCol] = 0;
                        boxes[node.parent.agentRow][node.parent.agentCol] = box;
                    }else
                        return false;
                }else
                    return false;
                //else if (node.action.actionType == Command.Type.NoOp)
            }
        }

        return true;

    }
}
