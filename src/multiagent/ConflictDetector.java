package multiagent;

import com.sun.org.apache.xml.internal.utils.SerializableLocatorImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Nichlas on 22-03-2017.
 */
public class ConflictDetector {

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



    public static Conflict CheckIfActionCanBeApplied(List<Node> nodes, CentralPlanner cp){
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

            if (node.action.actionType == Command.Type.Move) {
                //if(node.parent.agentRow == 1 && node.parent.agentCol == 13){
                //    System.err.println("Agent found! " + agents[node.agentRow][node.agentCol]);
                //}
                // Check if there's a wall or box on the cell to which the agent is moving

                int row = node.agentRow;
                int col = node.agentCol;
                if(CentralPlanner.walls[row][col])
                    return new Conflict(ConflictTypes.Wall);

                if(('0' <= agents[row][col] && agents[row][col] <= '9')){
                    System.err.println("ConflictDetector: " + node.agentRow + "/" + node.agentCol);
                    return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Character.getNumericValue(agents[row][col])));
                }

                if(boxes[row][col] != 0){
                    int c_row = node.parent.agentRow;
                    int c_col = node.parent.agentCol;

                    if(row+1 <= CentralPlanner.MAX_ROW &&!(c_row == row+1 && c_col == col) && ('0' <= agents[row+1][col] && agents[row+1][col] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));}
                    if(row-1 > 0 && !(c_row == row-1 && c_col == col) && ('0' <= agents[row-1][col] && agents[row-1][col] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));}
                    if(col+1 <= CentralPlanner.MAX_COL && !(c_row == row && c_col == col+1) && ('0' <= agents[row][col+1] && agents[row][col+1] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                    }
                    if(col-1 > 0 && !(c_row == row && c_col == col-1) && ('0' <= agents[row][col-1] && agents[row][col-1] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                    }



                    return new Conflict(ConflictTypes.Move);
                }

                char agent = agents[node.parent.agentRow][node.parent.agentCol];
                if(IncludeNegativeEffect)
                    agents[node.parent.agentRow][node.parent.agentCol] = ' ';

                agents[node.agentRow][node.agentCol] = agent;

            } else if (node.action.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (boxAt(node.agentRow, node.agentCol,boxes)) {
                    int newBoxRow = node.agentRow + Command.dirToRowChange(node.action.dir2);
                    int newBoxCol = node.agentCol + Command.dirToColChange(node.action.dir2);
                    int row = newBoxRow;
                    int col = newBoxCol;
                    if(('0' <= agents[newBoxRow][newBoxCol] && agents[newBoxRow][newBoxCol] <= '9')) {
                        System.err.println("ConflictDetector: " + node.agentRow + "/" + node.agentCol);
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Character.getNumericValue(agents[row][col])));
                    }

                    if(boxes[newBoxRow][newBoxCol] != 0){
                        int c_row = node.agentRow;
                        int c_col = node.agentCol;

                        if(row+1 <= CentralPlanner.MAX_ROW &&!(c_row == row+1 && c_col == col) && ('0' <= agents[row+1][col] && agents[row+1][col] <= '9')){
                            System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));
                            return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));}
                        if(row-1 > 0 && !(c_row == row-1 && c_col == col) && ('0' <= agents[row-1][col] && agents[row-1][col] <= '9')){
                            System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));
                            return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));}
                        if(col+1 <= CentralPlanner.MAX_COL && !(c_row == row && c_col == col+1) && ('0' <= agents[row][col+1] && agents[row][col+1] <= '9')){
                            System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                            return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                        }
                        if(col-1 > 0 && !(c_row == row && c_col == col-1) && ('0' <= agents[row][col-1] && agents[row][col-1] <= '9')){
                            System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                            return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                        }



                        return new Conflict(ConflictTypes.Push,agents,boxes);
                    }

                    char agent = agents[node.parent.agentRow][node.parent.agentCol];
                    char box = boxes[node.agentRow][node.agentCol];

                    agents[node.agentRow][node.agentCol] = agent;
                    boxes[newBoxRow][newBoxCol] = box;
                    if(IncludeNegativeEffect)
                        agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                    if(IncludeNegativeEffect)
                        boxes[node.agentRow][node.agentCol] = 0;
                }else
                    return new Conflict(ConflictTypes.Push,agents,boxes);
            }
            //
            // Check this code;
            else if (node.action.actionType == Command.Type.Pull) {

                if(node.c.getNumber() == 4 || node.c.getNumber() == 1){
                    System.err.println(node.c.getNumber() + " : Row " + node.agentRow + " Col " + node.agentCol);
                    System.err.println(node.c.getNumber() + " : CurrentRow " + node.c.currentState.agentRow + " CurrentCol " + node.c.currentState.agentCol);

                }

                if(('0' <= agents[node.agentRow][node.agentCol] && agents[node.agentRow][node.agentCol] <= '9')) {
                    System.err.println("ConflictDetector: " + node.agentRow + "/" + node.agentCol);
                    return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Character.getNumericValue(agents[node.agentRow][node.agentCol])));
                }

                int boxRow = node.parent.agentRow + Command.dirToRowChange(node.action.dir2);
                int boxCol = node.parent.agentCol + Command.dirToColChange(node.action.dir2);
                int row = node.agentRow;
                int col = node.agentCol;

                if(boxes[node.agentRow][node.agentCol] != 0){
                    int c_row = node.agentRow;
                    int c_col = node.agentCol;

                    if(row+1 <= CentralPlanner.MAX_ROW &&!(c_row == row+1 && c_col == col) && ('0' <= agents[row+1][col] && agents[row+1][col] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row+1][col]+"")));}
                    if(row-1 > 0 && !(c_row == row-1 && c_col == col) && ('0' <= agents[row-1][col] && agents[row-1][col] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row-1][col]+"")));}
                    if(col+1 <= CentralPlanner.MAX_COL && !(c_row == row && c_col == col+1) && ('0' <= agents[row][col+1] && agents[row][col+1] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col+1]+"")));
                    }
                    if(col-1 > 0 && !(c_row == row && c_col == col-1) && ('0' <= agents[row][col-1] && agents[row][col-1] <= '9')){
                        System.err.println("Number of Agent !!!!: " +CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                        return new Conflict(ConflictTypes.AgentsBlockEachother,CentralPlanner.clients.get(Integer.parseInt(agents[row][col-1]+"")));
                    }



                    return new Conflict(ConflictTypes.Pull,agents,boxes);
                }

                char agent = agents[node.parent.agentRow][node.parent.agentCol];
                if(IncludeNegativeEffect)
                    agents[node.parent.agentRow][node.parent.agentCol] = ' ';
                agents[node.agentRow][node.agentCol] = agent;

                char box = boxes[boxRow][boxCol];
                //boxes[boxRow][boxCol] = 0;
                boxes[node.parent.agentRow][node.parent.agentCol] = box;

                // Cell is free where agent is going
                /*
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

                        if(node.c.getNumber() == 8)
                        {
                            System.err.println("Current box pos: " + boxRow + ":" + boxCol);
                            System.err.println("New Box pos: " + node.parent.agentRow + ":" + node.parent.agentCol);
                            System.err.println("New Agent: " + node.agentRow + ":" + node.agentCol);
                        }

                    }else
                        return new Conflict(ConflictTypes.Pull,agents,boxes);
                        */

            }
        }

        return new Conflict(ConflictTypes.NoConflict);

    }
}
