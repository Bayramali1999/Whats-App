package uz.soft.whatsapp.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import uz.soft.whatsapp.model.Contacts;


public class RequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference, userRef, contactRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.requests_rv);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        currentUserId = mAuth.getCurrentUser().getUid();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(databaseReference.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ReqVH> adapter =
                new FirebaseRecyclerAdapter<Contacts, ReqVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ReqVH holder,
                                                    int position, @NonNull Contacts model) {

                        final String sender_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String value = snapshot.getValue().toString();

                                    if (value.equals("receiver")) {
                                        userRef.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image") && snapshot.exists()) {
                                                    String imageUrl = snapshot.child("image").getValue().toString();
                                                    Glide.with(getActivity().getApplicationContext())
                                                            .load(imageUrl)
                                                            .into(holder.imageUser);
                                                }
                                                String nameUser = snapshot.child("name").getValue().toString();

                                                holder.tvName.setText(nameUser);
                                                holder.tvStatus.setText("Wants connected you a or c");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence[] options = new CharSequence[]{
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                        if (getContext() != null) {
                                                            AlertDialog.Builder builder = new AlertDialog
                                                                    .Builder(getContext());
                                                            builder.setTitle(nameUser + "Char request");

                                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    if (i == 0) {
                                                                        contactRef
                                                                                .child(currentUserId)
                                                                                .child(sender_user_id)
                                                                                .child("Contact")
                                                                                .setValue("Saved")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            contactRef
                                                                                                    .child(sender_user_id)
                                                                                                    .child(currentUserId).child("Contact")
                                                                                                    .setValue("Saved")
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                removeRequestFromFirebase(sender_user_id);
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                    if (i == 1) {
                                                                        removeRequestFromFirebase(sender_user_id);
                                                                    }

                                                                }
                                                            });
                                                            builder.show();
                                                        }


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    if (value.equals("sent")) {
                                        userRef.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image") && snapshot.exists()) {
                                                    String imageUrl = snapshot.child("image").getValue().toString();
                                                    if (getContext() != null) {
                                                        Glide.with(getContext())
                                                                .load(imageUrl)
                                                                .into(holder.imageUser);
                                                    }
                                                }
                                                String nameUser = snapshot.child("name").getValue().toString();

                                                holder.tvName.setText(nameUser);
                                                holder.tvStatus.setText("Wants connected you a or c");
                                                holder.cancelBtn.setVisibility(View.GONE);
                                                holder.acceptBtn.setText("Cancel req");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence[] options = new CharSequence[]{
                                                                "Cancel"
                                                        };

                                                        AlertDialog.Builder builder = new AlertDialog
                                                                .Builder(getContext());
                                                        builder.setTitle(nameUser + "Char request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                removeRequestFromFirebase(sender_user_id);
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ReqVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_req_layout, parent, false);
                        return new ReqVH(v);
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void removeRequestFromFirebase(String sender_user_id) {
        databaseReference
                .child(currentUserId)
                .child(sender_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReference
                                .child(sender_user_id)
                                .child(currentUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
//                                                                                                                                                adapter.notifyDataSetChanged();
                                            Toast.makeText(getContext(), "New Contact saved", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });


    }


    class ReqVH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        CircleImageView imageUser;
        View view;
        Button acceptBtn, cancelBtn;

        public ReqVH(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.req_friends_name);
            tvStatus = itemView.findViewById(R.id.req_friends_status);
            imageUser = itemView.findViewById(R.id.req_friends_image);
            acceptBtn = itemView.findViewById(R.id.user_req_accept);
            cancelBtn = itemView.findViewById(R.id.user_req_remove);
        }
    }
}