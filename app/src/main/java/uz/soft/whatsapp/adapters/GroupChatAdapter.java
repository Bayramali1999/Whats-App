package uz.soft.whatsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.model.ChatModel;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GrVH> {

    private List<ChatModel> list;
    private String currentUser;

    public GroupChatAdapter(List<ChatModel> list, String currentUser) {
        this.list = list;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public GrVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gr_item_view, parent, false);
        return new GrVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GrVH holder, int position) {
        holder.onBind(list.get(position), currentUser);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class GrVH extends RecyclerView.ViewHolder {
        private LinearLayout senderItems;
        private LinearLayout receiverItems;
        private TextView senderNameText, senderText, senderTime;
        private TextView receiverNameText, receiverText, receiverTime;
        private ImageView senderImage, receiverImage;

        public GrVH(@NonNull View itemView) {
            super(itemView);
            receiverItems = itemView.findViewById(R.id.lv_sender);
            receiverNameText = itemView.findViewById(R.id.sender_name);
            receiverText = itemView.findViewById(R.id.text_receiver_text_send);
            receiverTime = itemView.findViewById(R.id.sender_time);
            receiverImage = itemView.findViewById(R.id.chat_sender_image_i);
            senderItems = itemView.findViewById(R.id.lv_receiver);
            senderNameText = itemView.findViewById(R.id.receiver_name);
            senderText = itemView.findViewById(R.id.text_reveiver_text_id);
            senderTime = itemView.findViewById(R.id.tv_receiver_tim);
            senderImage = itemView.findViewById(R.id.chat_rec_image_i);

        }

        public void onBind(ChatModel chatModel, String currentUser) {
            receiverItems.setVisibility(View.GONE);
            senderItems.setVisibility(View.GONE);

            if (chatModel.getUid().equals(currentUser)) {
                senderItems.setVisibility(View.VISIBLE);
                senderNameText.setText(chatModel.getName());
                senderTime.setText(chatModel.getTime());
                if (chatModel.getType().equals("text")) {
                    senderImage.setVisibility(View.GONE);
                    senderText.setVisibility(View.VISIBLE);
                    senderText.setText(chatModel.getText());
                }
                if (chatModel.getType().equals("image")) {
                    senderImage.setVisibility(View.VISIBLE);
                    senderText.setVisibility(View.GONE);
//                    senderImageContainer.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(chatModel.getText())
                            .into(senderImage);
                }
            } else {
                receiverItems.setVisibility(View.VISIBLE);
                receiverTime.setText(chatModel.getTime());
                receiverNameText.setText(chatModel.getName());
                if (chatModel.getType().equals("text")) {
                    receiverImage.setVisibility(View.GONE);
                    receiverText.setVisibility(View.VISIBLE);
                    receiverText.setText(chatModel.getText());
                }
                if (chatModel.getType().equals("image")) {
                    receiverImage.setVisibility(View.VISIBLE);
                    receiverText.setVisibility(View.GONE);
//                    receiverImageContainer.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(chatModel.getText())
                            .into(receiverImage);
                }
            }
        }
    }
}


