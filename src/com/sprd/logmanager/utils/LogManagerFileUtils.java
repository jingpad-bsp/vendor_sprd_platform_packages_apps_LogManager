package com.sprd.logmanager.utils;

import java.io.*;


import android.util.Log;

public class LogManagerFileUtils {
    private static final String TAG = "FileUtils";

    public static void write(String path, String content ,boolean flag) {;

        try {
            File f = new File(path);
            if (f.exists()) {
                Log.d(TAG,"File is exists");
                FileWriter writer = new FileWriter(path, flag);
                writer.write(content);
                writer.close();
            } else {
                Log.d(TAG,"file is not exist, creating...");
                if (f.createNewFile()) {
                    Log.d(TAG,"creat success");
                    FileWriter writer = new FileWriter(path, flag);
                    writer.write(content);
                    writer.close();
                } else {
                    Log.d(TAG,"creat fail");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newFolder(String folderPath){
        String filePath = folderPath.toString();
        java.io.File myFilePath = new java.io.File(filePath);
        try{
            if(myFilePath.isDirectory()){
                Log.d(TAG,"the directory is exists!");
            }else{
                myFilePath.mkdir();
                Log.d(TAG,"create directory success");
            }
        }catch(Exception e)
        {
            Log.d(TAG,"create directory fail");
            e.printStackTrace();
        }
    }

    public static boolean deleteFolder(String _filePath){
        java.io.File folder = new java.io.File(_filePath);
        boolean  result  =   false ;
        try {
            String childs[] = folder.list();
            if(childs == null || childs.length <= 0){
                if (folder.delete()){
                    result  =   true ;
                }
            } else {
                for( int i = 0 ; i < childs.length; i ++ ){
                    String  childName = childs[i];
                    String  childPath = folder.getPath() + File.separator + childName;
                    File  filePath = new File(childPath);
                    if(filePath.exists() && filePath.isFile())    {
                        if (filePath.delete()) {
                            result  =   true ;
                        }else {
                            result  =   false ;
                            break ;
                        }
                    }else if(filePath.exists() && filePath.isDirectory()){
                        if (deleteFolder(filePath.toString())) {
                            result  =   true ;
                        }else {
                            result  =   false ;
                            break ;
                        }
                    }
                }
            }
            folder.delete();
        } catch (Exception e) {
            e.printStackTrace();
            result  =   false ;
        }
        return  result;
    }

    public static boolean isFileDirExits(String folderPath){
        String filePath = folderPath.toString();
        java.io.File myFilePath = new java.io.File(filePath);
        if (myFilePath.isDirectory()){
            return true;
        } else {
            return false;
        }
    }
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    /* SPRD bug 831726/833673:Delete ylog Directory except poweron dir. */
    public static void deleteByFilter(File file, String filterFileName) {
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (file.getAbsolutePath().equals(filterFileName)) {
                return;
            }
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteByFilter(childFiles[i], filterFileName);
            }
            file.delete();
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
    }



}
