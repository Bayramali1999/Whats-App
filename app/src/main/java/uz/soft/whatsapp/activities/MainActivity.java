package uz.soft.whatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.TabAccessAdapter;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager myViewPager;
    private TabLayout mTabLayout;
    private TabAccessAdapter myTabAccessAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindData();
    }

    private void bindData() {
        toolbar = findViewById(R.id.main_page_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wats app");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabAccessAdapter = new TabAccessAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(myViewPager);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.find_friends:
                findFriends();
                break;
            case R.id.settings:
                openSettings();
                break;
            case R.id.log_out:
                logOut();
                break;
        }
        return true;
    }

    private void logOut() {
        mAuth.signOut();
        goToLoginActivity();
    }

    private void openSettings() {

    }

    private void findFriends() {

    }
}