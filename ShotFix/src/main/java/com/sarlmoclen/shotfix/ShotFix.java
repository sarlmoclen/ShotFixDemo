package com.sarlmoclen.shotfix;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class ShotFix {

    private static final String DEX_SUFFIX = ".dex";
    private static final String DEX_DIRECTORY = "dex_directory";
    private static final String UNZIP_DEX_DIRECTORY = "unzip_dex_directory";
    private static final String DEX_ELEMENTS = "dexElements";
    private static final String PATHLIST = "pathList";

    /**
     * 获取存放dex补丁包目录路径
     * 备注：外部存储data/data/包名/files/dex_directory，此目录不需要用户授权
     * */
    public static String getDexDirectory(Context context){
        return  context.getExternalFilesDir(DEX_DIRECTORY).getAbsolutePath();
    }

    /**
     * 获取dex补丁包临时解压目录路径
     * 备注：内部存储data/data/包名/files/unzip_dex_directory，api要求必须在放在内部存储下
     * */
    private static String getOptimizedDirectory(Context context){
        return  context.getFilesDir().getAbsolutePath() + File.separator + UNZIP_DEX_DIRECTORY;
    }

    /**
     * 热更新
     */
    public static void hotFix(Context context) {
        if (context == null) {
            return;
        }
        File fileDexDirectory = new File(getDexDirectory(context));
        if(!fileDexDirectory.exists()){
            fileDexDirectory.mkdirs();
            return;
        }
        File[] listDexFiles = fileDexDirectory.listFiles();
        String dexPath = null;
        for (File file : listDexFiles) {
            if (file.getName().endsWith(DEX_SUFFIX)) {
                dexPath = file.getAbsolutePath() + ":";
            }
        }
        if(TextUtils.isEmpty(dexPath)){
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            loadDex(context, dexPath);
        } 
    }

    /**
     * 加载dex
     */
    private static void loadDex(Context context, String dexPath) {
        File fileOptimizedDirectory = new File(getOptimizedDirectory(context));
        if (!fileOptimizedDirectory.exists()) {
            fileOptimizedDirectory.mkdirs();
        }
        try {
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            DexClassLoader dexClassLoader = new DexClassLoader(
                    dexPath,
                    fileOptimizedDirectory.getAbsolutePath(),
                    null,
                    pathClassLoader
            );
            combineDex(pathClassLoader,dexClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 合并dex
     */
    private static void combineDex(PathClassLoader pathClassLoader, DexClassLoader dexClassLoader)
            throws IllegalAccessException, NoSuchFieldException {
        Object[] pathDexElements = getDexElements(getPathList(pathClassLoader));
        Object[] dexDexElements = getDexElements(getPathList(dexClassLoader));
        Object[] combined = combineElements(dexDexElements, pathDexElements);
        setDexElements(getPathList(pathClassLoader),combined);
    }


    /**
     * 获取pathList
     */
    private static Object getPathList(Object classLoader)
            throws NoSuchFieldException, IllegalAccessException {
        return ShotFixUtils.getField(classLoader
                , PATHLIST);
    }

    /**
     * 获取dexElements
     */
    private static Object[] getDexElements(Object pathList )
            throws NoSuchFieldException, IllegalAccessException {
        return (Object[])ShotFixUtils.getField(pathList
                , DEX_ELEMENTS);
    }

    /**
     * 设置dexElements
     */
    private static void setDexElements(Object pathList, Object dexElements)
            throws NoSuchFieldException, IllegalAccessException {
        ShotFixUtils.setField(pathList
                , DEX_ELEMENTS
                , dexElements);
    }
    /**
     * 数组合并
     */
    private static Object[] combineElements(Object[] dexDexElements, Object[] pathDexElements) {
        Object[] combined = (Object[]) Array.newInstance(dexDexElements.getClass().getComponentType()
                , dexDexElements.length + pathDexElements.length);
        System.arraycopy(dexDexElements, 0, combined, 0, dexDexElements.length);
        System.arraycopy(pathDexElements, 0, combined, dexDexElements.length, pathDexElements.length);
        return combined;
    }

}
