package com.vma.testmapesri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LoadSettings;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.util.ListenableList;
import com.vma.testmapesri.direction.AStar;
import com.vma.testmapesri.direction.Cell;
import com.vma.testmapesri.direction.Node;
import com.vma.testmapesri.direction.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MapView mapvw_main;
    private MobileMapPackage mapPackage;
    Activity mActivity;

    private final double MAX_SCALE = 3000000;
    private final double MIN_SCALE = 5000;

    GraphicsOverlay graphicsOverlayDirection, graphicsPin;
    PictureMarkerSymbol pinStart, pinEnd;
    ArrayList<Point> directionPoint = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        initMap();

        findViewById(R.id.bbtn_delete).setOnClickListener(view -> {
            graphicsOverlayDirection.getGraphics().clear();
            graphicsPin.getGraphics().clear();
            directionPoint.clear();
        });

        findViewById(R.id.imvw_add).setOnClickListener(view -> addMarker());
        findViewById(R.id.bbtn_create).setOnClickListener(view -> initDirection());
    }

    protected void initMap(){
        mapvw_main = findViewById(R.id.mapvw_main);
        Context context = mActivity.getApplicationContext();
        boolean settingsCanWrite = Settings.System.canWrite(context);
        if(!settingsCanWrite) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mActivity.getPackageName()));
            mActivity.startActivity(intent);
        }

        String pathMap = FileProcessing.getMainPath(mActivity)+"/"+FileProcessing.ROOT+"/map.mmpk";
        File fileMap = new File(pathMap);
        mapPackage = new MobileMapPackage(fileMap.getAbsolutePath());
        mapPackage.loadAsync();
        mapPackage.addDoneLoadingListener(() -> {
            if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
                Utility.showToastError(mActivity,"Loaded map success");
                ArcGISMap map= mapPackage.getMaps().get(0);
                //  map view setting
                map.setMinScale(MAX_SCALE);
                map.setMaxScale(MIN_SCALE);

                LoadSettings loadSettings = new LoadSettings();
                loadSettings.setPreferredPointFeatureRenderingMode(FeatureLayer.RenderingMode.AUTOMATIC);
                loadSettings.setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.AUTOMATIC);
                loadSettings.setPreferredPolylineFeatureRenderingMode(FeatureLayer.RenderingMode.AUTOMATIC);
                map.setLoadSettings(loadSettings);
                mapvw_main.setMap(map);

                mapvw_main.setViewpointCenterAsync(new Point(105.2581213,-6.61159699,SpatialReferences.getWgs84()));
                new Handler().postDelayed(() -> mapvw_main.setViewpointScaleAsync(150000),2000);
            }
        });

        graphicsOverlayDirection = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        mapvw_main.getGraphicsOverlays().add(graphicsOverlayDirection);

        graphicsPin = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        mapvw_main.getGraphicsOverlays().add(graphicsPin);

        pinStart = createDestinationFinishMarker(R.drawable.track_start,25,25);
        pinEnd = createDestinationFinishMarker(R.drawable.track_end,25,25);

        directionPoint.add(new Point(105.163922,-6.585477,SpatialReferences.getWgs84()));
        directionPoint.add(new Point(105.160275,-6.632540,SpatialReferences.getWgs84()));
        initDirection();
    }


    private void addMarker(){
        ListenableList<Graphic> pinGraphics = graphicsPin.getGraphics();
        if (directionPoint.size() == 2){
            pinGraphics.remove(1);
            directionPoint.remove(1);
        }

        float centreX   = mapvw_main.getX() + mapvw_main.getWidth()  / 2f;
        float centreY   = mapvw_main.getY() + mapvw_main.getHeight() / 2.1f ;
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(centreX), Math.round(centreY));
        Point mapPoint = mapvw_main.screenToLocation(screenPoint);
        Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());

        Graphic graphic = new Graphic(wgs84Point, pinStart);
        if (pinGraphics.size() > 0){
            graphic = new Graphic(wgs84Point, pinEnd);
        }
        pinGraphics.add(graphic);
        directionPoint.add(wgs84Point);
    }


    private void initDirection(){
        if (directionPoint.size() == 0){
            Toast.makeText(mActivity,"Silahkan tambah posisi awal", Toast.LENGTH_LONG).show();
            return;
        }
        if (directionPoint.size() == 1){
            Toast.makeText(mActivity,"Silahkan tambah posisi akhir", Toast.LENGTH_LONG).show();
            return;
        }
        Point start =   directionPoint.get(0);
        Point end = directionPoint.get(1);
        SimpleLineSymbol   line  = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.BLUE, 2);
        PointCollection pointCollection = new PointCollection(SpatialReferences.getWgs84());
        pointCollection.add(start);
        pointCollection.add(end);
        Polyline polyline       = new Polyline(pointCollection);
        Graphic graphicRoute    = new Graphic(polyline, line);

        ListenableList<Graphic> graphicList = graphicsOverlayDirection.getGraphics();
        graphicList.clear();
        graphicList.add(graphicRoute);
        new Handler().postDelayed(() -> {
            ListenableList<Graphic> pinGraphics = graphicsPin.getGraphics();
            pinGraphics.clear();
            Graphic startGraphic = new Graphic(start, pinStart);
            Graphic endGraphic = new Graphic(end, pinEnd);
            pinGraphics.add(startGraphic);
            pinGraphics.add(endGraphic);
        },2000);

        Track track = new Track();
        track.setStartNode(start.getX(),start.getY());
        track.setEndNode(end.getX(),end.getY());

        track.setTrackCreatedLister(() -> {
            Node initialNode = track.getInitialNode();
            Node finalNode = track.getDestinationNode();
            int rows = track.getRow();
            int cols = track.getColumn();
            AStar aStar = new AStar(rows, cols, initialNode, finalNode);
            aStar.setBlock(track.getBlockCell());
            List<Node> nodes = aStar.findPath();
            ArrayList<Cell> cells = track.convertToCell(nodes);
            createNewTrack(cells);
        });
        track.createTrack(mActivity);
    }

    private PictureMarkerSymbol createDestinationFinishMarker(int resource, int h, int w){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(mActivity, resource);
        PictureMarkerSymbol mSymbolTargetPos = new PictureMarkerSymbol(bitmapDrawable);
        mSymbolTargetPos.setHeight(h);
        mSymbolTargetPos.setWidth(w);
        mSymbolTargetPos.loadAsync();
        return mSymbolTargetPos;
    }

    private void createNewTrack(ArrayList<Cell> cells){
        SimpleLineSymbol   line  = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#107FD6"), 3);
        PointCollection pointCollection = new PointCollection(SpatialReferences.getWgs84());
        for (Cell node : cells) {
            pointCollection.add( new Point(node.valueX, node.valueY, SpatialReferences.getWgs84()));
        }
        Polyline polyline       = new Polyline(pointCollection);
        Graphic graphicRoute    = new Graphic(polyline, line);

        ListenableList<Graphic> graphicList = graphicsOverlayDirection.getGraphics();
        graphicList.add(graphicRoute);
    }
}