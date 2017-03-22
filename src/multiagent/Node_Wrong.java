package multiagent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;


public class Node_Wrong {
    /*
    // Arrays are indexed from the top-left of the level, with first index being row and second being column.
    // Row 0: (0,0) (0,1) (0,2) (0,3) ...
    // Row 1: (1,0) (1,1) (1,2) (1,3) ...
    // Row 2: (2,0) (2,1) (2,2) (2,3) ...
    // ...
    // (Start in the top left corner, first go down, then go right)
    // E.g. this.walls[2] is an array of booleans having size MAX_COL.
    // this.walls[row][col] is true if there's a wall at (row, col)
    //


    public char[][] boxes = new char[Client.MAX_ROW][Client.MAX_COL];
    public char[][] agents = new char[Client.MAX_ROW][Client.MAX_COL];


    public Node_Wrong parent;
    //public Command action;

    private int g;

    private int _hash = 0;

    public Node_Wrong(Node_Wrong parent) {
        this.parent = parent;
        if (parent == null) {
            this.g = 0;
        } else {
            this.g = parent.g() + 1;
        }
    }

    public int g() {
        return this.g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {
        for (int row = 1; row < Client.MAX_ROW - 1; row++) {
            for (int col = 1; col < Client.MAX_COL - 1; col++) {
                char g = Client.goals[row][col];
                char b = Character.toLowerCase(boxes[row][col]);
                if (g > 0 && b != g) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<Node_Wrong> getExpandedNodes() {
        ArrayList<Node_Wrong> expandedNodes = new ArrayList<Node_Wrong>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action

            int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
            int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

            if (c.actionType == Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    Node_Wrong n = this.ChildNode();
                    n.action = c;
                    n.agentRow = newAgentRow;
                    n.agentCol = newAgentCol;
                    expandedNodes.add(n);
                }
            } else if (c.actionType == Type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        Node_Wrong n = this.ChildNode();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
                        n.boxes[newAgentRow][newAgentCol] = 0;
                        expandedNodes.add(n);
                    }
                }
            } else if (c.actionType == Type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
                    int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        Node_Wrong n = this.ChildNode();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        n.boxes[this.agentRow][this.agentCol] = this.boxes[boxRow][boxCol];
                        n.boxes[boxRow][boxCol] = 0;
                        expandedNodes.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedNodes, RND);
        return expandedNodes;
    }


    /**
     * Test this shit out!!!!
     * @param row this is the x coordinate
     * @param col this is the y coordinate
     * @return

    private boolean cellIsFree(int row, int col) {
        return !Client.walls[row][col] && this.boxes[row][col] == 0 && this.agents[row][col] == 0;
    }

    private boolean boxAt(int row, int col) {
        return this.boxes[row][col] > 0;
    }

    private Node_Wrong ChildNode() {
        Node_Wrong copy = new Node_Wrong(this);
        for (int row = 0; row < Client.MAX_ROW; row++) {
            System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, Client.MAX_COL);
        }
        return copy;
    }

    public LinkedList<Node_Wrong> extractPlan() {
        LinkedList<Node_Wrong> plan = new LinkedList<Node_Wrong>();
        Node_Wrong n = this;
        while (!n.isInitialState()) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(this.agents);
            result = prime * result + Arrays.deepHashCode(this.boxes);
            result = prime * result + Arrays.deepHashCode(Client.goals);
            result = prime * result + Arrays.deepHashCode(Client.walls);
            this._hash = result;
        }
        return this._hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Node_Wrong other = (Node_Wrong) obj;
        if (!Arrays.deepEquals(this.agents, other.agents))
            return false;
        if (!Arrays.deepEquals(this.boxes, other.boxes))
            return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < Client.MAX_ROW; row++) {
            if (!Client.walls[row][0]) {
                break;
            }
            for (int col = 0; col < Client.MAX_COL; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (Client.goals[row][col] > 0) {
                    s.append(Client.goals[row][col]);
                } else if (Client.walls[row][col]) {
                    s.append("+");
                } else if (this.agents[row][col] > 0) {
                    s.append(this.agents[row][col]);
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
*/
}