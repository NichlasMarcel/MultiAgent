package multiagent;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.List;

/**
 * Created by Nichlas on 22-03-2017.
 */
public class Bartek {

    static char[][] agents = new char[1][1];

    public static Boolean CheckIfActionCanBeApplied(List<Node> nodes, CentralPlanner cp){


        // Bartek, just remove this
        // I only used it for testing purposes
       if(agents.equals(cp.agents))
           return true;
       else{
           agents = cp.agents;
           return false;
       }
    }
}
