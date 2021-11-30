package uz.soft.whatsapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private Context context;

    public MessageAdapter(List<Message> list, Context applicationContext) {
        mAuth = FirebaseAuth.getInstance();
        this.list = list;
        context = applicationContext;
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
                        Glide.with(context)
                                .load(imageUrl)
                                .into(holder.userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        holder.messageReceiver.setVisibility(View.GONE);
        holder.userImage.setVisibility(View.GONE);
        holder.messageSender.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                holder.messageSender.setVisibility(View.VISIBLE);
                holder.messageSender.setBackgroundResource(R.drawable.sender_message_layout);
                holder.messageSender.setTextColor(Color.BLACK);
                holder.messageSender.setText(message.getMessage() + "\n" + message.getTime());
            } else {

                holder.messageReceiver.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);

                holder.messageReceiver.setBackgroundResource(R.drawable.receive_message_layout);
                holder.messageReceiver.setTextColor(Color.BLACK);
                holder.messageReceiver.setText(message.getMessage() + "\n" + message.getTime());
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
        ImageView senderImage, receiverImage;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            messageReceiver = itemView.findViewById(R.id.text_receiver_text);
            messageSender = itemView.findViewById(R.id.text_sender_text);
            userImage = itemView.findViewById(R.id.chat_user_image);

            senderImage = itemView.findViewById(R.id.chat_sender_image);
            receiverImage = itemView.findViewById(R.id.chat_receiver_image);
        }
    }
}
