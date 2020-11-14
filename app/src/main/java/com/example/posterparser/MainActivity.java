package com.example.posterparser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {



    private Button btnUseCamera,btnUseSavedImage;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //TODO  Dialogue menu to use saved Img or use Camera
            }
        });

        btnUseCamera =  findViewById(R.id.buttonTakePicture);
        btnUseSavedImage = findViewById(R.id.buttonUseSaveImage);

        Toast.makeText(this,   btnUseSavedImage.getText(), Toast.LENGTH_SHORT).show();

        btnUseSavedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // solution found in http://blog.vogella.com/2011/09/13/android-how-to-get-an-image-via-an-intent/
                //TODO remove
                Toast.makeText(MainActivity.this, "Use Saved Image", Toast.LENGTH_SHORT).show();
                Intent getSavedImageIntent =  new Intent();
                getSavedImageIntent.setType("image/*");
                getSavedImageIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(getSavedImageIntent, "Select Picture"), PPConstants.REQUEST_SAVED_IMAGE);

            }
        });

        btnUseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();


            }
        });


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
                        "com.example.posterparser.fileprovider",
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
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // go to Create event activity
            // give it Bitmap and imagePath
            Intent startCreateEventActivityIntent =  new Intent(getApplicationContext(), CreateEventActivity.class);
            startCreateEventActivityIntent.putExtra(PPConstants.URI, photoURI);
            startCreateEventActivityIntent.putExtra(PPConstants.IMAGE_PATH_CONSTANT, currentPhotoPath);
            startCreateEventActivityIntent.putExtra(PPConstants.BITMAP_CONSTANT, imageBitmap);
            startActivity(startCreateEventActivityIntent);
        } else if(requestCode == PPConstants.REQUEST_SAVED_IMAGE){
            //pass the URI to make image intent
            Uri uri =  data.getData();
            //start make event activity
            Intent startCreateEventActivityIntent =  new Intent(getApplicationContext(), CreateEventActivity.class);
            String uriString =  uri.toString();
            startCreateEventActivityIntent.putExtra(PPConstants.URI, uriString);
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

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}