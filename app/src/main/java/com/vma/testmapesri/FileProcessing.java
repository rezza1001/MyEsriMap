package com.vma.testmapesri;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileProcessing {

    public static final String ROOT = "ESRIMAP";
    public static final String TAG = "FileProcessing";

    public static void init(Context context){
        Log.d(TAG,"INIT");
        String[] permission = new String[2];
        permission[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        permission[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            permission[2] = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
//
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }

        }

        boolean hasPermission = Utility.hasPermission((Activity) context, permission);
        Log.d(TAG,"Init permission "+hasPermission);
        if (hasPermission){
            FileProcessing.createFolder(context,FileProcessing.ROOT);
        }
        else {
            Utility.showToastError(context,"Can't access Permission for "+TAG+" ");
        }
    }

    public static File getMainPath(Context context){
        File mediaPath = Environment.getExternalStorageDirectory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mediaPath = context.getExternalFilesDir("");
        }
        return mediaPath;
    }

    public static String getRootPath(Context context){
        File mainFolder = getMainPath(context);
        return mainFolder.getAbsolutePath()+"/"+ROOT+"/";
    }

    public static File getDownloadDir(Context context){
        return  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static boolean createFolder(Context context, String path){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File root = new File(getMainPath(context).getAbsolutePath(), "/"+ROOT);

            if (!root.exists()){
                if(root.mkdirs()){
                    Log.d(TAG,"Create root "+ROOT+" Success");
                    return create(context,path);
                }
                else {
                    Log.d(TAG,"Create root "+ROOT+" Failed");
                    return false;
                }
            }
            else {
                path = ROOT+ "/"+path;
                return create(context,path);
            }
        }
        else {
            Log.d(TAG,"MEDIA_MOUNTED NOT ACCESS");
            return false;
        }
    }

    private static boolean create(Context context, String path){
        File file;
        file = new File(getMainPath(context).getAbsolutePath(), path);
        if(!file.exists()) {
            if(file.mkdirs()){
                Log.d(TAG,"createFolder "+path+" Success");
                return true;
            }
            else {
                Log.d(TAG,"createFolder "+path+" Failed");
                return false;
            }
        }
        else {
            Log.d(TAG,"Folder exist "+path+"");
            return true;
        }
    }

    public static boolean DeleteFile(String path, String name){
        String mediaPath = path+"/"+name;
        File media = new File(mediaPath);
        Log.d("FileProcessing","Deleted : "+ media.getPath()+" -> "+media.exists());
        if (media.exists()){
            return media.delete();
        }
        else {
            return false;
        }
    }

    public static boolean DeleteFolder(String path){
        File media = new File(path);
        Log.d("FileProcessing","Deleted : "+ media.getPath()+" -> "+media.exists());
        if (media.exists()){
            return media.delete();
        }
        else {
            return false;
        }
    }

    public static void Copy(File src, File dst) throws IOException {
        Log.d(TAG,"Copy exists : "+src.getAbsolutePath()+" = "+src.exists()+" to "+dst.getAbsolutePath());
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void WriteFileToLog(Context context, String folder, String fileName, String data) {
        File root = new File(getMainPath(context)+"/"+FileProcessing.ROOT+"/"+folder);
        if (!root.exists()){
            boolean created = root.mkdirs();
            Log.d(TAG,"Create Folder Log ("+root.getName()+") = "+created);
        }
        File logFile = new File(root.getAbsolutePath()+"/"+fileName);
        if (!logFile.exists()){
            try {
                boolean createFile =  logFile.createNewFile();
                Log.d(TAG,"Create new file Logs ("+logFile.getName()+") "+createFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(data);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
