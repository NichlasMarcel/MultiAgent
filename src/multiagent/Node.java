package multiagent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;


public class Node {
    private static final Random RND = new Random(1);


    public int agentRow;
    public int agentCol;

    public char[][] boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];


    public Node parent;
    public Command action;
    public Client c;
    public int g;

    private int _hash = 0;

    public Node(Node parent, Client c) {
        this.parent = parent;
        this.c = c;
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

        return c.goalStack.peek().IsGoal(this);
    }

    public ArrayList<Node> getExpandedNodes() {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action


            if (c.actionType == Command.Type.NoOp) {
                Node n = this.ChildNode();
                n.action = c;
                n.agentRow = this.agentRow;
                n.agentCol = this.agentCol;
                expandedNodes.add(n);
                continue;
            }

            int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
            int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);


            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    Node n = this.ChildNode();
                    n.action = c;
                    n.agentRow = newAgentRow;
                    n.agentCol = newAgentCol;
                    expandedNodes.add(n);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        Node n = this.ChildNode();
                        n.action = c;
                        n.agentRow = newAgentRow;
                        n.agentCol = newAgentCol;
                        n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
                        n.boxes[newAgentRow][newAgentCol] = 0;
                        expandedNodes.add(n);
                    }
                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
                    int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        Node n = this.ChildNode();
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

        return expandedNodes;
    }

    public boolean cellIsFree(int row, int col) {
        return !c.walls[row][col] && this.boxes[row][col] == 0;
    }

    public boolean boxAt(int row, int col) {
        return this.boxes[row][col] > 0;
    }

    public Node ChildNode() {
        Node copy = new Node(this, c);
        copy.agentCol = this.agentCol;
        copy.agentRow = this.agentRow;
        copy.action = this.action;
        copy.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        CentralPlanner.CopyBoxes(this.boxes, copy.boxes);
        return copy;
    }

    public Node Copy() {
        Node copy = new Node(this.parent, c);
        copy.agentCol = this.agentCol;
        copy.agentRow = this.agentRow;
        copy.action = this.action;
        copy.boxes = new char[CentralPlanner.MAX_ROW][CentralPlanner.MAX_COL];
        CentralPlanner.CopyBoxes(this.boxes, copy.boxes);
        return copy;
    }

    public LinkedList<Node> extractPlan() {
        LinkedList<Node> plan = new LinkedList<Node>();
        Node n = this;
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
            result = prime * result + this.agentCol;
            result = prime * result + this.agentRow;
            result = prime * result + Arrays.deepHashCode(this.boxes);
            //result = prime * result + Arrays.deepHashCode(c.goals);
            result = prime * result + Arrays.deepHashCode(c.walls);
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
        Node other = (Node) obj;
        if (this.agentRow != other.agentRow || this.agentCol != other.agentCol)
            return false;
        return Arrays.deepEquals(this.boxes, other.boxes);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int row = 0; row < CentralPlanner.MAX_ROW; row++) {
            if (!c.walls[row][0]) {
                break;
            }
            for (int col = 0; col < CentralPlanner.MAX_COL; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (c.goals[row][col] > 0) {
                    s.append(c.goals[row][col]);
                } else if (c.walls[row][col]) {
                    s.append("+");
                } else if (row == this.agentRow && col == this.agentCol) {
                    s.append(c.getNumber());
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

}