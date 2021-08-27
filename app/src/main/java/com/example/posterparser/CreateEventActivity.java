package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CreateEventActivity extends AppCompatActivity implements DateTimePickerDialogFragment.NoticeDialogListener, PPDatePickerFragment.DateChangeListener, PPTimePickerFragment.TimeChangeListener {

    //UI object refrences
    private Bitmap bitmap;
    private RadioGroup rgTitle, rgDescription;
    private RadioGroup rgStartDate, rgEndDate;
    private Button btnStartDateTimePicker, btnEndDateTimePicker;
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

    //Date from dialog variables
    int customStartYear, customStartMonth, customStartDay;
    int customStartMinute,customStartHour;
    int customEndYear, customEndMonth, customEndDay;
    int customEndMinute,customEndHour;
    private boolean isDateChanged, isTimeChanged;
    private boolean isStartDateTimePicker;
    Date customEventStartDate, customEventEndDate;

    //Intent objects
    private boolean isFromSavedImage;
    private String uriString;

    private String TAG;
    private long timestamp;
    private EventEntity thisEd;
    private int uid;
    private PPDatabase db;
    private EventDao ed;
    private RadioButton rbCustomStartDate, rbCustomEndDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TAG =  this.getLocalClassName();

        isDateChanged = false;
        isTimeChanged = false;
        customStartHour = 0;
        customStartMinute = 0;

        ivPoster                = findViewById(R.id.ImageViewPoster);
        rgDescription           = findViewById(R.id.RadioGroupDescription);
        rgTitle                 = findViewById(R.id.RadioGroupTitle);
        rgStartDate             = findViewById(R.id.radioGroupStartDate);
        rgEndDate               = findViewById(R.id.radioGroupEndDate);
        rbCustomStartDate       = findViewById(R.id.radioButtonCustomStartDate);
        rbCustomEndDate         = findViewById(R.id.radioButttonCustomEndDate);
        btnSaveSession          = findViewById(R.id.buttonSaveSession);
        btnCreateEvent          = findViewById(R.id.buttonCreateEvent);
        btnStartDateTimePicker  = findViewById(R.id.buttonStartDateTimePicker);
        btnEndDateTimePicker    = findViewById(R.id.buttonEndDateTimePicker);




        //get intent and parse created activity
        Intent intent       = getIntent();
        uriString           = intent.getStringExtra(PPConstants.URI);
        isFromSavedImage    = intent.hasExtra(PPConstants.SAVE_IMAGE_FLAG);
        rotation            = intent.getIntExtra(PPConstants.IMAGE_ROTATION, 0);
        timestamp           = intent.getLongExtra(PPConstants.TIMESTAMP, 0L);
        uid                 = intent.getIntExtra(PPConstants.UID, 0);
        
        thisEd =  new EventEntity();
        thisEd.uid          = uid;
        thisEd.rotation     =   rotation;
        thisEd.timestamp    =   timestamp;
        thisEd.imageUrl     = uriString;

        Log.d("Setting IMG", "Setting Image:  \""+ uriString+"\"");
        PPutils.setImagetoView(uriString, ivPoster, rotation);

        //TODO cleaner date logic
        //use relative date Time logic
        //https://developer.android.com/reference/android/icu/text/RelativeDateTimeFormatter
        df                  = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        parser              = new Parser();
        parsedDataTreeMap   = new TreeMap();
        textRecognizer      = TextRecognition.getClient();


        db = Room.databaseBuilder(getApplicationContext(), PPDatabase.class, "Poster-Parser").build();
        ed = db.eventDao();

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
                startActivityForResult(createEventIntent, PPConstants.CREATE_EVENT_INTENT);
            }
        });

        btnSaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EventEntity ee =  new EventEntity();
                ee.imageUrl = bitmapUri.toString();

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
                isStartDateTimePicker = true;
                startDatePickerDialog();
            }
        });

        btnEndDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create DateTime Picker Dialogue
                isStartDateTimePicker = false;
                startDatePickerDialog();
            }
        });

        rbCustomStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgStartDate.clearCheck();

                //TODO set the start date to custom
                // OR
                //start dialogue picker to pick custom
                Log.d(TAG, "onClick: Text:" +btnStartDateTimePicker.getText() );
                if (btnStartDateTimePicker.getText().equals("Pick Date") ){
                    isStartDateTimePicker = true;
                    startDatePickerDialog();
                } else {
                    //TODO set start date to Custom startDate
                    Calendar c = Calendar.getInstance();
                    c.set(customStartYear, customStartMonth, customStartDay,customStartHour, customStartMinute);
                    Log.d(TAG, "onClick: setting start date to "+ c.getTime().toString());
                    selectedEventStartDate = c.getTime();

                }
            }
        });

        rbCustomEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgEndDate.clearCheck();

                //TODO set the end date to custom
                // OR
                //start dialogue picker to pick custom
                if (btnEndDateTimePicker.getText() == "Pick Date"){
                    isStartDateTimePicker = false;
                    startDatePickerDialog();
                } else {
                    //TODO set end date to Custom endDate
                    Calendar c = Calendar.getInstance();
                    c.set(customEndYear, customEndMonth, customEndDay,customEndHour, customEndMinute);
                    Log.d(TAG, "onClick: setting start date to "+ c.getTime().toString());
                    selectedEventEndDate = c.getTime();

                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==  PPConstants.CREATE_EVENT_INTENT){
            Log.d(TAG, "onActivityResult: resultCode: "+resultCode);
            if(resultCode == RESULT_OK){
                //TODO Delete saved session here

                Thread t =  new Thread(){
                    public void run(){
                        ed.deleteAll(thisEd);
                        Log.d(TAG, "run: Event delete");
                        PPutils.toast(getApplicationContext(), "Session created");
                        finishActivity(RESULT_OK);
                    }
                };

                t.start();
                
            }

        }
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
                            rbCustomStartDate.setChecked(false);
                        }
                    });
                    rgStartDate.addView(rbStartDate);

                    final RadioButton rbEndDate =  new RadioButton(getApplicationContext());
                    rbEndDate.setText(date.toString());

                    rbEndDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setEventEndDate(rbEndDate.getText());
                            rbCustomEndDate.setChecked(false);
                        }
                    });
                    rgEndDate.addView(rbEndDate);

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

    private void setEventEndDate(CharSequence text) {
        try {
            selectedEventEndDate = df.parse(text.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogButtonSetDateClick(long longDate) {

//        if(!isTimeChanged ||!isDateChanged){
//            PPutils.toast(getApplicationContext(), "Please set time and date");
//            isTimeChanged   = false;
//            isDateChanged   = false;
//            return;
//        }

        PPutils.toast(getApplicationContext(), "Set Date picked: "+longDate);
        Calendar c = Calendar.getInstance();

        if(isStartDateTimePicker){
            c.set(customStartYear, customStartMonth, customStartDay,customStartHour, customStartMinute);
            customEventStartDate = c.getTime();
            btnStartDateTimePicker.setText("Custom: " + customEventStartDate.toString());
            return;
        }else{
            c.set(customEndYear, customEndMonth, customEndDay,customEndHour, customEndMinute);
            customEventEndDate = c.getTime();
            btnEndDateTimePicker.setText("Custom: " + customEventEndDate.toString());
        }


    }

    @Override
    public void onDialogCancelDateClick(DialogFragment dialog) {
        PPutils.toast(getApplicationContext(),"Set Date cancelled");
    }

    @Override
    public void onDateChanged(int year, int month, int day) {
        Log.d(TAG, "onDateChanged: DateChanged");
        if(isStartDateTimePicker){
            this.customStartYear = year;
            this.customStartMonth = month;
            this.customStartDay = day;
        } else {
            this.customEndYear = year;
            this.customEndMonth = month;
            this.customEndDay = day;
        }
        isDateChanged = true;

    }


    @Override
    public void onTimeChanged(int hour, int minute) {
        Log.d(TAG, "onTimeChanged: TimeChanged");
        if(isStartDateTimePicker){
            this.customStartHour   =  hour;
            this.customStartMinute =  minute;
        } else{
            this.customEndHour   =  hour;
            this.customEndMinute =  minute;
        }
        isTimeChanged = true;
    }

    private void startDatePickerDialog(){
        DialogFragment dialog = DateTimePickerDialogFragment.newInstance(CreateEventActivity.this);
        dialog.show(getSupportFragmentManager(), "dialog");
    }
}