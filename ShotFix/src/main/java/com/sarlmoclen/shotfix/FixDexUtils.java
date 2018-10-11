package com.sarlmoclen.shotfix;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixDexUtils {

    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "dex_dir";
    private static final String UNZIP_DEX_DIR = "unzip_dex_dir";

    /**
     * 加载补丁
     */
    public static void loadFixedDex(Context context) {
        if (context == null) {
            return;
        }
        //获取DEX_DIR目录，这个目录存放补丁文件
        File fileDir = new File(context.getExternalFilesDir(DEX_DIR).getAbsolutePath());
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        //遍历所有的文件
        File[] listFiles = fileDir.listFiles();
        HashSet<File> loadedDex = new HashSet<>();
        for (File file : listFiles) {
            if (file.getName().startsWith("classes") &&
                    (file.getName().endsWith(DEX_SUFFIX)
                            || file.getName().endsWith(APK_SUFFIX)
                            || file.getName().endsWith(JAR_SUFFIX)
                            || file.getName().endsWith(ZIP_SUFFIX))) {
                // 存入集合
                loadedDex.add(file);
            }
        }
        // dex合并之前的dex
        doDexInject(context, loadedDex);
    }

    private static void doDexInject(Context context, HashSet<File> loadedDex) {
        // data/data/包名/files/optimize_dex（这个必须是自己程序下的目录）
        String optimizeDir = context.getFilesDir().getAbsolutePath() + File.separator + UNZIP_DEX_DIR;
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        try {
            // 1.加载应用程序的dex
            PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();
            for (File dex : loadedDex) {
                // 2.加载指定的修复的dex文件
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),// 修复好的dex（补丁）所在目录
                        fopt.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,// 加载dex时需要的库
                        pathLoader// 父类加载器
                );
                // 3.合并
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(pathLoader);
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);
                // 合并完成
                Object dexElements = combineArray(leftDexElements, rightDexElements);
                // 重写给PathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(pathLoader);// 一定要重新获取，不要用pathPathList，会报错
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    /**
     * 反射得到类加载器中的pathList对象
     */
    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射得到pathList中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 数组合并
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> componentType = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);// 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);// 得到原dex数组长度
        int k = i + j;// 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(componentType, k);// 创建一个类型为componentType，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }

}
