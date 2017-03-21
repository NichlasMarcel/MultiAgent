package multiagent;

import java.util.Comparator;

public abstract class Heuristic implements Comparator<Node> {
	private Node initialState;
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		this.initialState = initialState;
	}

	public int h(Node n) {
			int sum = 0;

			for(int g_x = 1; g_x < Client.MAX_ROW - 1; g_x++){
				for(int g_y = 1; g_y < Client.MAX_COL - 1; g_y++){
					if(initialState.c.goals[g_x][g_y] != ' '){
						if(n.boxes[g_x][g_y] == Character.toUpperCase(initialState.c.goals[g_x][g_y])){
							sum += 1;
						}
					}
				}
			}
			
			return sum;	
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
	
		private Node initialState;
		public Greedy(Node initialState) {
			super(initialState);
			this.initialState = initialState;
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
