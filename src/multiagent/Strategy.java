package multiagent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Stack;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.ArrayList;

public abstract class Strategy {
	private HashSet<Node> explored;
	private final long startTime;

	public Strategy() {
		this.explored = new HashSet<Node>();
		this.startTime = System.currentTimeMillis();
	}

	public void addToExplored(Node n) {
		this.explored.add(n);
	}

	public boolean isExplored(Node n) {
		return this.explored.contains(n);
	}

	public int countExplored() {
		return this.explored.size();
	}

	public String searchStatus() {
		return "";
		//return String.format("#Explored: %6d, #Frontier: %6d, #Generated: %6d, Time: %3.2f s \t%s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent());
	}

	public float timeSpent() {
		return (System.currentTimeMillis() - this.startTime) / 1000f;
	}

	public abstract Node getAndRemoveLeaf();

	public abstract void addToFrontier(Node n);

	public abstract boolean inFrontier(Node n);

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();

	@Override
	public abstract String toString();

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

	public static class StrategyDFS extends Strategy {
	
		private Stack frontier;
		private HashSet<Node> frontierSet;
		public StrategyDFS() {
			super();
			frontier = new Stack();
			frontierSet = new HashSet<Node>();
		}

		@Override
		public Node getAndRemoveLeaf() {
			Node n = (Node)frontier.pop();
			frontierSet.remove(n);
			
			return n;
		}

		@Override
		public void addToFrontier(Node n) {
			frontier.push(n);
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
			return "Depth-first Search";
		}
	}

	// Ex 3: Best-first Search uses a priority queue (Java contains no implementation of a Heap data structure)
	public static class StrategyBestFirst extends Strategy {
		private Heuristic heuristic;
		PriorityQueue<Node> frontier;// = new PriorityQueue<Node>();
		private HashSet<Node> frontierSet;
		public StrategyBestFirst(Heuristic h) {
			super();
			this.heuristic = h;
			frontier = new PriorityQueue<Node>(h);
			frontierSet = new HashSet<Node>();
			//throw new NotImplementedException();
		}

		@Override
		public Node getAndRemoveLeaf() {
			Node n = frontier.poll();
			frontierSet.remove(n);
			return  n;

			//throw new NotImplementedException();
		}

		@Override
		public void addToFrontier(Node n) {
			frontier.add(n);
			frontierSet.add(n);
			//throw new NotImplementedException();
		}

		@Override
		public int countFrontier() {
			return frontier.size();
			//throw new NotImplementedException();
		}

		@Override
		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
			//throw new NotImplementedException();
		}

		@Override
		public boolean inFrontier(Node n) {

			return frontierSet.contains(n);
		}

		@Override
		public String toString() {
			return "Best-first Search using " + this.heuristic.toString();
		}
	}
}
