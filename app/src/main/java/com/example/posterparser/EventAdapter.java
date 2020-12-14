package com.example.posterparser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{
    List<EventEntity> eventEntities;
    Context ctx;
    String TAG;

    public EventAdapter(List<EventEntity> eventEntities, Context ctx) {
        TAG = this.getClass().getName();
        Log.d(TAG, "EventAdapter: ");
        this.eventEntities = eventEntities;
        this.ctx =  ctx;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_event_layout, parent, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        EventEntity ed = eventEntities.get(position);
        holder.setImage(ed.imageUrl);
        holder.setDate(Long.toString(ed.timestamp));

    }

    @Override
    public int getItemCount() {
        return eventEntities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView tvCreatedDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setImage(String url){
            Log.d("Setting IMG", "Setting Image:  \""+ url+"\"");
            ImageView ivPosterPreview = mView.findViewById(R.id.imageViewPosterPreview);
            PPutils.setImagetoView(
                    url
                    ,ivPosterPreview
                    );

//            Picasso.get().load(url).placeholder(R.drawable.common_google_signin_btn_icon_light_focused).into((ImageView)mView.findViewById(R.id.imageViewPosterPreview));
        }
        public void setDate(String string){
            Date date = new Date(Long.parseLong(string));
            Log.d("Setting IMG", "Setting Date:  "+ date.toString());

            tvCreatedDate = mView.findViewById(R.id.textViewDateCreated);
            tvCreatedDate.setText("Created: "+date.toString());
        }
    }
}
