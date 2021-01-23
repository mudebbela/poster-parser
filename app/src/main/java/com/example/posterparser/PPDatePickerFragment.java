package com.example.posterparser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class PPDatePickerFragment extends Fragment {
    public static String TAG = "PPDatePickerFragment";

    public DateChangeListener listener;

    public PPDatePickerFragment(){
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Instantiate the DateChangeListener so we can send events to the host
            listener = (DateChangeListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DateChangeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, null);
        Log.d(TAG, "onCreateView: returning view  ");

        DatePicker dp = view.findViewById(R.id.datePicker);
        dp.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                listener.onDateChanged(year, monthOfYear, dayOfMonth);
            }
        });
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public interface DateChangeListener {
        void onDateChanged(int year, int month, int date);
    }
}


