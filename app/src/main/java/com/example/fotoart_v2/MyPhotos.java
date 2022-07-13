package com.example.fotoart_v2;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyPhotos extends AppCompatActivity {

    ArrayList<String> imagelist;
    RecyclerView recyclerView;
    StorageReference root;
    ProgressBar progressBar;
    private boolean isProcessing = false;
    String deletedURL = null;
    PhotoAdapter.ClickListener clickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_photos_activity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        imagelist = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 5));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("UserImage");
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference file : listResult.getItems()) {
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imagelist.add(uri.toString());
                            Log.e("Itemvalue", uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            progressBar.setVisibility(View.GONE);
                            clickListener = new PhotoAdapter.ClickListener() {
                                @Override
                                public void onClick(String url) {
                                    if (!isProcessing) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MyPhotos.this);
                                        LayoutInflater inflater = getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.open_photo, null);
                                        builder.setCancelable(false);
                                        builder.setView(dialogView);

                                        final AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                        alertDialog.setCanceledOnTouchOutside(true);

                                        TextView btnEdit, btnDelete;

                                        btnEdit = dialogView.findViewById(R.id.edit);
                                        btnDelete = dialogView.findViewById(R.id.delete);

                                        btnEdit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                isProcessing = true;
                                                Intent intent = new Intent(getApplicationContext(), EditPage.class);
                                                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MyPhotos.this).toBundle();
                                                intent.putExtra("imageURL", url);
                                                Log.v("URL", url);
                                                alertDialog.cancel();
                                                startActivity(intent, bundle);
                                                finish();
                                            }
                                        });

                                        btnDelete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
//                                                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//                                                StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);
//                                                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.e("Picture ", "#deleted");
//                                                    }
//                                                });
//
//                                                imagelist.remove(url);
//                                                alertDialog.cancel();
//                                                finish();
//                                                overridePendingTransition(0, 0);
//                                                startActivity(getIntent());
//                                                overridePendingTransition(0, 0);

                                                isProcessing = true;
                                                Intent intent = new Intent(getApplicationContext(), ViewPhoto.class);
                                                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MyPhotos.this).toBundle();
                                                intent.putExtra("imageURL2", url);
                                                alertDialog.cancel();
                                                startActivity(intent, bundle);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            };
                            final PhotoAdapter adapter = new PhotoAdapter(imagelist, getApplicationContext(), clickListener);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}