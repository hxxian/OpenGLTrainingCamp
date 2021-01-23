package com.wuling.opengltrainingcamp.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.wuling.opengltrainingcamp.common.GlobalContext;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class DeviceInfoUtil {

    private static final String TAG = "DeviceInfoUtil";

    /**
     * 获取手机运存
     *
     * @return 单位：G
     */
    public static int getDeviceMemory() {
        double memG = 0;
        try {
            ActivityManager manager = (ActivityManager) GlobalContext.context.getSystemService(Context.ACTIVITY_SERVICE);

            // 获取应用被系统默认分配的可用内存，单位: M
//            int memClass = manager.getMemoryClass();
            // 获取应用可被分配的最大可用内存，单位：M，AndroidManifest.xml设置android:largeHeap="true"可获取更大
//            int largeMemClass = manager.getLargeMemoryClass();

            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(memoryInfo);
            long totalMemory = memoryInfo.totalMem;
            //向上取整
            memG = Math.ceil(totalMemory / (float) (1024 * 1024 * 1024));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int) memG;
    }

    public static String getCpuName() {
        try (FileReader fr = new FileReader("/proc/cpuinfo");
             BufferedReader br = new BufferedReader(fr)) {
            String text;
            String last = "";
            while ((text = br.readLine()) != null) {
                last = text;
            }
            //一般机型的cpu型号都会在cpuinfo文件的最后一行
            if (last.contains("Hardware")) {
                String[] hardWare = last.split(":\\s+", 2);
                return hardWare[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Build.HARDWARE;
    }

    /**
     * 获取设备的真实分辨率，数值跟手机参数里的一样
     *
     * @param context
     * @return
     */
    public static Point getDeviceRealResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);

        return point;
    }

    /**
     * 获取屏幕显示内容像素宽度
     *
     * @param context
     * @return 屏幕显示内容像素宽度
     */
    public static int getScreenContentWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕显示内容像素高度（这个方法在有的手机上包含了导航栏的高度，有的手机上又没有包含）
     *
     * @param context
     * @return 屏幕显示内容像素高度
     */
    public static int getScreenContentHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 判断是否横屏
     *
     * @param context
     * @return 是否横屏
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        Resources resources = context.getResources();
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = resources.getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
            //获取status_bar_height资源的ID
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusHeight = resources.getDimensionPixelSize(resourceId);
            }
        }
        return statusHeight;
    }

    public static int dp2px(float dpValue) {
        if (GlobalContext.context == null) {
            return (int) dpValue;
        }
        final float scale = GlobalContext.context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    public static int px2dip(float pxValue) {
        final float scale = GlobalContext.context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(float spValue) {
        final float fontScale = GlobalContext.context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */

    public static int px2sp(float pxValue) {
        final float fontScale = GlobalContext.context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    private static float sNoncompatDensity;
    private static float sNoncompatScaleDensity;

    public static void setCustomDensity(Activity activity, final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();

        if (sNoncompatDensity == 0) {
            sNoncompatDensity = appDisplayMetrics.density;
            sNoncompatScaleDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sNoncompatScaleDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }

        // 这里需要扩展设计
        final float targetDensity = appDisplayMetrics.widthPixels / 360;
        final float targetScaledDensity = targetDensity * (sNoncompatScaleDensity / sNoncompatDensity);
        final int targetDensityDpi = (int) (160 * targetDensity);

        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        final DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

    /**
     * 获取设备型号
     *
     * @return
     */
    public static String getDeviceModel() {
        try {
            String model = Build.MODEL;
            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "test";
    }

    /**
     * 获取设备厂商
     *
     * @return
     */
    public static String getDeviceActurer() {
        try {
            String manufacturer = Build.MANUFACTURER;
            return manufacturer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "test";
    }

    /**
     * 获取虚拟按键栏高度
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (isNavigationBarShowing(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 判断底部导航栏是否显示
     *
     * @param context
     * @return
     */
    public static boolean isNavigationBarShowing(Context context) {
        //判断手机底部是否支持导航栏显示
        boolean haveNavigationBar = checkDeviceHasNavigationBar(context);
        if (haveNavigationBar) {
            if (Build.VERSION.SDK_INT >= 17) {
                String brand = Build.BRAND;
                String mDeviceInfo;
                if (brand.equalsIgnoreCase("HUAWEI")) {
                    mDeviceInfo = "navigationbar_is_min";
                } else if (brand.equalsIgnoreCase("XIAOMI")) {
                    mDeviceInfo = "force_fsg_nav_bar";
                } else if (brand.equalsIgnoreCase("VIVO")) {
                    mDeviceInfo = "navigation_gesture_on";
                } else if (brand.equalsIgnoreCase("OPPO")) {
                    mDeviceInfo = "navigation_gesture_on";
                } else {
                    mDeviceInfo = "navigationbar_is_min";
                }

                if (Settings.Global.getInt(context.getContentResolver(), mDeviceInfo, 0) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断手机是否存在导航栏
     *
     * @param context
     * @return
     */
    private static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }
}
