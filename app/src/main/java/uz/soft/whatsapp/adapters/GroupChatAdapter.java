package uz.soft.whatsapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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
        private CircleImageView circleImageView;

        public GrVH(@NonNull View itemView) {
            super(itemView);
            receiverItems = itemView.findViewById(R.id.lv_sender);
            receiverNameText = itemView.findViewById(R.id.sender_name);
            receiverText = itemView.findViewById(R.id.text_receiver_text_send);
            receiverTime = itemView.findViewById(R.id.sender_time);
            receiverImage = itemView.findViewById(R.id.chat_receiver_image);


            senderItems = itemView.findViewById(R.id.lv_receiver);
            senderNameText = itemView.findViewById(R.id.receiver_name);
            senderText = itemView.findViewById(R.id.text_reveiver_text_id);
            senderTime = itemView.findViewById(R.id.tv_receiver_tim);
            senderImage = itemView.findViewById(R.id.chat_sender_image);


        }

        public void onBind(ChatModel chatModel, String currentUser) {
            receiverItems.setVisibility(View.GONE);
            receiverImage.setVisibility(View.GONE);

            senderItems.setVisibility(View.GONE);
            senderImage.setVisibility(View.GONE);

            if (chatModel.getUid().equals(currentUser)) {

                Log.e("TAG", "onBind: " + chatModel.getName());
                Log.e("TAG", "onBind: " + chatModel.getText());
                Log.e("TAG", "onBind: " + chatModel.getUid());
                Log.e("TAG", "onBind: " + chatModel.getTime());
                senderItems.setVisibility(View.VISIBLE);
                senderNameText.setText(chatModel.getName());
                senderText.setText(chatModel.getText());
                senderTime.setText(chatModel.getTime());
            } else {

                Log.e("TAG", "onBind: " + chatModel.getName());
                Log.e("TAG", "onBind: " + chatModel.getText());
                Log.e("TAG", "onBind: " + chatModel.getUid());
                Log.e("TAG", "onBind: " + chatModel.getTime());
                receiverItems.setVisibility(View.VISIBLE);
                receiverNameText.setText(chatModel.getName());
                receiverText.setText(chatModel.getText());
                receiverTime.setText(chatModel.getTime());
            }
        }
    }
}


