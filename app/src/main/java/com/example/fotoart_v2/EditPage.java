package com.example.fotoart_v2;

import static com.amazonaws.regions.Regions.EU_WEST_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
import com.deeparteffects.sdk.android.model.Result;
import com.deeparteffects.sdk.android.model.Styles;
import com.deeparteffects.sdk.android.model.UploadRequest;
import com.deeparteffects.sdk.android.model.UploadResponse;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class EditPage extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imageView;
    Button btnGallery;
    TextView save, open, myProfile;
    Bitmap imageOriginal;
    Bitmap image;
    SeekBar sb_value;
    Uri cropImage;
    ProgressBar progressBar;
    String imageRecyclerView;


    private static final String TAG = EditPage.class.getSimpleName();

    private static final String API_KEY = "hjq6UT28O07X17sfDAp6p4wplHFcKPin1EFyew4p";
    private static final String ACCESS_KEY = "AKIA3XE3HF7SVYZTRKLQ";
    private static final String SECRET_KEY = "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";

    private static final int CHECK_RESULT_INTERVAL_IN_MS = 2500;
    private static final int IMAGE_MAX_SIDE_LENGTH = 768;

    private AppCompatActivity mActivity;
    private boolean isProcessing = false;

    private RecyclerView recyclerView;
    private DeepArtEffectsClient deepArtEffectsClient;

    final int CAMERA_PERMISSION_CODE = 100;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page_actvity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


        mActivity = this;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        ApiClientFactory factory = new ApiClientFactory()
                .apiKey(API_KEY)
                .credentialsProvider(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
                    }

                    @Override
                    public void refresh() {
                    }
                }).region(EU_WEST_1.getName());

        deepArtEffectsClient = factory.build(DeepArtEffectsClient.class);

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
                sb_value.setVisibility(View.INVISIBLE);
                openNewPicture();
            }
        });

        putImage();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                switch (item.getItemId()) {
                    case R.id.crop:
                        sb_value.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        startCropActivity();
                        return true;

                    case R.id.effects:
                        recyclerView.setVisibility(View.VISIBLE);
                        sb_value.setVisibility(View.INVISIBLE);
                        loadingStyles();
                        return true;

                    case R.id.exposure:
                        sb_value.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        changeBrightness();
                        return true;

                    case R.id.revert:
                        sb_value.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        revertToOriginalPhoto();
                        return true;
                }
                return false;
            }
        });
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
        if (progress >= 50) {
            int value = (int) (progress - 50) * 255 / 100;

            return new PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
        } else {
            int value = (int) (50 - progress) * 255 / 100;
            return new PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void choosePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPage.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.picture, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        btnGallery = dialogView.findViewById(R.id.btnGallery);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(true);


        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureFromGallery();
                alertDialog.cancel();
            }
        });

    }

    private void takePictureFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void openNewPicture() {
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

    private void revertToOriginalPhoto() {
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
                imageView.setImageBitmap(imageOriginal);
                alertDialog3.cancel();
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void startCropActivity() {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        cropImage = getImageUri(getApplicationContext(), bitmap);
        CropImage.activity(cropImage)
                .start(this);
        image = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("request code", String.valueOf(requestCode));
        Log.v("result code", String.valueOf(resultCode));
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    imageOriginal = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                            this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
                    image = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                            this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
                    imageView.setImageBitmap(image);
                }
                break;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImage = result.getUri();
                imageView.setImageURI(cropImage);
                image = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                        this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
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
                                Log.e("oops", "error in bitmap uploading");

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
                                    Log.e("oops", "error in url retrieval");
                                }
                            }
                        });
                    }
                });
            }
        });
        thread.start();
    }

    private void loadingStyles() {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Please wait....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Styles styles = deepArtEffectsClient.stylesGet();
                loadingBar.dismiss();
                final StyleAdapter styleAdapter = new StyleAdapter(
                        getApplicationContext(),
                        styles,
                        new StyleAdapter.ClickListener() {
                            @Override
                            public void onClick(String styleId) {
                                if (!isProcessing) {
                                    if (image != null) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Log.d(TAG, String.format("Style with ID %s clicked.", styleId));
                                        isProcessing = true;
                                        uploadImage(styleId);
                                    } else {
                                        Toast.makeText(mActivity, "Please choose a picture first!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(styleAdapter);
                    }
                });
            }
        }).start();
    }

    private class ImageReadyCheckTimer extends TimerTask {

        private final String mSubmissionId;

        ImageReadyCheckTimer(String submissionId) {
            mSubmissionId = String.valueOf(submissionId);
        }

        public void run() {
            try {
                final Result result = deepArtEffectsClient.resultGet(mSubmissionId);
                String submissionStatus = result.getStatus();
                Log.d(TAG, String.format("Submission status is %s", submissionStatus));
                if (submissionStatus.equals(SubmissionStatus.FINISHED)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mActivity).load(result.getUrl()).into(imageView);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    isProcessing = false;
                    cancel();
                }
            } catch (Exception e) {
                cancel();
            }
        }
    }

    private void uploadImage(final String styleId) {
        Log.d(TAG, String.format("Upload image with style id %s", styleId));
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadRequest uploadRequest = new UploadRequest();
                uploadRequest.setStyleId(styleId);
                uploadRequest.setImageBase64Encoded(convertBitmapToBase64(image));
                UploadResponse response = deepArtEffectsClient.uploadPost(uploadRequest);
                String submissionId = response.getSubmissionId();
                Log.d(TAG, String.format("Upload complete. Got submissionId %s", response.getSubmissionId()));
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ImageReadyCheckTimer(submissionId),
                        CHECK_RESULT_INTERVAL_IN_MS, CHECK_RESULT_INTERVAL_IN_MS);
            }
        }).start();
    }

    public void putImage() {
        Intent i = getIntent();
        imageRecyclerView = i.getStringExtra("imageURL");
        if (imageRecyclerView != null) {
            new EditPage.DownloadImageTask(imageView).execute(imageRecyclerView);
        }
    }

    public void getImages() {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        cropImage = getImageUri(getApplicationContext(), bitmap);
        image = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
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
            getImages();
        }
    }


    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src", src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap", "returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }
}