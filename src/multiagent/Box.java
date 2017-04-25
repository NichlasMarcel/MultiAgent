package multiagent;

import java.util.ArrayList;

/**
 * Created by Dan on 4/4/2017.
 */
public class Box {



    int x,y;
    char c;
    String color;
    ArrayList<Box> boxesBlocking = new ArrayList<>();
    public Box(int x,int y, char c)
    {
        this.x = x;
        this.y= y;
        this.c = c;



    }

    public void setColor(String s)
    {
        color = s;
    }

    public String getColor()
    {
        return color;
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

    public int surroundedWallsAndBoxes()
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

        if (CentralPlanner.boxes[x+1][y]>0)
            s++;
        if (CentralPlanner.boxes[x-1][y]>0)
            s++;
        if (CentralPlanner.boxes[x][y+1]>0)
            s++;
        if (CentralPlanner.boxes[x][y-1]>0)
            s++;

        return s;
    }

    public ArrayList<Box> getSurroundingBoxes(char[][] boxes)
    {
        ArrayList<Box> surroundingBoxes = new ArrayList<>();

        if (boxes[x+1][y]>='A')
            surroundingBoxes.add(new Box(x+1, y, boxes[x+1][y] ));
        if (boxes[x-1][y]>='A')
            surroundingBoxes.add(new Box(x-1, y, boxes[x-1][y] ));
        if (boxes[x][y+1]>='A')
            surroundingBoxes.add(new Box(x, y+1, boxes[x][y+1] ));
        if (boxes[x][y-1]>='A')
            surroundingBoxes.add(new Box(x, y-1, boxes[x][y-1] ));

        return surroundingBoxes;
    }




    public ArrayList<Box> findBlockingBoxes(char [][] boxes)
    {
        if (surrounded() + getSurroundingBoxes(boxes).size()==4)
        {
            for (Box b: getSurroundingBoxes(boxes))
            {
                this.boxesBlocking.add(b);
            }

        }
        return boxesBlocking;
    }
    @Override
    public String toString()
    {
        return "Box:" + c + " Row:" + x +"Col: " + y;
    }





}
