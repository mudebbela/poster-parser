package com.example.posterparser;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DateTimePickerFragmentActivity extends FragmentActivity {

    private static final int NUM_PAGES = 2;
    private static String TAG = "DateTimePickerFragmentActivity";
    private ViewPager2 viewPager;
    

    public DateTimePickerFragmentActivity() {
        super();
        Log.d("DateTimePickerFragmentActivity", "initialize DateTimePickerFragmentActivity: ");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_date_time_picker);
//        viewPager = findViewById(R.id.viewPagerDateTimePicker);
//        viewPager.setAdapter(new DateTimePickerAdapter(this));
        Log.d(TAG, "onCreate: created");
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }



}