package uz.soft.whatsapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, senderUserId, current_state;
    private DatabaseReference databaseReference, chatReference, contactRef;
    private FirebaseAuth mAuth;

    private TextView tvName, tvStatus;
    private CircleImageView profileImage;
    private Button button, cancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.visit_user_name);
        tvStatus = findViewById(R.id.visit_user_status);
        profileImage = findViewById(R.id.visit_user_image);
        button = findViewById(R.id.visit_user_send_message);
        cancelRequest = findViewById(R.id.cancel_user_send_message);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().getString("visit_user_id");

        retrieveData();

        if (receiverUserId.equals(senderUserId)) {
            button.setVisibility(View.INVISIBLE);
        }
    }

    private void retrieveData() {
        databaseReference
                .child(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && (snapshot.hasChild("image"))) {
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();
                            String image = snapshot.child("image").getValue().toString();

                            tvName.setText(name);
                            tvStatus.setText(status);
                            Glide.with(ProfileActivity.this)
                                    .load(image)
                                    .into(profileImage);
                            manageRequest();
                        } else {
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            tvName.setText(name);
                            tvStatus.setText(status);
                            profileImage.setImageResource(R.drawable.profile_image);

                            manageRequest();

                        }
                        current_state = "new";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void manageRequest() {

        chatReference.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                    String req_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if (req_type.equals("sent")) {
                        current_state = "request_sent";
                        button.setText("Cancel chat request");
                    }
                    if (req_type.equals("receiver")) {
                        current_state = "request_receiver";
                        button.setText("Accept chat request");
                        cancelRequest.setVisibility(View.VISIBLE);
                        cancelRequest.setEnabled(true);

                        cancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();

                            }
                        });
                    }
                } else {
                    contactRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(receiverUserId)) {
                                        current_state = "friend";
                                        button.setText("Remove this contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (receiverUserId.equals(senderUserId)) {
            button.setVisibility(View.INVISIBLE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button.setEnabled(false);

                    if (current_state.equals("new")) {
                        sendCharRequest();
                    } else if (current_state.equals("request_sent")) {
                        cancelRequest();
                    } else if (current_state.equals("request_receiver")) {
                        acceptChatRequest();
                    } else if (current_state.equals("friend")) {
                        cancelContactReq();
                    }
                }
            });
        }
    }

    private void cancelContactReq() {
        contactRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        contactRef.child(receiverUserId)
                                .child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            button.setEnabled(true);
                                            current_state = "new";
                                            button.setText("Sent message");
                                            cancelRequest.setVisibility(View.INVISIBLE);
                                            cancelRequest.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void acceptChatRequest() {
        contactRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            chatReference.child(senderUserId).child(receiverUserId)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                chatReference.child(receiverUserId).child(senderUserId)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                button.setEnabled(true);
                                                                                button.setText("Remove this contact");
                                                                                current_state = "friend";
                                                                                cancelRequest.setVisibility(View.INVISIBLE);
                                                                                cancelRequest.setEnabled(false);
                                                                            }
                                                                        });

                                                            }
                                                        }
                                                    });
                                        }
                                    });

                        }
                    }
                });
    }

    private void cancelRequest() {
        chatReference.child(senderUserId)
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        chatReference.child(receiverUserId)
                                .child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            button.setEnabled(true);
                                            current_state = "new";
                                            button.setText("Sent message");
                                            cancelRequest.setVisibility(View.INVISIBLE);
                                            cancelRequest.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void sendCharRequest() {
        chatReference.child(senderUserId)
                .child(receiverUserId)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatReference.child(receiverUserId)
                                    .child(senderUserId)
                                    .child("request_type")
                                    .setValue("receiver")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            button.setEnabled(true);
                                            current_state = "request_sent";
                                            button.setText("Cancel chat request");
                                        }
                                    });
                        }
                    }
                });
    }

}