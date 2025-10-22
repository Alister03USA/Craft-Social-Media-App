package com.example.androidexample;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileUtils {
    public static byte[] getFileDataFromUri(Context ctx, Uri uri) {
        try {
            ContentResolver cr = ctx.getContentResolver();
            InputStream is = cr.openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
            is.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
}