package com.csis.social.app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.csis.social.app.adapters.ChooseSubjectInAddPostAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    ActionBar actionBar;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int VIDEO_PICK_GALLERY_CODE = 500;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    PlayerView playerView;
    Button uploadBtn, choosePhoto_btn, chooseVideo_btn;

    //user info
    String name, email, uid, dp, subject, pId;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    //image picked will be samed in this uri
    Uri image_rui, video_uri;
    private String pVideo, pVideoName;
    private SimpleExoPlayer simpleExoPlayer;

    //progress bar
    ProgressDialog pd;

    TextView subjectName;
    LinearLayout chooseSubject_linear, data_linear;
    Spinner levelSpinner, semesterSpinner;
    View line;
    ListView listView;
    TextView noSubjects;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private ChooseSubjectInAddPostAdapter chooseSubjectInAddPostAdapter;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AddPostActivity.this);
        userType = mPrefs.getString("userType", "");

        pd = new ProgressDialog(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init views
        choosePhoto_btn = findViewById(R.id.choosePhoto_btn);
        chooseVideo_btn = findViewById(R.id.chooseVideo_btn);
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        playerView = findViewById(R.id.playerView);
        uploadBtn = findViewById(R.id.pUploadBtn);
        subjectName = findViewById(R.id.subjectName);
        chooseSubject_linear = findViewById(R.id.chooseSubject_linear);
        data_linear = findViewById(R.id.data_linear);
        levelSpinner = findViewById(R.id.levelSpinner);
        semesterSpinner = findViewById(R.id.semesterSpinner);
        line = findViewById(R.id.line);
        listView = findViewById(R.id.list);
        noSubjects = findViewById(R.id.noSubjects);

        //get data through intent from previous activitie's adapter
        Intent intent = getIntent();

        //get data and its type from intent
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {
                //text type data
                handleSendText(intent);
            } else if (type.startsWith("image")) {
                //image type data
                handleSendImage(intent);
            } else if (type.startsWith("video")) {
                //image type data
                imageIv.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);
                data_linear.setVisibility(View.VISIBLE);

                handleSendVideo(intent);
            }
        }

        final String isUpdateKey = "" + intent.getStringExtra("key");
        final String editPostId = "" + intent.getStringExtra("editPostId");
        //validate if we came here to update post i.e. came from AdapterPost
        if (isUpdateKey.equals("editPost")) {
            //update
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);

            data_linear.setVisibility(View.VISIBLE);

        } else {
            //add
            actionBar.setTitle("Add New Post");
            uploadBtn.setText("Upload");

            subjectName.setText("???");
            chooseSubject_linear.setVisibility(View.VISIBLE);
        }

        actionBar.setSubtitle(email);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Utilities.getAllLevels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);

        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {

                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(AddPostActivity.this
                        , android.R.layout.simple_spinner_item, Utilities.getAllSemesters());
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(adapter2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                databaseReference = firebaseDatabase.getReference().child("Courses").child(levelSpinner.getSelectedItem().toString())
                        .child(semesterSpinner.getSelectedItem().toString());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> list = Utilities.getAllSubjectsFromSemester(dataSnapshot);
                        fillListView(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        subjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!subjectName.getText().toString().equals("???")) {
                    data_linear.setVisibility(View.VISIBLE);
                    chooseSubject_linear.setVisibility(View.VISIBLE);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String clickedSubject = chooseSubjectInAddPostAdapter.getItem(i).toString();

                subjectName.setText(clickedSubject);

                subject = clickedSubject;

                chooseSubject_linear.setVisibility(View.GONE);
                data_linear.setVisibility(View.VISIBLE);
            }
        });

        //get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance().

                getReference("Users");

        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new

                                            ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        name = "" + ds.child("name").getValue();
                                                        email = "" + ds.child("email").getValue();
                                                        dp = "" + ds.child("image").getValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


        //get image from camera/gallery on click
        choosePhoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                imageIv.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);

                showImagePickDialog();
            }
        });

        //get video from camera/gallery on click
        chooseVideo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                imageIv.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);

                pickVideoFromGallery();
            }
        });

        //upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data(title, description) from EditTexts
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (subject == null) {
                    Toast.makeText(AddPostActivity.this, "Choose related subject...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(title, description, editPostId);
                } else {
                    uploadData(title, description);
                }
            }
        });
    }

    private void fillListView(ArrayList<String> list) {
        if (list.size() == 0)
            noSubjects.setVisibility(View.VISIBLE);
        else {
            if (noSubjects.getVisibility() == View.VISIBLE)
                noSubjects.setVisibility(View.GONE);

            chooseSubjectInAddPostAdapter = new ChooseSubjectInAddPostAdapter(this, list);
            listView.setAdapter(chooseSubjectInAddPostAdapter);
        }
    }

    private void handleSendVideo(Intent intent) {
        //handle the received image(uri)
        video_uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (video_uri != null) {

            initExoPlayer(video_uri.toString());

            pVideoName = getVideoName();

            imageIv.setBackground(null);
        }
    }

    private void handleSendImage(Intent intent) {
        //handle the received image(uri)
        Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            image_rui = imageURI;
            //set to imageview
            imageIv.setImageURI(image_rui);
        }
    }

    private void handleSendText(Intent intent) {
        //handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            //set to description edit text
            descriptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();

        if (editImage.equals("null") && pVideo != null && pVideoName != null) {

            String filePathAndName = "Posts/post_" + pId;

            if (imageIv.getDrawable() != null) {
                //was with video but now has image in imageview
                deleteOldVideo(filePathAndName);
                updateWithNowImage(title, description, editPostId);

            } else if (video_uri != null) {
                //was with video but now has another video

                pd.setMessage("Publishing post...");
                pd.show();

                deleteOldVideo(filePathAndName);
                uploudVideo(title, description, pId, filePathAndName);

            } else {
                //was with video and neither made update on it nor made update on imageView, so only update texts
                updateWithoutImage(title, description, editPostId, null);
            }

        } else if (!editImage.equals("noImage")) {
            //was with image
            if (video_uri != null) {
                //but now have a video
                pd.setMessage("Publishing post...");
                pd.show();

                String filePathAndName = "Posts/post_" + pId;

                deleteOldVideo(filePathAndName);
                uploudVideo(title, description, pId, filePathAndName);

            } else if (imageIv.getDrawable() != null) {
                //but now have an image aslo
                updateWasWithImage(title, description, editPostId);
            }
        } else if (imageIv.getDrawable() != null || video_uri != null) {

            //was without image or video, but now has video
            if (video_uri != null) {
                //but now have a video
                pd.setMessage("Publishing post...");
                pd.show();

                String filePathAndName = "Posts/post_" + pId;

                uploudVideo(title, description, pId, filePathAndName);

            } else if (imageIv.getDrawable() != null) {
                //was without image or video, but now has image in imageview
                updateWithNowImage(title, description, editPostId);
            }
        } else {
            //was without image, and still no image in imageview
            updateWithoutImage(title, description, editPostId, "noImage");
        }
    }

    private void deleteOldVideo(String filePathAndName) {
        //post is with image, delete previous image first
        StorageReference mVideoRef = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        mVideoRef.delete();
    }

    private void updateWithoutImage(String title, String description, String editPostId, String pImage) {

        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        if (pImage != null)
            hashMap.put("pImage", pImage);
        else {
            hashMap.put("pVideo", pVideo);
            hashMap.put("pVideoName", pVideoName);
        }
        hashMap.put("subject", subject);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        //get image from imageview
        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //url is recieved, upload to firbease database

                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);
                            hashMap.put("subject", subject);

                            if (pVideo != null && pVideoName != null) {
                                hashMap.put("pVideo", null);
                                hashMap.put("pVideoName", null);
                            }

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {
        //post is with image, delete previous image first
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, upload new image
                        //for post-image name, post-id, publish-time
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timeStamp;

                        //get image from imageview
                        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //image uploaded get its url
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {
                                            //url is recieved, upload to firbease database

                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            //put post info
                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);
                                            hashMap.put("uEmail", email);
                                            hashMap.put("uDp", dp);
                                            hashMap.put("pTitle", title);
                                            hashMap.put("pDescr", description);
                                            hashMap.put("pImage", downloadUri);
                                            hashMap.put("subject", subject);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    editTitle = "" + ds.child("pTitle").getValue();
                    editDescription = "" + ds.child("pDescr").getValue();
                    editImage = "" + ds.child("pImage").getValue();
                    subject = "" + ds.child("subject").getValue();
                    pVideo = "" + ds.child("pVideo").getValue();
                    pVideoName = "" + ds.child("pVideoName").getValue();
                    pId = "" + ds.child("pId").getValue();

                    //set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);
                    subjectName.setText(subject);

                    //set image or video
                    if (editImage.equals("null") && pVideo != null && pVideoName != null) {

                        imageIv.setVisibility(View.GONE);
                        playerView.setVisibility(View.VISIBLE);

                        initExoPlayer(pVideo);

                    } else if (!editImage.equals("noImage")) {

                        playerView.setVisibility(View.GONE);
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        pd.setMessage("Publishing post...");
        pd.show();

        //for post-image name, post-id, post-publish-time
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (video_uri != null && pVideoName != null) {

            //post with video
            uploudVideo(title, description, timeStamp, filePathAndName);

        } else if (imageIv.getDrawable() != null) {
            //post with image
            //get image from imageview
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded to firebase storage, now get it's url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()) {

                                //url is received upload post to firebase database

                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");
                                hashMap.put("subject", subject);
                                if (userType.equals("Student"))
                                    hashMap.put("userNode", "Users");
                                else if (userType.equals("Admin"))
                                    hashMap.put("userNode", "Admins");

                                //path to store post data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_rui = null;

                                                //send notification
                                                prepareNotification(
                                                        "" + timeStamp,//since we are using timestamp for post id
                                                        "" + name + " added new post",
                                                        "" + title + "\n" + description,
                                                        "PostNotification",
                                                        "POST"
                                                );

                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //post without image

            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");
            hashMap.put("subject", subject);
            if (userType.equals("Student"))
                hashMap.put("userNode", "Users");
            else if (userType.equals("Admin"))
                hashMap.put("userNode", "Admins");

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_rui = null;

                            //send notification
                            prepareNotification(
                                    "" + timeStamp,//since we are using timestamp for post id
                                    "" + name + " added new post",
                                    "" + title + "\n" + description,
                                    "PostNotification",
                                    "POST"
                            );
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding post in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploudVideo(final String title, final String description, final String timeStamp, String filePathAndName) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(video_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to firebase storage, now get it's url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {

                            //url is received upload post to firebase database

                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pId", timeStamp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pVideo", downloadUri);
                            hashMap.put("pVideoName", pVideoName);
                            hashMap.put("pTime", timeStamp);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");
                            hashMap.put("subject", subject);
                            if (userType.equals("Student"))
                                hashMap.put("userNode", "Users");
                            else if (userType.equals("Admin"))
                                hashMap.put("userNode", "Admins");

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            //put data in this ref
                            ref.child(timeStamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //added in database
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                            //reset views
                                            titleEt.setText("");
                                            descriptionEt.setText("");
                                            imageIv.setImageURI(null);
                                            video_uri = null;

                                            //send notification
                                            prepareNotification(
                                                    "" + timeStamp,//since we are using timestamp for post id
                                                    "" + name + " added new post",
                                                    "" + title + "\n" + description,
                                                    "PostNotification",
                                                    "POST"
                                            );

                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed adding post in database
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed uploading image
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        //prepare data for notification


        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; //topic must match with what the receiver subscribed to
        String NOTIFICATION_TITLE = title; //e.g. Atif Pervaiz added new post
        String NOTIFICATION_MESSAGE = description; //content of post
        String NOTIFICATION_TYPE = notificationType; //now there are two notification types chat & post, so to differentiate in FirebaseMessaging.java class

        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid);//uid of current use/sender
            notificationBodyJo.put("pId", pId);//post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);

            notificationJo.put("data", notificationBodyJo);//combine data to be sent
        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        //send volley object requrest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error occurred
                        Toast.makeText(AddPostActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAA7AuJGz8:APA91bGwtgymO7JGkhboQEjJPR7wdIzOYA4ZeCU0th6udSCABz8VnPcfcwlh8R7hSrYBzX1QQcP8To55cwcRSjIj0YttTGVaaXP2e8u18QGbluxclRlIFBwlExiwqk9AkHPt6cLegJkt");//paste your fcm key here after "key="

                return headers;
            }
        };
        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void showImagePickDialog() {
        //options(camera, gallery) to show in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (which == 1) {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickPhotoFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickPhotoFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickVideoFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_GALLERY_CODE);
    }

    private void initExoPlayer(String url) {

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(simpleExoPlayer);
        DataSource.Factory daFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CSIS"));

        MediaSource mediaSource = new ExtractorMediaSource.Factory(daFactory).createMediaSource(Uri.parse(url));

        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(false);
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if camera permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            email = user.getEmail();
            uid = user.getUid();
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void clearSharedPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AddPostActivity.this);
        //editing into shared preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userType", "no users");
        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            clearSharedPreference();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press Allow or Deny from permission request dialog
        //here we will handle permission cases (allowed and denied)

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permission are granted
                        pickFromCamera();
                    } else {
                        //camera or gallery or both permissions were denied
                        Toast.makeText(this, "Camera & Storage both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission granted
                        pickPhotoFromGallery();
                    } else {
                        //camera or gallery or both permissions were denied
                        Toast.makeText(this, "Storage permissions necessary...", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_rui = data.getData();

                //set to imageview
                imageIv.setImageURI(image_rui);

                video_uri = null;
                playerView.setPlayer(null);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image

                imageIv.setImageURI(image_rui);

                video_uri = null;
                playerView.setPlayer(null);

            } else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                //image is picked from camera, get uri of image

                video_uri = data.getData();

                initExoPlayer(video_uri.toString());

                pVideoName = getVideoName();

                imageIv.setBackground(null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getVideoName() {
        String uriString = video_uri.toString();
        File myFile = new File(uriString);

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(video_uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        } else if (uriString.startsWith("file://")) {
            return myFile.getName();
        }
        return null;
    }

    public void pausePlayer() {

        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }

    public void releaseExoPlayer() {

        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(AddPostActivity.this);

        releaseExoPlayer();
    }
}
