package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.joestelmach.natty.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CreateEventActivity extends AppCompatActivity implements DateTimePickerDialogFragment.NoticeDialogListener {

    //UI object refrences
    private Bitmap bitmap;
    private RadioGroup rgTitle, rgDescription;
    private RadioGroup rgStartDate, rgEndDate;
    private Button btnStartDateTimePicker;
    private ImageView ivPoster;

    private Button btnCreateEvent, btnSaveSession;


    //Parser  and Parser assisting objects
    private TextRecognizer textRecognizer;
    private int rotation;
    private TreeMap<Long, String> parsedDataTreeMap;
    private Parser parser;

    //parsing session Data
    private String selectedTitleString, selectedDescriptionString;
    private Date selectedEventStartDate, selectedEventEndDate;
    private DateFormat df;

    //Intent objects
    private boolean isFromSavedImage;
    private String uriString;

    private String TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TAG =  this.getLocalClassName();


        ivPoster                = findViewById(R.id.ImageViewPoster);
        rgDescription           = findViewById(R.id.RadioGroupDescription);
        rgTitle                 = findViewById(R.id.RadioGroupTitle);
        rgStartDate             = findViewById(R.id.radioGroupStartDate);
        rgEndDate               = findViewById(R.id.radioGroupEndDate);
        btnSaveSession          = findViewById(R.id.buttonSaveSession);
        btnCreateEvent          = findViewById(R.id.buttonCreatEvent);
        btnStartDateTimePicker  = findViewById(R.id.buttonStartDateTimePicker);



        //get intent and parse created activity
        Intent intent       = getIntent();
        uriString           =  intent.getStringExtra(PPConstants.URI);
        isFromSavedImage    = intent.hasExtra(PPConstants.SAVE_IMAGE_FLAG);
        rotation            = intent.getIntExtra(PPConstants.IMAGE_ROTATION, 0);


        Log.d("Setting IMG", "Setting Image:  \""+ uriString+"\"");
        PPutils.setImagetoView(uriString, ivPoster, rotation);

//TODO cleaner date logic
        //use relative date Time logic
        //https://developer.android.com/reference/android/icu/text/RelativeDateTimeFormatter
        df                  = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        parser              = new Parser();
        parsedDataTreeMap   = new TreeMap();
        textRecognizer      = TextRecognition.getClient();

        final Uri bitmapUri = Uri.parse(uriString);
        Log.d(TAG, "onCreate: Rotation: "+rotation );
        try {
            InputStream stream = getContentResolver().openInputStream(
                    bitmapUri);
            bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        //TODO delete session if succesfully saved
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: clicked ");
                EventBuilder eb =  new EventBuilder();

                //TODO handle null dates
                //maybe never have null dates
                //at least never have null starDates, End date optional
                Intent createEventIntent = eb.setName(selectedTitleString)
                        .setDescription(selectedDescriptionString)
                        .setStartDate(selectedEventStartDate)
                        .setEndDate(selectedEventEndDate)
                        .build();
                startActivity(createEventIntent);
            }
        });

        btnSaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EventEntity ee =  new EventEntity();
                ee.imageUrl = bitmapUri.toString();
                //If from a saved image
                //Create an accesible file to store a copy of image
                if(isFromSavedImage){
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
                ee.rotation = rotation;

                //thread to save the image in
                Thread t =  new Thread(){
                    public void run(){
                        PPDatabase db = Room.databaseBuilder(getApplicationContext(), PPDatabase.class, "Poster-Parser").build();
                        final EventDao ed = db.eventDao();
                        ed.insertAll(ee);
                        Log.d(TAG, "run: Event Saved");
                    }
                };

                t.start();
                
            }
        });

        btnStartDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create DateTime Picker Dialogue
                DialogFragment dialog = DateTimePickerDialogFragment.newInstance(CreateEventActivity.this);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        //Parse
        parseImage(bitmap);


    }

    private void parseImage(Bitmap bitmap) {
        //get bitmap
        Log.d(this.getLocalClassName(), "parseImage: Parsing Image");
        InputImage inputImage = InputImage.fromBitmap(bitmap, rotation);

        textRecognizer.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            long size = PPutils.getSize(block);
                            parsedDataTreeMap.put(size, block.getText());
                        }
                        updateRadioButtons(parsedDataTreeMap);
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
    }

    private void updateRadioButtons(TreeMap<Long, String> stringTreeMap) {

        for(Map.Entry<Long, String> entry: stringTreeMap.descendingMap().entrySet()){
            String value = entry.getValue();


            final RadioButton rbTitle =  new RadioButton(getApplicationContext());
            rbTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setSelectedTitleString(rbTitle.getText());
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

            updateDates(value);

        }
    }

    private void setSelectedTitleString(CharSequence text) {
        selectedTitleString = text.toString();
        Log.d(TAG, "setTitleString: Title set to :" + selectedTitleString);
    }

    private void setDescriptionString(CharSequence text) {
        selectedDescriptionString = text.toString();
        Log.d(TAG, "setTitleString: Description set to :" +selectedDescriptionString);
    }

    private void updateDates(String value) {
        value = value.replace("\n", " ").trim();
        Log.d(TAG, "updateDates: value: \"" +value+"\"");
        if(value ==  null)
            return;
        try{
            List<DateGroup> groups = parser.parse(value);
            if(groups.isEmpty()){
                Log.d(TAG, "updateDates: Skipping empty group");
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
                            setSelectedEventEndDate(rbEndDate.getText());

                        }
                    });
                    rgEndDate.addView(rbEndDate);
                    Log.d(TAG, "updateDates: value parsed");

                }
            }
            Log.d(TAG, "successfully parsed "+value);
        }catch(Exception e){
            Log.d(TAG, "updateDates: Date Parser Exception... gracefully exit: "+e.getMessage());
            e.printStackTrace();


        }


    }

    private void setEventStartDate(CharSequence text) {
        try {
            selectedEventStartDate = df.parse(text.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setSelectedEventEndDate(CharSequence text) {
        try {
            selectedEventEndDate = df.parse(text.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogButtonSetDateClick(long longDate) {
        PPutils.toast(getApplicationContext(), "Set Date picked: "+longDate);

    }

    @Override
    public void onDialogCancelDateClick(DialogFragment dialog) {
        PPutils.toast(getApplicationContext(),"Set Date cancelled");
    }
}