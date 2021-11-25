package uz.soft.whatsapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uz.soft.whatsapp.R;
import uz.soft.whatsapp.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private List<Message> list;

    public MessageAdapter(List<Message> list) {
        mAuth = FirebaseAuth.getInstance();
        this.list = list;
    }

    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_chat_item, parent, false);
        return new MessageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = list.get(position);
        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("image")) {
                        String imageUrl = snapshot.child("image").getValue().toString();
                        Glide.with(holder.itemView.getContext())
                                .load(imageUrl)
                                .into(holder.userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        if (fromMessageType.equals("text")) {
            holder.messageReceiver.setVisibility(View.INVISIBLE);
            holder.userImage.setVisibility(View.INVISIBLE);
            holder.messageSender.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderId)) {
                holder.messageSender.setVisibility(View.VISIBLE);
                holder.messageSender.setBackgroundResource(R.drawable.sender_message_layout);
                holder.messageSender.setTextColor(Color.BLACK);
                holder.messageSender.setText(message.getMessage());
            } else {

                holder.messageReceiver.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);

                holder.messageReceiver.setBackgroundResource(R.drawable.receive_message_layout);
                holder.messageReceiver.setTextColor(Color.BLACK);
                holder.messageReceiver.setText(message.getMessage());

            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MessageVH extends RecyclerView.ViewHolder {
        TextView messageSender, messageReceiver;
        CircleImageView userImage;

        public MessageVH(@NonNull View itemView) {
            super(itemView);

            messageReceiver = itemView.findViewById(R.id.text_receiver_text);
            messageSender = itemView.findViewById(R.id.text_sender_text);
            userImage = itemView.findViewById(R.id.chat_user_image);

        }
    }
}
