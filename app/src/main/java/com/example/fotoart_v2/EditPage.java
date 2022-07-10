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
import android.app.ActivityOptions;
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
import android.os.Handler;
import android.os.Looper;
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
    TabLayout tabLayout;

    private static final String TAG = EditPage.class.getSimpleName();

    private static final String API_KEY = "1Qi7C3CXx06nrDkM7PqKPaPLuveFkRaWZhD7lrHc";
    private static final String ACCESS_KEY = "AKIA3XE3HF7SQQUCVZN3";
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

        tabLayout = findViewById(R.id.tabLayout);

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
                tabLayout.setVisibility(View.INVISIBLE);
                BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = draw.getBitmap();
                UploadBitmap(EditPage.this, bitmap, UUID.randomUUID().toString());
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditPage.this, MyProfile.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(EditPage.this).toBundle();
                startActivity(i, bundle);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setChecked(false);
        bottomNavigationView.getMenu().getItem(1).setChecked(false);
        bottomNavigationView.getMenu().getItem(2).setChecked(false);
        bottomNavigationView.getMenu().getItem(3).setChecked(false);
        bottomNavigationView.getMenu().getItem(4).setChecked(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.crop:
                        sb_value.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        startCropActivity();
                        return true;
                    case R.id.effects:
                        recyclerView.setVisibility(View.VISIBLE);
                        sb_value.setVisibility(View.INVISIBLE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        loadingStyles();
                        return true;
                    case R.id.exposure:
                        sb_value.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        changeBrightness();
                        return true;
                    case R.id.filters:
                        sb_value.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.VISIBLE);
                        // revertToOriginalPhoto();
                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                if (tab.getText().equals("BW")) {
                                    imageView.setImageBitmap(setFiltru(image, 1));
                                }
                                if (tab.getText().equals("Sepia")) {
                                    imageView.setImageBitmap(setFiltru(image, 2));
                                }
                                if (tab.getText().equals("Warm tones")) {
                                    imageView.setImageBitmap(setFiltru(image, 3));
                                }
                                if (tab.getText().equals("Cold tones")) {
                                    imageView.setImageBitmap(setFiltru(image, 4));
                                }
                                if (tab.getText().equals("Solarise")) {
                                    imageView.setImageBitmap(setFiltru(image, 5));
                                }
                                if (tab.getText().equals("Invert")) {
                                    imageView.setImageBitmap(setFiltru(image, 6));
                                }
                                if (tab.getText().equals("No Filter")) {
                                    imageView.setImageBitmap(setFiltru(image, 7));
                                }
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                            }
                        });
                        if (image != null) {
                            getImages();
                        } else {
                            Toast.makeText(getApplicationContext(), "Choose a photo first!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.revert:
                        sb_value.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        revertToOriginalPhoto();
                }
                return false;
            }
        });
    }

    private void changeBrightness() {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        cropImage = getImageUri(getApplicationContext(), bitmap);
        image = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);

        sb_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 50) {
                    int value = (int) (progress - 50) * 255 / 100;
                    imageView.setImageBitmap(setBrightness(image, value));
                } else {
                    int value = (int) (50 - progress) * 255 / 100;
                    imageView.setImageBitmap(setBrightness(image, -value));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        BitmapDrawable draw1 = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap1 = draw1.getBitmap();
        cropImage = getImageUri(getApplicationContext(), bitmap1);
        image = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
    }

    public Bitmap setBrightness(Bitmap src, int value) {

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B;
        int pixel;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {

                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                R += value;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += value;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += value;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }

    public Bitmap setFiltru(Bitmap src, int value) {

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B;
        int Ai, Ri, Gi, Bi;
        int pixel;
        int r1, g1, b1;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {

                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                Ai = Color.alpha(pixel);
                Ri = Color.red(pixel);
                Gi = Color.green(pixel);
                Bi = Color.blue(pixel);

                if (value == 1) {
                    int media = (R + G + B) / 3;

                    R = media;
                    G = media;
                    B = media;
                }
                if (value == 2) {
                    double tR = 0.393 * R + 0.769 * G + 0.189 * B;
                    double tG = (int) (0.349 * R + 0.686 * G + 0.168 * B);
                    double tB = (int) (0.272 * R + 0.534 * G + 0.131 * B);

                    if (tR > 255) {
                        R = 255;
                    } else {
                        R = (int) Math.round(tR);
                    }

                    if (tG > 255) {
                        G = 255;
                    } else {
                        G = (int) Math.round(tG);
                    }

                    if (tB > 255) {
                        B = 255;
                    } else {
                        B = (int) Math.round(tB);
                    }
                }
                if (value == 3) {
                    if (R < 245) {
                        R = R + 8;
                    }
                }
                if (value == 4) {
                    if (B < 245) {
                        B = B + 8;
                        G = G + 5;
                    }
                }
                if (value == 5) {
                    if (R < 128) {
                        r1 = 255 - R;
                    } else {
                        r1 = R;
                    }
                    if (G < 128) {
                        g1 = 255 - G;
                    } else {
                        g1 = G;
                    }
                    if (B < 128) {
                        b1 = 255 - B;
                    } else {
                        b1 = B;
                    }

                    R = r1;
                    B = b1;
                    G = g1;
                }
                if (value == 6) {
                    R = 255 - R;
                    G = 255 - G;
                    B = 255 - B;
                }

                if (value == 7) {
                    A = Ai;
                    B = Bi;
                    R = Ri;
                    G = Gi;
                }

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
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
                                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(EditPage.this).toBundle();
                                    startActivity(intent, bundle);
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
        imageOriginal = ImageHelper.loadSizeLimitedBitmapFromUri(cropImage,
                this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
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
}