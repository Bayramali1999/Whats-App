package uz.soft.whatsapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.activities.ChatActivity;
import uz.soft.whatsapp.model.Contacts;

public class ChatsFragment extends Fragment {

    private View chatFragmentView;
    private RecyclerView recyclerView;

    private DatabaseReference chatRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        chatFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = chatFragmentView.findViewById(R.id.all_charted_items);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return chatFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatVH> adapter = new FirebaseRecyclerAdapter<Contacts, ChatVH>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatVH holder, int position, @NonNull Contacts model) {
                final String userId = getRef(position).getKey();
                userRef.child(userId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.hasChild("image") && getContext() != null) {
                                        String imageUrl = snapshot.child("image").getValue().toString();
                                        Glide.with(getContext())
                                                .load(imageUrl)
                                                .into(holder.imageUser);
                                    }
                                    String name = snapshot.child("name").getValue().toString();
                                    holder.tvName.setText(name);

                                    if (snapshot.hasChild("userState")) {
                                        String status = snapshot.child("userState").child("state").getValue().toString();
                                        String lastSeenTime = snapshot.child("userState").child("time").getValue().toString();
                                        String lastSeenDate = snapshot.child("userState").child("date").getValue().toString();


                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
                                        String dateNow = sd.format(calendar.getTime());
                                        if (status.equals("offline")) {
                                            if (dateNow.equals(lastSeenDate)) {
                                                holder.tvStatus.setText(lastSeenTime);
                                            } else {
                                                holder.tvStatus.setText(lastSeenDate);
                                            }
                                            holder.imageOnline.setVisibility(View.INVISIBLE);
                                        }
                                        if (status.equals("online")) {
                                            holder.tvStatus.setVisibility(View.INVISIBLE);
                                            holder.imageOnline.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        holder.tvStatus.setText("offline");
                                        holder.imageOnline.setVisibility(View.INVISIBLE);
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("visit_user_id", userId);
                                            intent.putExtra("visit_user_name", name);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @NonNull
            @Override
            public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.display_layout_f, parent, false);
                return new ChatVH(v);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    class ChatVH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        CircleImageView imageUser, imageOnline;

        public ChatVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.find_friends_name);
            tvStatus = itemView.findViewById(R.id.find_friends_status);
            imageUser = itemView.findViewById(R.id.find_friends_image);
            imageOnline = itemView.findViewById(R.id.find_friends_image_online);
        }
    }
}