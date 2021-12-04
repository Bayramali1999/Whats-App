package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private SearchView searchView = null;
    private String str = "";


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
        FirebaseRecyclerOptions<Contacts> options = null;
        if (str.equals("")) {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(databaseReference,
                            Contacts.class)
                    .build();
        } else {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(databaseReference
                                    .orderByChild("name")
                                    .startAt(str)
                                    .endAt(str + "\uf8ff"),
                            Contacts.class)
                    .build();

        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_freind_menu, menu);
        MenuItem.OnActionExpandListener listener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        };

        menu.findItem(R.id.find_freind_search).setOnActionExpandListener(listener);
        searchView = (SearchView) menu.findItem(R.id.find_freind_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    Toast.makeText(FindFriendsActivity.this, "Please Write ", Toast.LENGTH_SHORT).show();
                    str = "";

                } else {
                    str = newText;
                }
                onStart();

                return true;
            }
        });
        return true;
    }
}
