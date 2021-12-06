package uz.soft.whatsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private List<Message> list;
    private Context context;
    private String recID;

    public MessageAdapter(List<Message> list, Context applicationContext, String receiverUserId) {
        mAuth = FirebaseAuth.getInstance();
        this.list = list;
        context = applicationContext;
        recID = receiverUserId;
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

        holder.senderItems.setVisibility(View.GONE);
        holder.receiverItems.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.receiverText.setVisibility(View.GONE);
        holder.senderText.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                holder.senderItems.setVisibility(View.VISIBLE);
                holder.senderText.setVisibility(View.VISIBLE);
                holder.senderText.setText(message.getMessage());
                holder.senderTime.setText(message.getTime());
            } else {
                holder.receiverItems.setVisibility(View.VISIBLE);
                holder.receiverText.setVisibility(View.VISIBLE);
                holder.receiverText.setText(message.getMessage());
                holder.receiverTime.setText(message.getTime());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)) {
                holder.senderItems.setVisibility(View.VISIBLE);
                holder.senderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage())
                        .into(holder.senderImage);
                holder.senderTime.setText(message.getTime());
            } else {
                holder.receiverItems.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage())
                        .into(holder.receiverImage);
                holder.receiverTime.setText(message.getTime());
            }
        } else {
            if (fromUserID.equals(messageSenderId)) {
                holder.senderItems.setVisibility(View.VISIBLE);
                holder.senderImage.setVisibility(View.VISIBLE);
                holder.senderImage.setImageResource(R.drawable.file);

            } else {
                holder.receiverItems.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);
                holder.receiverImage.setImageResource(R.drawable.file);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MessageVH extends RecyclerView.ViewHolder {

        private LinearLayout senderItems;
        private LinearLayout receiverItems;
        private TextView senderText, senderTime;
        private TextView receiverText, receiverTime;
        private ImageView senderImage, receiverImage;

        public MessageVH(@NonNull View itemView) {
            super(itemView);

            receiverItems = itemView.findViewById(R.id.chat_lv_sender);
            senderItems = itemView.findViewById(R.id.caht_lv_receiver);

            senderText = itemView.findViewById(R.id.chat_sender_text);
            senderTime = itemView.findViewById(R.id.chat_sender_time);

            receiverText = itemView.findViewById(R.id.text_receiver_text);
            receiverTime = itemView.findViewById(R.id.chat_receiver_time);


            senderImage = itemView.findViewById(R.id.chat_sender_image);
            receiverImage = itemView.findViewById(R.id.chat_receiver_image);
        }
    }
}
