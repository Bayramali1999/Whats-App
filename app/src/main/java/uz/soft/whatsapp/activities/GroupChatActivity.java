package uz.soft.whatsapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import uz.soft.whatsapp.R;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendButton;
    private TextView tv_text;
    private EditText message_et;
    private ScrollView scrollView;

    private FirebaseAuth mAuth;
    private String groupName, currentUserId, currentUserName, currentDate, currentTime;
    private DatabaseReference databaseReference, groupNameRef, groupMessageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        bindView();

        getUserInfo();

        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageInfoDB();
                message_et.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessage(snapshot);
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


    private void bindView() {
        groupName = getIntent().getStringExtra("group_name");
        currentUserId = mAuth.getCurrentUser().getUid();
        tv_text = findViewById(R.id.tv_message);
        toolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);

        sendButton = findViewById(R.id.send_message);
//        tv_text = findViewById(R.id.)
        message_et = findViewById(R.id.input_group_message);
        scrollView = findViewById(R.id.group_chat_scroll_view);

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
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
            currentDate = sd.format(calendar.getTime());

            Calendar forTime = Calendar.getInstance();
            SimpleDateFormat sdTime = new SimpleDateFormat("hh.mm a");
            currentTime = sdTime.format(forTime.getTime());

            HashMap<String, Object> hashMap = new HashMap<>();
            groupNameRef.updateChildren(hashMap);
            groupMessageRef = groupNameRef.child(pushKey);

            HashMap<String, Object> hashMessage = new HashMap<>();

            hashMessage.put("message", msg);
            hashMessage.put("date", currentDate);
            hashMessage.put("time", currentTime);
            hashMessage.put("name", currentUserName);
            groupMessageRef.updateChildren(hashMessage);
        }
    }

    private void displayMessage(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String date = ((DataSnapshot) iterator.next()).getValue().toString();
            String message = ((DataSnapshot) iterator.next()).getValue().toString();
            String name = ((DataSnapshot) iterator.next()).getValue().toString();
            String time = ((DataSnapshot) iterator.next()).getValue().toString();
            tv_text.append(name + "\n" + message + "\n" + time + "  " + date+"\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}