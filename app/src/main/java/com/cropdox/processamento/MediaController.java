package com.cropdox.processamento;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class MediaController {
    private static final String GENIAL_LOG = "MediaController";
    private static Application applicationLoader = null;

    public MediaController(Application applicationLoader) {
        MediaController.applicationLoader = applicationLoader;
    }
    public static String fixFileName(String fileName) {
        if (fileName != null) {
            fileName = fileName.replaceAll("[\u0001-\u001f<>\u202E:\"/\\\\|?*\u007f]+", "").trim();
        }
        return fileName;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getFileName(Uri uri) {
        if (uri == null) {
            return "";
        }
        try {
            String result = null;
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = MediaController.applicationLoader.getApplicationContext().getContentResolver().query(uri,
                        new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception e) {
                    Log.e(GENIAL_LOG, String.valueOf(e));
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        } catch (Exception e) {
            Log.e(GENIAL_LOG, String.valueOf(e));
        }
        return "";
    }
}
