package uz.soft.whatsapp.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.MessageAdapter;
import uz.soft.whatsapp.model.Message;

public class ChatActivity extends AppCompatActivity {

    private ImageButton sentMessage;
    private EditText textMessage;
    private RecyclerView recyclerChat;
    private CircleImageView imageUser;
    private TextView tvName, tvDate;
    private Toolbar toolbar;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    private String receiverUserId, thisUserName, senderUserId;

    private FirebaseAuth mAuth;
    private DatabaseReference chatUserRef, rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializationData();
        retrieveData();
        sentMessageAction();
    }

    private void sentMessageAction() {
        sentMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "place write message", Toast.LENGTH_SHORT).show();
                } else {
                    String messageSenderRefId = "Messages/" + senderUserId + "/" + receiverUserId;
                    String messageReceiverRefId = "Messages/" + receiverUserId + "/" + senderUserId;

                    DatabaseReference messageRef = rootRef.child("Messages").
                            child(senderUserId)
                            .child(receiverUserId)
                            .push();

                    String pusId = messageRef.getKey();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", message);
                    messageTextBody.put("type", "text");
                    messageTextBody.put("from", senderUserId);

                    Map messageDetails = new HashMap();

                    messageDetails.put(messageSenderRefId + "/" + pusId, messageTextBody);
                    messageDetails.put(messageReceiverRefId + "/" + pusId, messageTextBody);

                    rootRef.updateChildren(messageDetails)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Message Sent successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                    textMessage.setText("");
                                }
                            });
                }
            }
        });
    }

    private void retrieveData() {
        chatUserRef.child(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("image")) {
                                String imageUrl = snapshot.child("image").getValue().toString();
                                Glide.with(ChatActivity.this)
                                        .load(imageUrl)
                                        .into(imageUser);
                            }
                            String userName = snapshot.child("name").getValue().toString();
                            tvName.setText(userName);
                            tvDate.setText("Last seen recently");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializationData() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        toolbar = findViewById(R.id.chat_toolbar);
        chatUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserId = getIntent().getStringExtra("visit_user_id");
        thisUserName = getIntent().getStringExtra("visit_user_name");

        sentMessage = findViewById(R.id.chat_send_button);
        textMessage = findViewById(R.id.chat_message);
        recyclerChat = findViewById(R.id.chat_rv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_custom_toolbar, null);
        getSupportActionBar().setCustomView(view);


        imageUser = findViewById(R.id.chat_user_image);
        tvName = findViewById(R.id.chat_user_name);
        tvDate = findViewById(R.id.chat_user_seen_date);

        adapter = new MessageAdapter(messageList);
        recyclerChat.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages")
                .child(senderUserId)
                .child(receiverUserId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        messageList.add(message);
                        adapter.notifyDataSetChanged();
                        recyclerChat.smoothScrollToPosition(recyclerChat.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}