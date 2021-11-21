package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;

public class SettingsActivity extends AppCompatActivity {

    private Button editProfileBtn;
    private EditText editUserName, editUserStatus;
    private CircleImageView circleImageView;

    private String uid;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializationView();
        editUserName.setVisibility(View.INVISIBLE);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserData();
            }
        });

        viewCheck();
    }

    private void viewCheck() {
        databaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                    String userName = snapshot.child("name").toString();
                    String userStatus = snapshot.child("status").toString();
                    String userImage = snapshot.child("image").toString();

                    editUserName.setText(userName);
                    editUserStatus.setText(userStatus);
//circleImageView.setIm
                } else if (snapshot.exists() && snapshot.hasChild("name")) {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    editUserName.setText(userName);
                    editUserStatus.setText(userStatus);
                } else {
                    editUserName.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserData() {
        HashMap<String, String> hashMap = new HashMap<>();
        String userName = editUserName.getText().toString();
        String userStatus = editUserStatus.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userStatus)) {

        } else {
            hashMap.put("name", userName);
            hashMap.put("uid", uid);
            hashMap.put("status", userStatus);
            databaseReference.child("Users").child(uid)
                    .setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                            } else {
                                String msg = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

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

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}