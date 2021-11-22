package uz.soft.whatsapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.activities.GroupChatActivity;
import uz.soft.whatsapp.adapters.GroupAdapter;
import uz.soft.whatsapp.listener.OnItemClickListener;

public class GroupFragment extends Fragment {

    private View contentView;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private ArrayList<String> arrayList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private OnItemClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_group, container, false);

        bindView();

        retrieveAndDisplayData();

        return contentView;
    }

    private void bindView() {

        listener = new OnItemClickListener() {
            @Override
            public void groupItemClickListener(String name) {
                Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                intent.putExtra("group_name", name);
                startActivity(intent);
            }
        };

        recyclerView = contentView.findViewById(R.id.rv_group);
        adapter = new GroupAdapter(arrayList, listener);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");

    }

    private void retrieveAndDisplayData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();

                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                arrayList.clear();
                arrayList.addAll(set);
                Log.e("TAG", "onDataChange: " + arrayList.get(0));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}