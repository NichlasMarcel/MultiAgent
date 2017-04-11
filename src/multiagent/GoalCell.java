//package multiagent;
//
//import java.util.ArrayList;
//
///**
// * Created by Dan on 4/1/2017.
// */
//public class GoalCell {
//
//    int x,y;
//    char c;
//    ArrayList<GoalCell> goalsBefore = new ArrayList<>();
//    public GoalCell(int x, int y, char c)
//    {
//        this.x = x;
//        this.y= y;
//        this.c = c;
//
//
//    }
//
//    public void findGoalBefore()
//    {
//        if (surrounded() + getSurroundingGoals().size()==4)
//        {
//            for (GoalCell g: getSurroundingGoals())
//            {
//                g.goalsBefore.add(this);
//            }
//
//        }
//    }
//
//
//    public int surrounded()
//    {
//        int s = 0;
//
//        if (CentralPlannerOld.walls[x+1][y])
//            s++;
//        if (CentralPlannerOld.walls[x-1][y])
//            s++;
//        if (CentralPlannerOld.walls[x][y+1])
//            s++;
//        if (CentralPlannerOld.walls[x][y-1])
//            s++;
//
//        return s;
//}
//
//
//    public ArrayList<GoalCell> getSurroundingGoals()
//    {
//            ArrayList<GoalCell> surroundingGoalCells = new ArrayList<>();
//
//            if(CentralPlannerOld.goalsMap.get(x+1)!=null) if (CentralPlannerOld.goalsMap.get(x+1) .get(y)!=null)
//            surroundingGoalCells.add(CentralPlannerOld.goalsMap.get(x+1).get(y));
//        if(CentralPlannerOld.goalsMap.get(x-1)!=null) if(CentralPlannerOld.goalsMap.get(x-1).get(y)!=null)
//            surroundingGoalCells.add(CentralPlannerOld.goalsMap.get(x-1).get(y));
//        if(CentralPlannerOld.goalsMap.get(x)!=null) {
//        if (CentralPlannerOld.goalsMap.get(x).get(y + 1) != null)
//            surroundingGoalCells.add(CentralPlannerOld.goalsMap.get(x).get(y + 1));
//        if (CentralPlannerOld.goalsMap.get(x).get(y - 1) != null)
//            surroundingGoalCells.add(CentralPlannerOld.goalsMap.get(x).get(y - 1));
//    }
//
//        return surroundingGoalCells;
//    }
//
//
//
//
//
//
//
//
//
//}
