package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.FirebaseSimpleAdapter;
import uz.soft.whatsapp.listener.OnItemClickListener;
import uz.soft.whatsapp.model.Contacts;

public class FindFriendsActivity extends AppCompatActivity implements OnItemClickListener {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseSimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = findViewById(R.id.find_friends_toolbar);
        recyclerView = findViewById(R.id.find_friends_rv);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(databaseReference, Contacts.class)
                .build();

        adapter = new FirebaseSimpleAdapter(options, this);

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void groupItemClickListener(String name) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("visit_user_id", name);
        startActivity(intent);
    }
}
