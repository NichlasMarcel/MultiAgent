package multiagent;

import java.util.ArrayList;

/**
 * Created by Dan on 4/1/2017.
 */
public class GoalCell {

    int x,y;
    char c;
    ArrayList<GoalCell> goalsBefore = new ArrayList<>();
    public GoalCell(int x, int y, char c)
    {
        this.x = x;
        this.y= y;
        this.c = c;


    }

    public  void findGoalsBefore()
    {
        if (surrounded() + getSurroundingGoals().size()==4)
        {
            for (GoalCell g: getSurroundingGoals())
            {
                g.goalsBefore.add(this);

            }

        }

    }

    public GoalCell findGoalBefore(GoalCell g)
    {
        if (goalsBefore.size()>0)
        return goalsBefore.get(0);
        return g;

    }


    public int surrounded()
    {
        int s = 0;

        if (CentralPlanner.walls[x+1][y])
            s++;
        if (CentralPlanner.walls[x-1][y])
            s++;
        if (CentralPlanner.walls[x][y+1])
            s++;
        if (CentralPlanner.walls[x][y-1])
            s++;

        return s;
}


    public ArrayList<GoalCell> getSurroundingGoals()
    {
        GoalCell[][] goals = CentralPlanner.goalsMap;
            ArrayList<GoalCell> surroundingGoalCells = new ArrayList<>();

        if (goals[x+1][y]!=null)
            surroundingGoalCells.add(goals[x+1][y]);
        if (goals[x-1][y]!=null)
            surroundingGoalCells.add(goals[x-1][y]);
        if (goals[x][y+1]!=null)
            surroundingGoalCells.add(goals[x][y+1]);
        if (goals[x][y-1]!=null)
            surroundingGoalCells.add(goals[x][y-1]);


        return surroundingGoalCells;
    }

    @Override
    public String toString()
    {
        return "Goal: " + c;
    }








}
