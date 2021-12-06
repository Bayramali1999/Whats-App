package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;

public class VideoCallComingActivity extends AppCompatActivity {


    private CircleImageView profileImage;
    private FloatingActionButton cancelButton, acceptBtn;
    private TextView tvName;

    private String senderId, userName, userImageUrl, currentUser;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_coming);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        profileImage = findViewById(R.id.ic_vc_image);
        tvName = findViewById(R.id.ic_vc_name);
        acceptBtn = findViewById(R.id.ic_vc_accept);
        cancelButton = findViewById(R.id.ic_vc_cancel);

        mediaPlayer = MediaPlayer.create(this, R.raw.mp);
        currentUser = mAuth.getCurrentUser().getUid();
        senderId = getIntent().getExtras().getString("senderUid");


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(senderId)
                        .child("GoOutCall")
                        .removeValue();

                userRef.child(currentUser)
                        .child("ComingCall")
                        .removeValue();

                startActivity(new Intent(VideoCallComingActivity.this, RegisterActivity.class));
                finish();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                acceptVideoCall();
            }
        });
    }

    private void acceptVideoCall() {
        HashMap<String, Object> map = new HashMap();
        map.put("ans", "yes");
        userRef.child(senderId)
                .child("GoOutCall")
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userRef.child(currentUser)
                                .child("ComingCall")
                                .updateChildren(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(VideoCallComingActivity.this, VideoCallActivity.class);
                                        intent.putExtra("senderId", senderId);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.child(senderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mediaPlayer.start();
                            if (snapshot.hasChild("image")) {
                                String imageUrl = snapshot.child("image").getValue().toString();
                                Picasso.get().load(imageUrl).into(profileImage);
                            }
                            String name = snapshot.child("name").getValue().toString();
                            tvName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
