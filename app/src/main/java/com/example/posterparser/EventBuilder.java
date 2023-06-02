package com.example.posterparser;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;

import java.util.Date;

public class EventBuilder {

    private String name;
    private String description;
    private Date startDate;
    private Date endDate;

    public EventBuilder() {
    }

    public EventBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EventBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public EventBuilder setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public EventBuilder setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public Intent build(){

        Intent eventIntent =  new Intent(Intent.ACTION_INSERT);

        eventIntent.setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, name)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
        ;


        if(startDate!=null){
            eventIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTime());
        }


        if(endDate!=null){
            eventIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, endDate.getTime());
        }


        return  eventIntent;
    }



}
