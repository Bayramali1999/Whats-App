package uz.soft.whatsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.listener.OnItemClickListener;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

    private ArrayList<String> list;
    private OnItemClickListener listener;

    public GroupAdapter(ArrayList<String> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        return new VH(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class VH extends RecyclerView.ViewHolder {
        private TextView tv;
        private String name;


        public VH(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv = itemView.findViewById(R.id.group_name_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.groupItemClickListener(name);
                }
            });
        }

        public void onBind(String it) {
            tv.setText(it);
            this.name = it;
        }
    }
}
