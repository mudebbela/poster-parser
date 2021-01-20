package com.example.posterparser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PPDatePickerFragment extends Fragment {
    public static String TAG = "PPDatePickerFragment";

    public PPDatePickerFragment(){
        super();
        Log.d(TAG, "PPDatePickerFragment: initializing Date fragment");

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, null);
        Log.d(TAG, "onCreateView: returning view  ");
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
