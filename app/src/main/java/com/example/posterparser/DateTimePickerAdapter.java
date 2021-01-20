package com.example.posterparser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DateTimePickerAdapter extends FragmentStateAdapter {

    public static String TAG = "DateTimePickerAdapter";
    public static String [] tabNames = {"Set Date","Set Time"};

    public DateTimePickerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        Log.d(TAG, "DateTimePickerAdapter: initialize DatetimePickerAdapter");

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                Log.d(TAG, "createFragment: returning Date picker fragment");
                return new PPDatePickerFragment();
            case 1:
                Log.d(TAG, "createFragment: returning time picker fragment");
                return new PPTimePickerFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: returning count of 2");
        return 2;
    }
}
