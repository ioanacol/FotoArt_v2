package com.example.fotoart_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ViewPhoto extends AppCompatActivity {

    ImageView imageView;
    String imageRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        imageView = findViewById(R.id.viewImage);

        putImage();

        PhotoViewAttacher pAttacher;
        pAttacher = new PhotoViewAttacher(imageView);
        pAttacher.update();
    }

    public void putImage() {
        Intent i = getIntent();
        imageRecyclerView = i.getStringExtra("imageURL2");
        if (imageRecyclerView != null) {
            new ViewPhoto.DownloadImageTask2(imageView).execute(imageRecyclerView);
        }
    }

    private class DownloadImageTask2 extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask2(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}