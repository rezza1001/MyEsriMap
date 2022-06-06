package com.vma.testmapesri.direction;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.vma.testmapesri.FileProcessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Track {

    ArrayList<Cell> cells = new ArrayList<>();
    ArrayList<Cell> blockCell = new ArrayList<>();
    HashMap<Integer, Double> mapRow = new HashMap<>();
    HashMap<Integer, Double> mapCol = new HashMap<>();

    private double valueStartX, valueStartY, valueEndX, valueEndY;

    private int startX = 0;
    private int startY = 0;
    private int endX = 0;
    private int endY = 0;

    private int maxX = 0;
    private int maxY = 0;


    public void setStartNode(double x, double y){
        valueStartX = x;
        valueStartY = y;
    }
    public void setEndNode(double x, double y){
        valueEndX = x;
        valueEndY = y;
    }


    public void createTrack(Context context){
        cells.clear();
        blockCell.clear();
        try {
            File myObj = new File(FileProcessing.getMainPath(context).getAbsolutePath() +"/"+ FileProcessing.ROOT+"/dummyAstar.txt");
            Scanner myReader = new Scanner(myObj);

            HashMap<Double,Integer> mapX = new HashMap<>();
            HashMap<Double,Integer> mapY = new HashMap<>();


            int indexX = 0;
            int indexY = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                double x = Double.parseDouble(data.split(";")[2]);
                double y = Double.parseDouble(data.split(";")[3]);
                String type = data.split(";")[1];

                Cell cell = new Cell();
                cell.valueX = x;
                cell.valueY = y;

                if (type.equals("Darat")){
                    cell.isLand = true;
                }

                if (!mapX.containsKey(x)){
                    mapX.put(x,indexX);
                    cell.x = indexX;
                    indexX ++;
                }
                else {
                    indexX = mapX.get(x);
                    cell.x = indexX;
                }


                if (!mapY.containsKey(y)){
                    mapY.put(y, indexY);
                }
                else {
                    indexY = mapY.get(y);
                }
                cell.y = indexY;
                indexY ++;
                cells.add(cell);
            }

            myReader.close();

            for (Cell cell : cells){
                if (cell.isLand){
                    blockCell.add(cell);
                }

                if (maxX < cell.x){
                    maxX = cell.x;
                }
                if (maxY < cell.y){
                    maxY = cell.y;
                }

                if (!cell.isLand){
                    findClosestStartCell(cell.valueX ,cell.valueY, cell.x, cell.y);
                    findClosestEndCell(cell.valueX ,cell.valueY, cell.x, cell.y);
//                    if (cell.valueX == valueStartX && cell.valueY == valueStartY){
//                        startX = cell.x;
//                        startY = cell.y;
//                    }

//                    if (cell.valueX == valueEndX && cell.valueY == valueEndY){
//                        endX = cell.x;
//                        endY = cell.y;
//                    }
                }

                if (cell.x == 0){
                    mapRow.put(cell.y, cell.valueY);
                }
                if (cell.y == 0){
                    mapCol.put(cell.x, cell.valueX);
                }
            }

            if (trackCreatedLister != null){
                trackCreatedLister.onCreatedTrack();
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    double minSizeStart =  -1;
    private void findClosestStartCell(double toX, double toY, int indexX, int indexY){
        double size = calcDistance(valueStartY, valueStartX,toY,toX);
        minSizeStart = minSizeStart == -1 ? size : minSizeStart;
        if (minSizeStart >= size ){
            minSizeStart= size;

            startX = indexX;
            startY = indexY;
        }
    }

    double minSizeEnd =  -1;
    private void findClosestEndCell(double toX, double toY, int indexX, int indexY){
        double size = calcDistance(valueEndY, valueEndX,toY,toX);
        minSizeEnd = minSizeEnd == -1 ? size : minSizeEnd;
        if (size <= minSizeEnd){
            minSizeEnd= size;
            endX = indexX;
            endY = indexY;
        }
    }

    public int getRow(){
        return maxY +1;
    }

    public int getColumn(){
        return maxX + 1;
    }

    public Node getInitialNode(){
        Log.d("Track","InitialNode = "+startX+" , "+startY);
        return new Node(startY, startX);
    }
    public Node getDestinationNode(){
        Log.d("Track","DestinationNode = "+endY+" , "+endX);
        return new Node(endY, endX);
    }

    public ArrayList<Cell> getBlockCell(){
        return blockCell;
    }

    public ArrayList<Cell> convertToCell(List<Node> list){
        ArrayList<Cell> cells = new ArrayList<>();
        {
            Cell cell = new Cell();
            cell.valueX = valueStartX;
            cell.valueY = valueStartY;
            cells.add(cell);
        }
        for (Node node : list){
            Log.d("Track","Route "+ node.toString());
            Cell cell = new Cell();
            cell.valueY = mapRow.get(node.getRow());
            cell.valueX = mapCol.get(node.getCol());
            cell.x = node.getCol();
            cell.y = node.getRow();
            cells.add(cell);
        }

        {
            Cell cell = new Cell();
            cell.valueX = valueEndX;
            cell.valueY = valueEndY;
            cells.add(cell);
        }
        return cells;
    }

    public static double calcDistance(double lat1, double lon1, double lat2, double lon2){
        double distance;

        Location point1 = new Location("locationA");
        point1.setLatitude(lat1);
        point1.setLongitude(lon1);

        Location point2 = new Location("locationB");
        point2.setLatitude(lat2);
        point2.setLongitude(lon2);

        distance = point1.distanceTo(point2);
        distance /= 1852.0;

        return distance;
    }

    private TrackCreatedLister trackCreatedLister;
    public void setTrackCreatedLister(TrackCreatedLister trackCreatedLister){
        this.trackCreatedLister = trackCreatedLister;
    }
    public interface TrackCreatedLister {
        void onCreatedTrack();
    }
}
