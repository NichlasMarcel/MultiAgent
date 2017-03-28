package multiagent;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.List;
import java.util.Random;

/**
 * Created by Nichlas on 22-03-2017.
 */
public class CheckWhetherActionPossible {


    public static Boolean CheckIfActionCanBeApplied(List<Node> nodes, CentralPlanner cp){
        System.err.println("Here are size of actions : " + nodes.size());
        for(int i = 0; i < nodes.size(); i++){
            Node node = nodes.get(i);
            //node.action.
            if(node.action.actionType == Command.Type.Move){
                if(cp.IsCellFree(nodes.get(i).agentRow, nodes.get(i).agentCol)) {
                    return true;
                }
                else {

                    if ('A' <= nodes.get(i).agentRow && nodes.get(i).agentCol <= 'Z') {
                        return false;
                    }
                    else if ('0' <= nodes.get(i).agentRow && nodes.get(i).agentCol <= '9'){
                        return  false;
                    }
                    else {
                        return true;
                    }
                }
            }
            else{
                return true;
            }


        }
        return true;
    }
}
