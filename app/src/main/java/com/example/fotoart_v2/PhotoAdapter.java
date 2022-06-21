package com.example.fotoart_v2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deeparteffects.sdk.android.model.Styles;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder>{
    private ArrayList<String> imageList;
    private PhotoAdapter.ClickListener mClickListener;
    private Context context;

    public PhotoAdapter(ArrayList<String> imageList, Context context, PhotoAdapter.ClickListener clickListener) {
        this.imageList = imageList;
        this.context = context;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolder holder, int position) {
        // loading the images from the position
        String imageUrl = imageList.get(position);
        Glide.with(holder.itemView.getContext()).load(imageList.get(position)).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.photo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onClick(imageList.get(getBindingAdapterPosition()));
        }
    }

    public void removeItem(String item){
        this.imageList.remove(item);
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(String url);
    }
}
