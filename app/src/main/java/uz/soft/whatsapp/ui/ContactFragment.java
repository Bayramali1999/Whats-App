package uz.soft.whatsapp.ui;

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

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.FirebaseSimpleAdapter;
import uz.soft.whatsapp.model.Contacts;

public class ContactFragment extends Fragment {

    private View currentView;
    private FirebaseSimpleAdapter adapter;
    private RecyclerView recyclerView;

    private String currentUserId;
    private DatabaseReference databaseReference, userRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentView = inflater.inflate(R.layout.fragment_contact, container, false);

        recyclerView = currentView.findViewById(R.id.contacts_rv);
        currentUserId = mAuth.getCurrentUser().getUid().toString();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return currentView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options = new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(databaseReference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, VH> adapter = new FirebaseRecyclerAdapter<Contacts, VH>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VH holder, int position, @NonNull Contacts model) {

                String userId = getRef(position).getKey();
                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("image") && snapshot.exists()) {
                            String image = snapshot.child("image").getValue().toString();
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();


                            holder.tvName.setText(name);
                            holder.tvStatus.setText(status);
                            Glide.with(holder.view.getContext())
                                    .load(image)
                                    .into(holder.imageUser);
                        } else {
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            holder.tvName.setText(name);
                            holder.tvStatus.setText(status);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_layout_f, parent, false);
                return new VH(v);
            }
        };


        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        CircleImageView imageUser, imageOnline;
        View view;

        public VH(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.find_friends_name);
            tvStatus = itemView.findViewById(R.id.find_friends_status);
            imageUser = itemView.findViewById(R.id.find_friends_image);
            imageOnline = itemView.findViewById(R.id.find_friends_image_online);

        }
    }

}