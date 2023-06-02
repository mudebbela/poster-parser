package com.example.posterparser;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.google.mlkit.vision.text.Text;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.posterparser.MainActivity.TAG;

public class PPutils {
    public static void toast(Context applicationContext, String text) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show();
    }

    public static void setImagetoView(String imageUrl, int default_image, ImageView userImage) {
        Picasso.get().load(imageUrl).fit()
                .placeholder(default_image).into(userImage);
    }

    public static void setImagetoView(String imageUrl, int default_image, ImageView userImage, int rotation) {
        Picasso.get().load(imageUrl).fit()
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

    public static long getSize(Text.TextBlock block) {
        //get Size of block
        Rect box = block.getBoundingBox();
        int numOfLetters = block.getText().length();
        int height = box.top - box.bottom;
        int width = box.left -  box.right;
        return height * width/numOfLetters;
    }

    public static String getUriForFile(File imageFile, Context ctx){

        Uri imageUri = FileProvider.getUriForFile(ctx,
                "com.example.posterparser",
                imageFile);
        return  imageUri.toString();
    }

    //From https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore?rq=1
    // will return the absolute file given the uri
    public static String getRealPathFromURI(Uri contentUri, Context ctx) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(ctx, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public static int getRotation(String fullImagePath) {
        try {
            ExifInterface current =  new ExifInterface(fullImagePath);
            int orientationFlag  =current.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Log.d(TAG, "getRotation: orientationFlag : "+orientationFlag);
            switch (orientationFlag){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
