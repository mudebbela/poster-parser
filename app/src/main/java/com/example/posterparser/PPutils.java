package com.example.posterparser;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PPutils {
    public static void toast(Context applicationContext, String text) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show();
    }

    public static void setImagetoView(String imageUrl, int default_image, ImageView userImage) {
        Log.d("TAG", "setImagetoView: \""+imageUrl+"\"");
        Picasso.get().load(imageUrl)
                .placeholder(default_image).into(userImage);
    }
    public static void setImagetoView(String imageUrl, int default_image, ImageView userImage, int rotation) {
        Log.d("TAG", "setImagetoView: \""+imageUrl+"\"");
        Picasso.get().load(imageUrl)
                .placeholder(default_image)
                .rotate(rotation).into(userImage);
    }

    public static void setImagetoView(String imageUrl, ImageView userImage) {
        setImagetoView(imageUrl, R.drawable.default_poster, userImage);
    }

    public static void setImagetoView(String imageUrl, ImageView userImage, int rotation) {
        setImagetoView(imageUrl, R.drawable.default_poster, userImage, rotation);
    }
    public static void setImagetoView(String message, ImageView iv, int x, int y) {
        Picasso.get().load(message).placeholder(R.drawable.default_poster).resize(x, y).onlyScaleDown().into(iv);
    }

    public static File createImageFile(Context ctx) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(ctx.getClass().getName(), "createImageFile: absolutePath: "+image.getAbsolutePath());
        return image;
    }

    //shoutout to https://stackoverflow.com/questions/13133579/android-save-a-file-from-an-existing-uri
    // for helping me figure this out
    public static void saveFile(String sourceFilename, String destinationFilename) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                Log.d(PPutils.class.getName(), "Error saving to file "+ destinationFilename +"\n"+e.getMessage());
                e.printStackTrace();
            }
        }
        Log.d(PPutils.class.getName(), "saveFile: create file "+ destinationFilename);
    }

    public static String getRealPathFromURI(Uri contentURI, Context ctx) {
        String result;
        Cursor cursor = ctx.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
