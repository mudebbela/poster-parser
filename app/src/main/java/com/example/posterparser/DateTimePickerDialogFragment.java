package com.example.posterparser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;

public class DateTimePickerDialogFragment extends DialogFragment {
    //TODO initialize from created instance
    private static Context ctx;

    NoticeDialogListener listener;
    private ViewPager2 viewPager;
    private Long longDate;

    public Long getLongDate() {
        return longDate;
    }

    public void setLongDate(Long longDate) {
        this.longDate = longDate;
    }


    public static DialogFragment newInstance(Context newCtx) {
        ctx =newCtx;
        return new DateTimePickerDialogFragment();
    };


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_date_time_picker_dialog_fragment,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        longDate = 0L;
        viewPager =  view.findViewById(R.id.pagerCreateDate);
        viewPager.setAdapter(new DateTimePickerAdapter(getActivity()));
        viewPager.setUserInputEnabled(false);



        //TODO Set date and time on tabs
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(DateTimePickerAdapter.tabNames[position]);
            }

        }).attach();

        Button btSetDateTime = (Button) view.findViewById(R.id.buttonSetDateTime);
        Button btCancelDateTime = (Button) view.findViewById(R.id.buttonCancelDateTime);

        btSetDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longDate = 12L;
                //TODO update Datetime on dismiss
                listener.onDialogButtonSetDateClick(longDate);
                dismiss();
            }
        });

        btCancelDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btCancelDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO dismiss
            }
        });

    }

    public interface NoticeDialogListener {
        public void onDialogButtonSetDateClick(long date);
        public void onDialogCancelDateClick(DialogFragment dialog);
    }

}