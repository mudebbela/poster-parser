package com.example.posterparser;

import android.content.Intent;
import android.provider.CalendarContract;
import static org.junit.Assert.*;


import androidx.core.app.ShareCompat;

import org.junit.Test;

import java.util.Date;

public class EventBuilderUnitTest {
    @Test
    public void testIntentBuilder(){
        String title = "title";
        String description =  "description";
        Date startDate =  new Date();
        Date endDate = new Date();
        Intent testIntent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE,title )
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTime())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,endDate.getTime());
        EventBuilder eb = new EventBuilder();
        Intent asserIntent = eb.setName(title)
                .setDescription(description)
                .setEndDate(endDate)
                .setStartDate(startDate)
                .build();
        assertEquals(testIntent,asserIntent);
    }
}
