package uz.soft.whatsapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;

public class SettingsActivity extends AppCompatActivity {

    //todo does not works if i first created new account

    private static final int REQ_GALLERY_CODE = 416;
    private Button editProfileBtn;
    private EditText editUserName, editUserStatus;
    private CircleImageView circleImageView;

    private String uid;
    private String imageUrl = null;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressDialog = new ProgressDialog(this);

        progressDialog.setTitle(" Loading...");
        progressDialog.setTitle("Please wait..");
        progressDialog.show();
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        initializationView();

        viewCheck();
        editUserName.setVisibility(View.INVISIBLE);

        editProfileBtn.setOnClickListener(view -> updateUserData());

        circleImageView.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, REQ_GALLERY_CODE);
        });
    }

    private void initializationView() {
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        editProfileBtn = findViewById(R.id.edit_profile_btn);
        editUserName = findViewById(R.id.update_username);
        editUserStatus = findViewById(R.id.profile_status);
        circleImageView = findViewById(R.id.setting_image);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.show();

        Toast.makeText(this, "resultCode: " + resultCode + "requestCode:"
                + requestCode, Toast.LENGTH_SHORT).show();

        if (resultCode == RESULT_OK && requestCode == REQ_GALLERY_CODE && data != null) {
            Uri uri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);


        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                StorageReference filePath = storageReference.child(uid + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getMetadata()
                                .getReference()
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    progressDialog.show();
                                    String downloadUrl = uri.toString();
                                    imageUrl = downloadUrl;
                                    databaseReference.child("Users")
                                            .child(uid)
                                            .child("image")
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Glide.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(circleImageView);
                                                    progressDialog.dismiss();
                                                } else {
                                                    Toast.makeText(SettingsActivity.this, "Download url", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }).addOnFailureListener(err -> progressDialog.dismiss());
                                    ;
                                }));

            }
        }
    }

    private void viewCheck() {
        databaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()
                        && snapshot.hasChild("name")
                        && snapshot.hasChild("image")) {

                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String urlDownload = snapshot.child("image").getValue().toString();
                    imageUrl = urlDownload;
                    editUserName.setText(userName);
                    editUserStatus.setText(userStatus);

                    Glide.with(getApplicationContext())
                            .load(urlDownload)
                            .into(circleImageView);

                } else if (snapshot.exists()
                        && snapshot.hasChild("name")) {

                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    editUserName.setText(userName);
                    editUserStatus.setText(userStatus);
                } else {
                    editUserName.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void updateUserData() {
        progressDialog.show();

        HashMap<String, String> hashMap = new HashMap<>();

        String userName = editUserName.getText().toString();
        String userStatus = editUserStatus.getText().toString();

        if (TextUtils.isEmpty(userName) && TextUtils.isEmpty(userStatus)) {
            Toast.makeText(this, "check your inputs", Toast.LENGTH_SHORT).show();
        }
        if (imageUrl != null && !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userStatus)) {
            hashMap.put("name", userName);
            hashMap.put("uid", uid);
            hashMap.put("status", userStatus);
            hashMap.put("image", imageUrl);

            databaseReference
                    .child("Users")
                    .child(uid)
                    .setValue(hashMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this,
                                    "Welcome", Toast.LENGTH_SHORT)
                                    .show();
                            goToMainActivity();
                        }
                    });
        } else {
            hashMap.put("name", userName);
            hashMap.put("uid", uid);
            hashMap.put("status", userStatus);

            databaseReference
                    .child("Users")
                    .child(uid)
                    .setValue(hashMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this,
                                    "Welcome", Toast.LENGTH_SHORT)
                                    .show();
                            goToMainActivity();
                        }
                    });
        }
    }


    private void goToMainActivity() {
        progressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}