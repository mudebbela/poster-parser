package com.example.posterparser;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {



    private String currentPhotoPath;
    private Uri photoURI;
    static String TAG;
    private List<EventEntity> events;
    private EventAdapter eventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rvEvents;
    private PPDatabase db;

    @Override
    protected void onResume() {
        super.onResume();
        final EventDao ed = db.eventDao();
        Thread updateSessionListThread =  new Thread(){
            public void run(){
                final List<EventEntity> eds = ed.getAll();
                //add to view
                Log.d(TAG, "run: number of events: " +eds.size());
                if(eds.size()> 0){
                    events.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView welcome = findViewById(R.id.textViewWelcome);
                            welcome.setText("Saved Sessions");
                            welcome.setTextAppearance(R.style.TextAppearance_AppCompat_Display2);
                            for(EventEntity ed : eds){
                                Log.d(TAG, "Event UID: " + ed.uid);
                                Log.d(TAG, "Event Url: " + ed.imageUrl);
                                Log.d(TAG, "Event timestamp: " + ed.timestamp);

                                events.add(ed);
                                eventAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                Log.d(TAG, "run: Done getting all");
            }
        };
        updateSessionListThread.start();

   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TAG =  this.getLocalClassName();

        events =  new LinkedList();
        eventAdapter =  new EventAdapter(events, getApplicationContext());
        mLinearLayoutManager =  new LinearLayoutManager(this);
        Picasso.get().setLoggingEnabled(true);
        rvEvents = findViewById(R.id.recyclerViewEvents);
        rvEvents.setHasFixedSize(true);
        rvEvents.setAdapter(eventAdapter);
        rvEvents.setLayoutManager(mLinearLayoutManager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String [] options = {"Take Picture", "Use Saved Image"};
                builder.setTitle("Get Poster");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 1: {
                                PPutils.toast(getApplicationContext(), "using saved image");
                                useSavedImage();
                                break;
                            }
                            case 0:{
                                PPutils.toast(getApplicationContext(), "Taking picture");
                                takePicture();
                                break;
                            }
                            default:{
                                PPutils.toast(getApplicationContext(),"which: "+which);
                            }
                        }

                    }
                });
                AlertDialog dialog =  builder.create();
                dialog.show();
            }
        });
        db = Room.databaseBuilder(getApplicationContext(), PPDatabase.class, "Poster-Parser").build();
    }

    private void useSavedImage() {
        // solution found in http://blog.vogella.com/2011/09/13/android-how-to-get-an-image-via-an-intent/
        //TODO remove
        Intent getSavedImageIntent =  new Intent();
        getSavedImageIntent.setType("image/*");
        getSavedImageIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(getSavedImageIntent, "Select Picture"), PPConstants.REQUEST_SAVED_IMAGE);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "IO Exception, Check codebase", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //TODO add photo
                 photoURI = FileProvider.getUriForFile(this,
                        "com.example.posterparser",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PPConstants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(this, "requestCode : "+requestCode +"resultCode: " +resultCode, Toast.LENGTH_SHORT).show();
        if(resultCode != RESULT_OK){
            Toast.makeText(this, "intent not okay\n" +
                    "RESULT_CODE: "+resultCode+ ", REQUEST_CODE: "+requestCode, Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PPConstants.REQUEST_IMAGE_CAPTURE) {
            //TODO rotate image and save it to photo again
            Intent startCreateEventActivityIntent =  new Intent(getApplicationContext(), CreateEventActivity.class);
            startCreateEventActivityIntent.putExtra(PPConstants.URI, photoURI.toString());
            startCreateEventActivityIntent.putExtra(PPConstants.IMAGE_ROTATION, PPutils.getRotation(currentPhotoPath));
            startActivity(startCreateEventActivityIntent);

        } else if(requestCode == PPConstants.REQUEST_SAVED_IMAGE){
            //pass the URI to make image intent
            Uri uri =  data.getData();
            //start make event activity
            Intent startCreateEventActivityIntent =  new Intent(getApplicationContext(), CreateEventActivity.class);
            String uriString =  uri.toString();
            startCreateEventActivityIntent.putExtra(PPConstants.URI, uriString);
            startCreateEventActivityIntent.putExtra(PPConstants.SAVE_IMAGE_FLAG, true);
            startCreateEventActivityIntent.putExtra(PPConstants.IMAGE_ROTATION, 0);
            startActivity(startCreateEventActivityIntent);


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents'

        currentPhotoPath = image.getAbsolutePath();
        Log.d(this.getLocalClassName(), "createImageFile: absolutePath: "+currentPhotoPath);
        return image;
    }

}