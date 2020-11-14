package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.TreeMap;

public class CreateEventActivity extends AppCompatActivity {

    private Button btnMockEvent;
    private String photoPathString;
    private Bitmap bitmap;
    private RadioGroup rgDescription;
    private RadioGroup rgTitle;

    ImageView ivPoster;
    private TextRecognizer textRecognizer;
    private String TAG;
    private static final String COLON_SEPARATOR = ":";
    private static final String IMAGE = "image";
    private int rotation;
    private TreeMap stringTreeMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TAG =  this.getLocalClassName();
        //get intent and parse created activity
        Intent intent = getIntent();
        textRecognizer = TextRecognition.getClient();
        stringTreeMap =  new TreeMap<Long, String>();
        btnMockEvent = findViewById(R.id.buttonMockEvent);


//        photoPathString = intent.getStringExtra(PPConstants.IMAGE_PATH_CONSTANT);
//        bitmap =  intent.getParcelableExtra(PPConstants.BITMAP_CONSTANT);

        //getURI and get bitmap
        String uriString =  intent.getStringExtra(PPConstants.URI);
        Uri bitmapUri = Uri.parse(uriString);
        //rotation = getImageRotationDegrees(bitmapUri);
        rotation = 1;
        try {
            InputStream stream = getContentResolver().openInputStream(
                    bitmapUri);
            bitmap = BitmapFactory.decodeStream(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ivPoster        =  findViewById(R.id.ImageViewPoster);
        rgDescription   = findViewById(R.id.RadioGroupDescription);
        rgTitle         = findViewById(R.id.RadioGroupTitle);

        btnMockEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBuilder eb = new EventBuilder();
                Intent testEventIntent = eb.setName("Mock Title")
                        .setDescription("This is a Mock Desription for this event to test the Event Builder")
                        .setEndDate(new Date())
                        .setStartDate(new Date())
                        .build();
                startActivity(testEventIntent);
            }
        });

        ivPoster.setImageBitmap(bitmap);

        //Parse
        parseImage(bitmap);


    }

    private void parseImage(Bitmap bitmap) {
        //get bitmap
        Log.d(this.getLocalClassName(), "parseImage: Parsing Image");
        InputImage inputImage = InputImage.fromBitmap(bitmap, rotation);

        Task<Text> result =
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                Log.d(this.getClass().getName(), "onSuccess: found "+visionText.getText());
                                Toast.makeText(CreateEventActivity.this, visionText.getText(), Toast.LENGTH_SHORT).show();



                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

        //put textx into Title and Descrip
    }
    public int getImageRotationDegrees(@NonNull Uri imgUri) {
        int photoRotation = ExifInterface.ORIENTATION_UNDEFINED;

        try {
            boolean hasRotation = false;
            //If image comes from the gallery and is not in the folder DCIM (Scheme: content://)
            String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
            Cursor cursor = getContentResolver().query(imgUri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.getColumnCount() > 0 && cursor.moveToFirst()) {
                    photoRotation = cursor.getInt(cursor.getColumnIndex(projection[0]));
                    hasRotation = photoRotation != 0;
                    Log.d(TAG, "getImageRotationDegrees: ");
                }
                cursor.close();
            }

            //If image comes from the camera (Scheme: file://) or is from the folder DCIM (Scheme: content://)
            if (!hasRotation) {
                ExifInterface exif = new ExifInterface(getAbsolutePath(imgUri));
                int exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                switch (exifRotation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: {
                        photoRotation = 90;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_180: {
                        photoRotation = 180;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_270: {
                        photoRotation = 270;
                        break;
                    }
                }
                Log.d(TAG, "Exif orientation: "+ photoRotation);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error determining rotation for image"+ imgUri, e);
        }
        return photoRotation;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getAbsolutePath(Uri uri) {
        //Code snippet edited from: http://stackoverflow.com/a/20559418/2235133
        String filePath = uri.getPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            // Will return "image:x*"
            String[] wholeID = TextUtils.split(DocumentsContract.getDocumentId(uri), COLON_SEPARATOR);
            // Split at colon, use second item in the array
            String type = wholeID[0];
            if (IMAGE.equalsIgnoreCase(type)) {//If it not type image, it means it comes from a remote location, like Google Photos
                String id = wholeID[1];
                String[] column = {MediaStore.Images.Media.DATA};
                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = getContentResolver().
                        query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);
                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
                Log.d(TAG, "Fetched absolute path for uri" + uri);
            }
        }
        return filePath;
    }
}