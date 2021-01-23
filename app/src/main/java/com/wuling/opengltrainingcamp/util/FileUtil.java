package com.wuling.opengltrainingcamp.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * @Author: huang xiao xian
 * @Date: 2021/1/18
 * @Des:
 */
public class FileUtil {

    public static Bitmap getBitmapFromAsset(Context context, String path) {
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}