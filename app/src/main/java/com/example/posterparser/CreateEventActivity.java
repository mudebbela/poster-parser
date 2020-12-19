package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.room.Room;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.joestelmach.natty.*;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    private Button btnMockEvent;
    private String photoPathString;
    private Bitmap bitmap;
    private RadioGroup rgDescription;
    private RadioGroup rgTitle;
    private RadioGroup rgStartDate, rgEndDate;


    ImageView ivPoster;
    private TextRecognizer textRecognizer;
    private String TAG;
    private static final String COLON_SEPARATOR = ":";
    private static final String IMAGE = "image";
    private int rotation;
    private TreeMap<Long, String> stringTreeMap;
    private int increment;
    private Parser parser;
    private Button btnCreateEvent, btnSaveSession;

    private String titleString, descriptionString;
    private Date eventStartDate, eventEndDate;
    private DateFormat df;
    private boolean isRequestSavedImages;
    private String uriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TAG =  this.getLocalClassName();

        PPDatabase db = Room.databaseBuilder(getApplicationContext(), PPDatabase.class, "Poster-Parser").build();
        final EventDao ed = db.eventDao();
        //get intent and parse created activity
        Intent intent = getIntent();
        uriString =  intent.getStringExtra(PPConstants.URI);
        isRequestSavedImages = intent.hasExtra(PPConstants.IS_REQUEST_SAVED_IMAGE);

        textRecognizer = TextRecognition.getClient();
        stringTreeMap =  new TreeMap();
        parser = new Parser();
        increment = 0;
        df =  new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");



        final Uri bitmapUri = Uri.parse(uriString);
        Log.d(TAG, "onCreate: URI String: "+ uriString);
        rotation = getRotation(bitmapUri);
        PPutils.toast(getApplicationContext(),"rotation " +rotation);

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
        rgStartDate     = findViewById(R.id.radioGroupStartDate);
        rgEndDate     = findViewById(R.id.radioGroupEndDate);

        btnSaveSession =  findViewById(R.id.buttonSaveSession);
        btnCreateEvent = findViewById(R.id.buttonCreatEvent);


        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: clicked ");
                EventBuilder eb =  new EventBuilder();
                Intent createEventIntent = eb.setName(titleString)
                        .setDescription(descriptionString)
                        .setStartDate(eventStartDate)
                        .setEndDate(eventEndDate)
                        .build();
                startActivity(createEventIntent);
            }
        });

        btnSaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EventEntity ee =  new EventEntity();
                ee.imageUrl = bitmapUri.toString();
                //Create Local File to store saved image
                if(isRequestSavedImages){
                    Log.d(TAG, "onClick: Saving Requested Image");
                    try {
                        File imageFile =PPutils.createImageFile(getApplicationContext());
                        FileOutputStream ofs =  new FileOutputStream(imageFile.getAbsoluteFile());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ofs);
                        ee.imageUrl = PPutils.getUriForFile(imageFile, getApplicationContext());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Date now = new Date();
                ee.timestamp = now.getTime();
                ee.uid = (int) now.getTime();
                
                Thread t =  new Thread(){
                    public void run(){
                        ed.insertAll(ee);
                        Log.d(TAG, "run: Event Saved");
                    }
                };

                t.start();
                
            }
        });
        Log.d("Setting IMG", "Setting Image:  \""+ bitmapUri+"\"");
        PPutils.setImagetoView(uriString, ivPoster);
        //Parse
        parseImage(bitmap);


    }

    private String getInternalFileUrl(String bitmapUriString) {
        //create local file
        File image = PPutils.createImageFile(getApplicationContext());
        String imageUriString = image.getAbsolutePath();
        //copy uri File into it
        PPutils.saveFile(bitmapUriString, imageUriString);

        //return new Uri string

        Uri imageUri = FileProvider.getUriForFile(this,
                "com.example.posterparser",
                image);

        return imageUri.toString();
    }

    private int getRotation(Uri bitmapUri) {
        File image =  new File(bitmapUri.getPath());
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ExifInterface exif =  new ExifInterface(image.getAbsoluteFile());

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                PPutils.toast(getApplicationContext(),"rotation " +orientation);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    default:
                        PPutils.toast(getApplicationContext(), "Orientation found:" + orientation);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void parseImage(Bitmap bitmap) {
        //get bitmap
        Log.d(this.getLocalClassName(), "parseImage: Parsing Image");
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        Task<Text> result =
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    long size = getSize(block);
                                    stringTreeMap.put(size, block.getText());
                                }
                                updateRadioButtons(stringTreeMap);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        e.printStackTrace();

                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Text>() {
                    @Override
                    public void onComplete(@NonNull Task<Text> task) {

                    }
                });
        //result.getResult();
        //put textx into Title and Descrip
    }

    private void updateRadioButtons(TreeMap<Long, String> stringTreeMap) {
        Iterator it = stringTreeMap.entrySet().iterator();


        for(Map.Entry<Long, String> entry: stringTreeMap.entrySet()){
            String value = entry.getValue();


            final RadioButton rbTitle =  new RadioButton(getApplicationContext());
            rbTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setTitleString(rbTitle.getText());
                }
            });
            final RadioButton rbDescription =  new RadioButton(getApplicationContext());

            rbDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDescriptionString(rbDescription.getText());
                }
            });

            rbTitle.setText(value);
            rbDescription.setText(value);
            rgTitle.addView(rbTitle);
            rgDescription.addView(rbDescription);

            parseAndAddDates(value);

        }
    }

    private void setTitleString(CharSequence text) {
        titleString = text.toString();
        Log.d(TAG, "setTitleString: Title set to :" +titleString);
    }

    private void setDescriptionString(CharSequence text) {
        descriptionString = text.toString();
        Log.d(TAG, "setTitleString: Description set to :" +descriptionString);
    }

    private void parseAndAddDates(String value) {
        value = value.replace("\n", " ").trim();
        Log.d(TAG, "parseAndAddDates: value: \"" +value+"\"");
        if(value ==  null)
            return;
        try{
            List<DateGroup> groups = parser.parse(value);
            if(groups.isEmpty()){
                Log.d(TAG, "parseAndAddDates: Skipping empty group");
            }

            for(DateGroup group: groups){
                List<Date> dates =  group.getDates();

                for(Date date: dates){
                    final RadioButton rbStartDate =  new RadioButton(getApplicationContext());
                    rbStartDate.setText(date.toString());

                    rbStartDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setEventStartDate(rbStartDate.getText());

                        }
                    });
                    rgStartDate.addView(rbStartDate);

                    final RadioButton rbEndDate =  new RadioButton(getApplicationContext());
                    rbEndDate.setText(date.toString());

                    rbEndDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setEventEndDate(rbEndDate.getText());

                        }
                    });
                    rgEndDate.addView(rbEndDate);
                    Log.d(TAG, "parseAndAddDates: value parsed");

                }
            }
            Log.d(TAG, "successfully parsed "+value);
        }catch(Exception e){
            Log.d(TAG, "parseAndAddDates: Date Parser Exception... gracefully exit: "+e.getMessage());
            e.printStackTrace();


        }


    }

    private void setEventStartDate(CharSequence text) {
        try {
            eventStartDate = df.parse(text.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setEventEndDate(CharSequence text) {
        try {
            eventEndDate = df.parse(text.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private long getSize(Text.TextBlock block) {
        //get Size of block
        return increment++;
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