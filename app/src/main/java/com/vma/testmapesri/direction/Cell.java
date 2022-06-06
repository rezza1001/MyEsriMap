package com.vma.testmapesri.direction;

public class Cell {
    public int x = 0;
    public int y = 0;
    public double valueX = 0;
    public double valueY = 0;
    public boolean isLand = false;

    public String toString(){
        return "x = ["+x+" = "+valueX+"] " + "y = ["+y+" = "+valueY+"] isLand "+ isLand;
    }
}
