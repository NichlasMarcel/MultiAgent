package multiagent;

import java.util.ArrayDeque;
import java.util.HashSet;

/**
 * Created by Dan on 3/29/2017.
 */
public class MapAnalyzer {

    public static class StrategyBFS extends Strategy {
        private ArrayDeque<Node> frontier;
        private HashSet<Node> frontierSet;

        public StrategyBFS() {
            super();
            frontier = new ArrayDeque<Node>();
            frontierSet = new HashSet<Node>();
        }

        @Override
        public Node getAndRemoveLeaf() {
            Node n = frontier.pollFirst();
            frontierSet.remove(n);
            return n;
        }

        @Override
        public void addToFrontier(Node n) {
            frontier.addLast(n);
            frontierSet.add(n);
        }

        @Override
        public int countFrontier() {
            return frontier.size();
        }

        @Override
        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        @Override
        public boolean inFrontier(Node n) {
            return frontierSet.contains(n);
        }

        @Override
        public String toString() {
            return "Breadth-first Search";
        }
    }

}
