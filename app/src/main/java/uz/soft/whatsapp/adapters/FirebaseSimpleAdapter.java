package uz.soft.whatsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.listener.OnItemClickListener;
import uz.soft.whatsapp.model.Contacts;

public class FirebaseSimpleAdapter extends FirebaseRecyclerAdapter<Contacts, FirebaseSimpleAdapter.FirebaseVH> {

    private OnItemClickListener listener;

    public FirebaseSimpleAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseVH holder,
                                    int position, @NonNull Contacts model) {
        getRef(position).child("userState")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("state")) {
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
                            String dateNow = sd.format(calendar.getTime());

                            String status = snapshot.child("state").getValue().toString();
                            String lastSeenDate = snapshot.child("date").getValue().toString();
                            String lastSeenTime = snapshot.child("time").getValue().toString();

                            if (status.equals("online")) {
                                holder.tvStatus.setText(status);
                            }
                            if (status.equals("offline")) {
                                if (lastSeenDate.equals(dateNow)) {
                                    holder.tvStatus.setText(lastSeenTime);
                                } else {
                                    holder.tvStatus.setText(lastSeenDate);
                                }
                            }
                        } else {
                            holder.tvStatus.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.onBind(model, position, listener);

    }


    @NonNull
    @Override
    public FirebaseVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.display_layout_f, parent, false);
        return new FirebaseVH(v);
    }

    class FirebaseVH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        CircleImageView imageUser, imageOnline;
        View view;

        public FirebaseVH(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.find_friends_name);
            tvStatus = itemView.findViewById(R.id.find_friends_status);
            imageUser = itemView.findViewById(R.id.find_friends_image);
            imageOnline = itemView.findViewById(R.id.find_friends_image_online);

        }

        public void onBind(Contacts model, int position, OnItemClickListener listener) {

            tvName.setText(model.getName());
            tvStatus.setText(model.getStatus());
            if (model.getImage() == null) {
                imageUser.setImageResource(R.drawable.profile_image);
            } else {
                Glide.with(view.getContext()).load(model.getImage()).into(imageUser);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.groupItemClickListener(getRef(position).getKey());
                }
            });

        }
    }
}
