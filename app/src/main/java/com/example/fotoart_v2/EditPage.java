package com.example.fotoart_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class EditPage extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imageView;
    Button btnGallery, btnCamera;
    TextView save, open, myProfile;
    Uri imageOriginal, image;
    TabLayout tabLayout;
    SeekBar sb_value;

    final int CAMERA_PERMISSION_CODE = 100;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page_actvity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        tabLayout = findViewById(R.id.tabLayout);
        sb_value = (SeekBar) findViewById(R.id.seekBar);
        sb_value.setProgress(50);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }

        });

        save = findViewById(R.id.save);
        open = findViewById(R.id.open);
        myProfile = findViewById(R.id.myProfile);
        progressDialog = new ProgressDialog(this);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabLayout.setVisibility(View.INVISIBLE);
                sb_value.setVisibility(View.INVISIBLE);
                openNewPicture();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabLayout.setVisibility(View.INVISIBLE);
                sb_value.setVisibility(View.INVISIBLE);
                BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = draw.getBitmap();

                UploadBitmap(EditPage.this, bitmap, UUID.randomUUID().toString());
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditPage.this, MyProfile.class);
                startActivity(i);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(4).setChecked(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.crop:
                        tabLayout.setVisibility(View.INVISIBLE);
                        sb_value.setVisibility(View.INVISIBLE);
                        startCropActivity(image);
                        return true;

                    case R.id.effects:
                        tabLayout.setVisibility(View.VISIBLE);
                        sb_value.setVisibility(View.INVISIBLE);
                        return true;

                    case R.id.exposure:
                        sb_value.setVisibility(View.VISIBLE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        changeBrightness();
                        return true;

                    case R.id.revert:
                        tabLayout.setVisibility(View.INVISIBLE);
                        sb_value.setVisibility(View.INVISIBLE);
                        revertToOriginalPhoto();
                        return true;
                }
                return false;
            }
        });

//        sb_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
//                textView.setText("" + progress);
//                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
//                //textView.setY(100); just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
//
//            }
//        });

    }

    private void changeBrightness() {
        sb_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imageView.setColorFilter(setBrightness(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public static PorterDuffColorFilter setBrightness(int progress) {
        if (progress >= 50)
        {
            int value = (int) (progress-50) * 255 / 100;

            return new PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
        }
        else
        {
            int value = (int) (50-progress) * 255 / 100;
            return new PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        tabLayout.setVisibility(View.INVISIBLE);
    }

    private void choosePicture (){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPage.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.picture, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        btnGallery = dialogView.findViewById(R.id.btnGallery);
        btnCamera = dialogView.findViewById(R.id.btnCamera);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(true);


        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureFromGallery();
                alertDialog.cancel();
                imageView.setClickable(false);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(checkPermission(Manifest.permission.CAMERA,
                       CAMERA_PERMISSION_CODE)) {
                    takePictureFromCamera();
                    alertDialog.cancel();
                    imageView.setClickable(false);
                }
            }
        });
    }
    private void takePictureFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicture, 2);
        }
    }

    private void openNewPicture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPage.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.open_new_picture, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        final AlertDialog alertDialog2 = builder.create();
        alertDialog2.show();
        alertDialog2.setCanceledOnTouchOutside(true);

        TextView btnOpen, btnCancel;

        btnOpen = dialogView.findViewById(R.id.open);
        btnCancel = dialogView.findViewById(R.id.cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog2.cancel();
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureFromGallery();
                alertDialog2.cancel();
            }
        });
    }

    private void revertToOriginalPhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPage.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.revert_to_initial_photo, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        final AlertDialog alertDialog3 = builder.create();
        alertDialog3.show();
        alertDialog3.setCanceledOnTouchOutside(true);

        TextView btnRevert, btnCancel;

        btnRevert = dialogView.findViewById(R.id.revert);
        btnCancel = dialogView.findViewById(R.id.cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog3.cancel();
            }
        });

        btnRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageURI(imageOriginal);
                alertDialog3.cancel();
            }
        });
    }

    private void startCropActivity(Uri image){
        CropImage.activity(image)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    imageOriginal = data.getData();
                    image = data.getData();
                    imageView.setImageURI(image);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Bitmap bitmapImage = (Bitmap) bundle.get("data");
                    imageView.setImageBitmap(bitmapImage);
                }
                break;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                image = result.getUri();
                imageView.setImageURI(image);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void UploadBitmap(Activity activity, Bitmap bitmap, String serverFileName) {

        if (bitmap == null) {
            return;
        }

        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Save image");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                String directory = "UserImage/";

                StorageReference fileRef = storageRef.child(directory + serverFileName);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] data = stream.toByteArray();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        UploadTask uploadTask = fileRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Unsuccessful uploads
                                Log.e("oops","error in bitmap uploading");

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // ...
                            }
                        });

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return fileRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    String stringUrl = downloadUri.toString();
                                    Intent intent = new Intent(getApplicationContext(), SaveActivity.class);
                                    intent.putExtra("imageURL", stringUrl);
                                    startActivity(intent);
                                    progressDialog.dismiss();
                                    finish();

                                } else {
                                    // Unsuccessful uploads
                                    Log.e("oops","error in url retrieval");
                                }
                            }
                        });


                    }
                });


            }
        });
        thread.start();
    }

    public boolean checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(EditPage.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(EditPage.this, new String[] { permission }, requestCode);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(EditPage.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(EditPage.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}