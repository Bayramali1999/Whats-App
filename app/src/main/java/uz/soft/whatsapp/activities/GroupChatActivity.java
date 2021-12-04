package uz.soft.whatsapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.GroupChatAdapter;
import uz.soft.whatsapp.model.ChatModel;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendButton;
    private EditText message_et;

    private FirebaseAuth mAuth;
    private String groupName, currentUserId, currentUserName, currentTime;
    private DatabaseReference databaseReference, groupNameRef, groupMessageRef;
    private GroupChatAdapter groupAdapter;
    private final List<ChatModel> charList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        recyclerView = findViewById(R.id.group_chat_rv);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        groupAdapter = new GroupChatAdapter(charList, mAuth.getCurrentUser().getUid());
        recyclerView.setAdapter(groupAdapter);
        bindView();

        getUserInfo();

        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageInfoDB();
                message_et.setText("");
                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);//
                //rv scroll to bottom
                recyclerView.scrollToPosition(charList.size() - 1);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessage(snapshot);

                    recyclerView.scrollToPosition(charList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessage(snapshot);

                    recyclerView.scrollToPosition(charList.size() - 1);
                }
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


    @SuppressLint("RestrictedApi")
    private void bindView() {
        groupName = getIntent().getStringExtra("group_name");
        currentUserId = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        sendButton = findViewById(R.id.send_message);
        message_et = findViewById(R.id.input_group_message);

    }


    private void getUserInfo() {
        databaseReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageInfoDB() {
        String msg = message_et.getText().toString();
        String pushKey = groupNameRef.push().getKey();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Please write message", Toast.LENGTH_SHORT).show();
        } else {
            Calendar forTime = Calendar.getInstance();
            SimpleDateFormat sdTime = new SimpleDateFormat("hh.mm a");
            currentTime = sdTime.format(forTime.getTime());

            HashMap<String, Object> hashMap = new HashMap<>();
            groupNameRef.updateChildren(hashMap);
            groupMessageRef = groupNameRef.child(pushKey);

            HashMap<String, Object> hashMessage = new HashMap<>();

            hashMessage.put("message", msg);
            hashMessage.put("uid", mAuth.getCurrentUser().getUid());
            hashMessage.put("time", currentTime);
            hashMessage.put("name", currentUserName);
            groupMessageRef.updateChildren(hashMessage);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayMessage(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String message = ((DataSnapshot) iterator.next()).getValue().toString();
            String name = ((DataSnapshot) iterator.next()).getValue().toString();
            String time = ((DataSnapshot) iterator.next()).getValue().toString();
            String uid = ((DataSnapshot) iterator.next()).getValue().toString();

            charList.add(new ChatModel(name, message, time, uid));
        }
        groupAdapter.notifyDataSetChanged();
    }
}