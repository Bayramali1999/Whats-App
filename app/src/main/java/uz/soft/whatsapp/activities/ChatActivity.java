package uz.soft.whatsapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.MessageAdapter;
import uz.soft.whatsapp.model.Message;

public class ChatActivity extends AppCompatActivity {

    //sekinro ishlatish kere bumasa zagruska qimaskan

    private String cheker = "", myUrl = "";
    private StorageTask storageTask;
    private Uri uri;
    private String dateNow, timeNow;
    private ImageButton sentMessage, sentFiles;
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
        changeReceiverUserState();

        sentFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF",
                        "MS WORD"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Choose file type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            cheker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "select image"), 321);

                        }
                        if (i == 1) {
                            cheker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "select pdf"), 321);
                        }
                        if (i == 2) {
                            cheker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "select ms word"), 321);


                        }
                    }
                });
                builder.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 321 && data != null) {
            uri = data.getData();
            if (cheker.equals("pdf")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Ref");

                String senderMessageId = "Messages/" + senderUserId + "/" + receiverUserId;
                String receiverMessageId = "Messages/" + receiverUserId + "/" + senderUserId;
                DatabaseReference userMessage = rootRef.child("Messages")
                        .child(senderUserId)
                        .child(receiverUserId)
                        .push();
                String messagePushId = userMessage.getKey();
                StorageReference filePath = storageReference.child(messagePushId + ".pdf");

                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata()
                                .getReference()
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        myUrl = downloadUrl;
                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("message", myUrl);
                                        messageTextBody.put("type", cheker);
                                        messageTextBody.put("name", uri.getLastPathSegment());
                                        messageTextBody.put("from", senderUserId);
                                        messageTextBody.put("to", receiverUserId);
                                        messageTextBody.put("date", dateNow);
                                        messageTextBody.put("time", timeNow);

                                        Map messageDetails = new HashMap();

                                        messageDetails.put(senderMessageId + "/" + messagePushId, messageTextBody);
                                        messageDetails.put(receiverMessageId + "/" + messagePushId, messageTextBody);

                                        rootRef.updateChildren(messageDetails);
                                    }
                                });

                    }
                });

            } else if (cheker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Ref");

                String senderMessageId = "Messages/" + senderUserId + "/" + receiverUserId;
                String receiverMessageId = "Messages/" + receiverUserId + "/" + senderUserId;
                DatabaseReference userMessage = rootRef.child("Messages")
                        .child(senderUserId)
                        .child(receiverUserId)
                        .push();
                String messagePushId = userMessage.getKey();
                StorageReference filePath = storageReference.child(messagePushId + ".jpg");
                storageTask = filePath.putFile(uri);

                storageTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (task.isSuccessful()) {
                            return filePath.getDownloadUrl();
                        } else {
                            throw task.getException();
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();


                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("type", cheker);
                            messageTextBody.put("name", uri.getLastPathSegment());
                            messageTextBody.put("from", senderUserId);
                            messageTextBody.put("to", receiverUserId);
                            messageTextBody.put("date", dateNow);
                            messageTextBody.put("time", timeNow);

                            Map messageDetails = new HashMap();

                            messageDetails.put(senderMessageId + "/" + messagePushId, messageTextBody);
                            messageDetails.put(receiverMessageId + "/" + messagePushId, messageTextBody);

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

            } else {

            }
        }
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
                    messageTextBody.put("to", receiverUserId);
                    messageTextBody.put("date", dateNow);
                    messageTextBody.put("time", timeNow);

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
                                Glide.with(getApplicationContext())
                                        .load(imageUrl)
                                        .into(imageUser);
                            }
                            String userName = snapshot.child("name").getValue().toString();
                            tvName.setText(userName);
                        } else {
                            Toast.makeText(ChatActivity.this, "Snapshot ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializationData() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat sdTime = new SimpleDateFormat("hh:mm");

        dateNow = sd.format(calendar.getTime());
        timeNow = sdTime.format(calendar.getTime());

        sentFiles = findViewById(R.id.chat_send_image);
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

        adapter = new MessageAdapter(messageList, getApplicationContext(), receiverUserId);
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

    private void changeReceiverUserState() {
        chatUserRef.child(receiverUserId)
                .child("userState")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String state = snapshot.child("state").getValue().toString();
                            if (state.equals("online")) {
                                tvDate.setText(state);
                            }
                            if (state.equals("offline")) {
                                String lastSeenDate = snapshot.child("date").getValue().toString(),
                                        lastSeenTime = snapshot.child("time").getValue().toString();

                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
                                String currentDate = sd.format(calendar.getTime());

                                if (currentDate.equals(lastSeenDate)) {
                                    tvDate.setText(lastSeenTime);
                                }
                                if (!currentDate.equals(lastSeenDate)) {
                                    tvDate.setText(lastSeenDate);
                                }
                            }

                        } else {
                            tvDate.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}