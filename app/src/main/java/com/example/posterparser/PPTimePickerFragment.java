package com.example.posterparser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class PPTimePickerFragment extends Fragment {

    TimeChangeListener listener;
    public PPTimePickerFragment(){

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Instantiate the DateChangeListener so we can send events to the host
            listener = (TimeChangeListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement TimeChangeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_picker, null);
        TimePicker timePicker =  view.findViewById(R.id.timePicker);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                listener.onTimeChanged(hourOfDay, minute);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public interface TimeChangeListener {
        void onTimeChanged(int hour, int minute);
    }
}
