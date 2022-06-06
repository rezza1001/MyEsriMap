package com.vma.testmapesri;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.location.Location;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.esri.arcgisruntime.geometry.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class Utility {
    private static String TAG = "Utility";
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    public static float dpToPx(Context context, double valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) valueInDp, metrics);
    }


    public static int dpToPx(Context context, int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public static int getPixelValue(Context context, int dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
    }

    public static Date getDate(String pDate){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return format.parse(pDate.replace("T"," ").split("\\.")[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
    public static Date getDate(String pDate, String inputFormat){
        DateFormat format = new SimpleDateFormat(inputFormat, Locale.getDefault());
        try {
            return format.parse(pDate.replace("T"," ").split("\\.")[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getDateString(Date date, String sFormat){
        DateFormat format = new SimpleDateFormat(sFormat, Locale.getDefault());
        return format.format(date);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity, EditText editText){
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static ShapeDrawable getRectBackground(String color_code, int radius){
        if (color_code.startsWith("#")){
            color_code =  color_code.replace("#","");
        }
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{
                radius, radius, radius, radius,
                radius, radius, radius, radius}, null, null);

        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(Color.parseColor("#"+color_code));
        return shapeDrawable;
    }
    public static ShapeDrawable getRectBackground(int color, int radius){
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{
                radius, radius, radius, radius,
                radius, radius, radius, radius}, null, null);

        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
    public static ShapeDrawable getRectBackground(String color_code, int leftTop, int rightTop, int rightBottom, int leftBottom){
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{
                leftTop, leftTop, rightTop, rightTop,
                rightBottom, rightBottom, leftBottom ,leftBottom}, null, null);

        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(Color.parseColor("#"+color_code));
        return shapeDrawable;
    }
    public static ShapeDrawable getRectBackground(int color, int leftTop, int rightTop, int rightBottom, int leftBottom){
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{
                leftTop, leftTop, rightTop, rightTop,
                rightBottom, rightBottom, leftBottom ,leftBottom}, null, null);

        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    public static Drawable getRectBackground(int color_code, int linesize, int linecolor, int leftTop, int rightTop, int leftBottom, int rightBottom){
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadii(new float[] { leftTop, rightTop, leftBottom, rightBottom, 0, 0, 0, 0 });
        bg.setColor(color_code);
        bg.setStroke(linesize ,linecolor);
        return bg;
    }

    public static void showToastError(Context context, String message){
       Toast.makeText(context,message, Toast.LENGTH_LONG).show();
    }
    public static void showToastSuccess(Context context, String message){
        Toast.makeText(context,message, Toast.LENGTH_LONG).show();
    }

    private static final String[] ORIENTATIONS = "N/S/E/W".split("/");

    public static String processCoordinates(float lat, float lon) {
        Log.d(TAG,"Lat ="+ lat+" Lan ="+ lon);
        float[] coordinates = {lat, lon};
        String converted0 = decimalToDMS(coordinates[1]);
        final String dmsLat = coordinates[0] > 0 ? ORIENTATIONS[0] : ORIENTATIONS[1];
        converted0 = dmsLat.concat(" ").concat(converted0);

        String converted1 = decimalToDMS(coordinates[0]);
        final String dmsLng = coordinates[1] > 0 ? ORIENTATIONS[2] : ORIENTATIONS[3];
        converted1 = dmsLng.concat(" ").concat(converted1);

        return converted0.concat("\n").concat(converted1);
    }

    private static String decimalToDMS(float coord) {
        float mod = coord % 1;
        int intPart = (int) coord;
        String degrees = String.valueOf(intPart);

        coord = mod * 60;
        mod = coord % 1;
        intPart = (int) coord;
        if (intPart < 0)
            intPart *= -1;

        String minutes = String.valueOf(intPart);

        coord = mod * 60;
        intPart = (int) coord;
        if (intPart < 0)
            intPart *= -1;

        String seconds = String.valueOf(intPart);

        return Math.abs(Integer.parseInt(degrees)) + "°" + minutes + "'" + seconds + "\"";
    }

    public static boolean hasPermission(Activity activity, String[] permissions){
        Utility.logDbug("Utility"," hasPermissions with request "+permissions.length);
        if(!hasPermissions(activity, permissions)){
            ActivityCompat.requestPermissions(Objects.requireNonNull(activity), permissions, 32);
            return false;
        }
        else {
            return true;
        }
    }

    public static boolean checkPermission(Activity activity, String[] permissions){
        return hasPermissions(activity, permissions);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                Utility.logDbug("Utility",permission+" check");
                if (permission == null){
                    continue;
                }
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Utility.logDbug("Utility",permission+" hasPermissions false");
                    return false;
                }
            }
        }
        Utility.logDbug("Utility","hasPermissions true");
        return true;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getApplicationContext().getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }


    public static String getStringFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    public static String getStringFromFile(String path) {
        String jsonString;
        try {
            InputStream is = new FileInputStream(path);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    public static Drawable getImageFromAssets(Context context, String folder, String fileName){
        Drawable d = null;
        try {
            String path = folder+"/"+fileName;
            Log.d(TAG,"Load image from asset "+path);
            InputStream is = context.getAssets().open(path);
            d = Drawable.createFromStream(is, null);
            is.close();
        } catch (IOException e) {
           Log.e(TAG,e.getMessage());
        }
        return d;
    }


    public static ShapeDrawable getOvalBackground(String color_code){
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(Color.parseColor("#"+color_code));
        return shapeDrawable;
    }

    public static ShapeDrawable getOvalBackground(int color){
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    public static Calendar getLocalTime(Date date){
        SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            String dateStr = readDate.format(date);
            readDate.setTimeZone(TimeZone.getTimeZone("GMT+07:00"));
            Date date2 = readDate.parse(dateStr);
            readDate.setTimeZone(TimeZone.getDefault());
            String mDate = readDate.format(date2);
            Log.d("Utility","TIME "+ mDate);

            String h = mDate.split(" ")[1].split(":")[0];
            String m = mDate.split(" ")[1].split(":")[1];
            String s = mDate.split(" ")[1].split(":")[2];
            calendar.setTime(date2);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
            calendar.set(Calendar.MINUTE, Integer.parseInt(m));
            calendar.set(Calendar.SECOND, Integer.parseInt(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static float getDistance(Point pos1, Point pos2 ) {
        return calDistance(pos1.getY(), pos1.getX(), pos2.getY(), pos2.getX());
    }

    public static Float calDistance(double lat1, double lon1, double lat2, double lon2){
        float distance;

        Location point1 = new Location("locationA");
        point1.setLatitude(lat1);
        point1.setLongitude(lon1);

        Location point2 = new Location("locationB");
        point2.setLatitude(lat2);
        point2.setLongitude(lon2);

        distance = point1.distanceTo(point2);
        distance /= 1852.0;
        Log.d("Utility","calDistance location1 ("+lat1+","+lon1+") | location2 ("+lat2+","+lon2+") = "+ distance);

        return distance;
    }

    public static void logDbug(String value){
        Log.d("DBUG_RZ", value);
    }

    public static void logDbug(String tag, String value){
        Log.d(tag, value);
    }
    public static void logService(String value){
        Log.d("SERVICE_RZ", value);
    }
}
