package multiagent;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
    {   System.err.println(goalsBefore);


        if (goalsBefore.size()>0){


            ArrayList<GoalCell> l = new ArrayList<>();
            l.add(g);
            l= AddNeighbourGoals(l,g);
            System.err.println("L size: " + l.size() + " Goals: "  + l);

            for(GoalCell i: l)
                if (i.surrounded()==3)
                    return i;
        }
        return g;

    }

    public ArrayList<GoalCell> AddNeighbourGoals(ArrayList<GoalCell> list, GoalCell go)
    {
        boolean k = false;
        for (GoalCell n : go.goalsBefore)
        {
            if(list.size()!=0)
            {
                boolean t= true;
                for (GoalCell g: list)
                {
                    if(g.x == n.x && g.y == n.y)
                        t=false;

                }
                if (t) {
                    System.err.println("adding "  + n );
                    list.add(n);
                    k = true;
                }
            }
            if(k)
                list = AddNeighbourGoals(list,n);

        }

        return list;

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
