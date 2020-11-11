package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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

public class CreateEventActivity extends AppCompatActivity {
    private String photoPathString;
    private Bitmap bitmap;
    private RadioGroup rgDescription;
    private RadioGroup rgTitle;

    ImageView ivPoster;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        //get intent and parse created activity
        Intent intent = getIntent();
        textRecognizer = TextRecognition.getClient();
        photoPathString = intent.getStringExtra(PPConstants.IMAGE_PATH_CONSTANT);
        bitmap =  intent.getParcelableExtra(PPConstants.BITMAP_CONSTANT);

        ivPoster        =  findViewById(R.id.ImageViewPoster);
        rgDescription   = findViewById(R.id.RadioGroupDescription);
        rgTitle         = findViewById(R.id.RadioGroupTitle);

        ivPoster.setImageBitmap(bitmap);

        //Parse
        parseImage(bitmap);


    }

    private void parseImage(Bitmap bitmap) {
        //get bitmap
        InputImage inputImage = InputImage.fromBitmap(bitmap, 1);

        // recognize text

        Task<Text> result =
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
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
        Log.d(getLocalClassName(), "parseImage: "+ result.getResult().getText());
        Toast.makeText(this, result.getResult().getText(), Toast.LENGTH_SHORT).show();
    }
}