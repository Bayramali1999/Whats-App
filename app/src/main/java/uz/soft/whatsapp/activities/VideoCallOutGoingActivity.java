package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;

public class VideoCallOutGoingActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private FloatingActionButton cancelButton;
    private TextView tvName;

    private String receiverId, currentUser;

    private MediaPlayer mediaPlayer;
    FirebaseUser user;

    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_out_going);
        profileImage = findViewById(R.id.og_vc_image);
        user = FirebaseAuth.getInstance().getCurrentUser();
        cancelButton = findViewById(R.id.ug_vc_cancel);
        tvName = findViewById(R.id.og_vc_name);
        receiverId = getIntent().getExtras().getString("receiverUserId");
        currentUser = user.getUid();
        mediaPlayer = MediaPlayer.create(this, R.raw.mp);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer.start();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                userRef.child(currentUser)
                        .child("GoOutCall")
                        .removeValue();

                userRef.child(receiverId)
                        .child("ComingCall")
                        .removeValue();

                startActivity(new Intent(VideoCallOutGoingActivity.this, RegisterActivity.class));
                finish();

            }
        });


        userRef.child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("image")) {
                                String url = snapshot.child("image").getValue().toString();
                                Picasso.get().load(url).into(profileImage);
                            }
                            String name = snapshot.child("name").getValue().toString();
                            tvName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        HashMap<String, Object> map = new HashMap<>();

        map.put("uid", receiverId);
        map.put("ans", "");

        userRef.child(currentUser)
                .child("GoOutCall")
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> hashMapp = new HashMap<>();
                            hashMapp.put("uid", currentUser);
                            hashMapp.put("ans", "");

                            userRef.child(receiverId)
                                    .child("ComingCall")
                                    .updateChildren(hashMapp)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(VideoCallOutGoingActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        userRef.child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("ComingCall")) {
                            String answer = snapshot
                                    .child("ComingCall")
                                    .child("ans").getValue().toString();
                            if (answer.equals("")) {

                            }
                            if (answer.equals("no")) {

                                mediaPlayer.stop();

                                String name = snapshot.child("name").getValue().toString();
                                Intent intent = new Intent(VideoCallOutGoingActivity.this, ChatActivity.class);
                                intent.putExtra("visit_user_id", receiverId);
                                intent.putExtra("visit_user_name", name);
                                startActivity(intent);
                                finish();
                            } else if (answer.equals("yes")) {
                                mediaPlayer.stop();

                                Intent intent = new Intent(VideoCallOutGoingActivity.this, VideoCallActivity.class);
                                intent.putExtra("visit_user_id", receiverId);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}