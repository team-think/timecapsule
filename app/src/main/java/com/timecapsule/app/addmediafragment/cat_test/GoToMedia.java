package com.timecapsule.app.addmediafragment.cat_test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.timecapsule.app.R;
import com.timecapsule.app.addmediafragment.AudioFragment;
import com.timecapsule.app.addmediafragment.CapsuleUploadFragment;
import com.timecapsule.app.feedactivity.FeedActivity;
import com.timecapsule.app.profilefragment.model.Capsule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by tarynking on 3/11/17.
 */

public class GoToMedia extends AppCompatActivity {

    private static final int TAKE_PICTURE = 200;
    private static final int CAPTURE_VIDEO = 201;
    private static final String TAG = "Database Exception";
    private AudioFragment audioFragment;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference imagesRef;
    private UploadTask uploadTask;
    private ProgressDialog mProgress;
    private String mediaType;
    private CapsuleUploadFragment capsuleUploadFragment;
    private double locationLat;
    private double locationLong;
    private String address;
    private File destinationFile;
    private Intent intent;
    private Capsule capsule;
    private List <String> keyId;
    private List<String> mUrl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        mProgress = new ProgressDialog(this);
        imagesRef = storageReference.child("images");
        mediaType = getIntent().getExtras().getString("mediaType");
        locationLat = getIntent().getExtras().getDouble("locationLat");
        locationLong = getIntent().getExtras().getDouble("locationLong");
        address = getIntent().getExtras().getString("keyAddress");
        openMedia(mediaType);
    }

    private void openMedia(String mediaType) {
        switch (mediaType) {
            case "camera":
                goToNativeCamera();
                break;
            case "video":
                goToNativeVideo();
                break;
            case "audio":
                goToAudio();
                break;
        }
    }

    private void goToNativeCamera() {
        Intent capture = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(capture, TAKE_PICTURE);
    }


    private void goToAudio() {
        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        audioFragment = AudioFragment.newInstance("Audio");
        audioFragment.show(ft, "audio");
    }


    public void goToNativeVideo() {
        Intent record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(record, CAPTURE_VIDEO);
    }

    private void addUrlToDatabase(Uri uri) {
        Calendar c = Calendar.getInstance();
        String date = c.getTime().toString();
        String capsuleId = UUID.randomUUID().toString().replaceAll("-", "");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users")
                .child(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid())
                .child("capsules").child(capsuleId);
        myRef.setValue(uri.toString());
        DatabaseReference capRef = database.getReference("capsules").child(capsuleId);
        String storageLink = uri.toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.setValue(new Capsule(userId, storageLink, locationLat, locationLong, date));
        capRef.setValue(new Capsule(userId, storageLink, locationLat, locationLong, date));
        database.getReference("capsules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Capsule capsule = dataSnapshot.getValue(Capsule.class);
                capsule.getStorageUrl();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Get Capsule:Canceled", databaseError.toException());
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("GO TO MEDIA", "onActivityResult: ");
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    mProgress.setMessage("Uploading Photo");
                    mProgress.setIcon(R.drawable.time_capsule_logo12);
                    mProgress.show();
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] dataBAOS = baos.toByteArray();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        String firebaseReference = imageFileName.concat(".jpg");
                        imagesRef = imagesRef.child(firebaseReference);
                        StorageReference newImageRef = storageReference.child("images/".concat(firebaseReference));
                        newImageRef.getName().equals(newImageRef.getName());
                        newImageRef.getPath().equals(newImageRef.getPath());
                        UploadTask uploadTask = imagesRef.putBytes(dataBAOS);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                addUrlToDatabase(downloadUrl);
                                mProgress.dismiss();
                                goToCapsuleUploadFragment("capsule upload");

                            }
                        });
                    }
                }
                break;
            case CAPTURE_VIDEO:
                if (resultCode == RESULT_OK) {
                    mProgress.setMessage("uploading video...");
                    mProgress.show();
                    if (data != null) {
                    }
                }
                break;
        }
    }

    private void goToCapsuleUploadFragment(String capsuleUpload) {
        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        capsuleUploadFragment = CapsuleUploadFragment.newInstance(capsuleUpload);
        capsuleUploadFragment.show(ft, "Capsule Uploaded");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gotoFeedActivity();
    }

    private void gotoFeedActivity() {
        Intent intent = new Intent(this, FeedActivity.class);
        this.startActivity(intent);
    }
}