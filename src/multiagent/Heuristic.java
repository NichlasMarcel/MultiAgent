package multiagent;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class Heuristic implements Comparator<Node> {
	private Node initialState;

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		this.initialState = initialState;
	}






	public int h(Node n) {

		for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
			for (int j = 0; j < CentralPlanner.MAX_COL; j++) {

				if(n.boxes[i][j] != 0){
					double distance = CentralPlanner.CalculateMathDistance(n.agentRow,n.agentCol,i, j) +
							CentralPlanner.CalculateMathDistance(i,j, n.c.goalStack.peek().goalRow, n.c.goalStack.peek().goalCol);

					//System.err.println("TEST: d " + distance + "br " + i + " bc " + j + " gr " + n.c.goalStack.peek().goalRow + " gc " + n.c.goalStack.peek().goalCol);
					return (int)(distance);

				}
			}
		}


		return Integer.MAX_VALUE;

/*
		boolean finished = false;
		int grow = -1;
		int gcol = -1;
		int row = -1;
		int col = -1;

		for (int i = 0; i < CentralPlanner.MAX_ROW; i++) {
			if(finished)
				break;
			for (int j = 0; j < CentralPlanner.MAX_COL; j++) {
				if(n.c.goalStack.peek().goals[i][j] != 0){

					grow = i;
					gcol = j;
					finished = true;
					break;
				}

				if(n.boxes[i][j] != 0)
				{
					row = i;
					col = j;
				}
			}


		}


		double distance = CentralPlanner.CalculateMathDistance(row,col, grow,gcol) + CentralPlanner.CalculateMathDistance(row,col,n.agentRow,n.agentCol);
		//System.err.println("Distance: " + distance);
		return (int)distance;
*/

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
