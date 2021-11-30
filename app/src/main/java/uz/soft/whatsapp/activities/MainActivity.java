package uz.soft.whatsapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import uz.soft.whatsapp.R;
import uz.soft.whatsapp.adapters.TabAccessAdapter;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager myViewPager;
    private TabLayout mTabLayout;
    private TabAccessAdapter myTabAccessAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            goToLoginActivity();
        } else {
            verifyUserNameAndStatus();
            updateUserState("online");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null) {
            updateUserState("offline");
        }
    }


    private void verifyUserNameAndStatus() {
        String uid = mAuth.getCurrentUser().getUid();
        databaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name").exists())) {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    openSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
            case R.id.create_group:
                createNewGroup();
                break;
        }
        return true;
    }

    private void createNewGroup() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialog);
        final EditText editText = new EditText(this);
        editText.setHint("e.g Ali game");
        dialog.setView(editText);
        dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = editText.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "We need", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroupByName(groupName);
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        dialog.show();
    }

    private void createNewGroupByName(String groupName) {
        databaseReference.child("Groups").child(groupName)
                .setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    }
                });
    }

    private void logOut() {
        mAuth.signOut();
        goToLoginActivity();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void findFriends() {
        Intent intent = new Intent(this, FindFriendsActivity.class);
        startActivity(intent);
    }

    private void updateUserState(String state) {
        String currentDate, currentTime;
        Calendar data = Calendar.getInstance();
        SimpleDateFormat sdDate = new SimpleDateFormat("dd.MM.yyyy");

        SimpleDateFormat sdTime = new SimpleDateFormat("hh:mm");

        currentDate = sdDate.format(data.getTime());
        currentTime = sdTime.format(data.getTime());

        HashMap<String, Object> statusHash = new HashMap<>();

        statusHash.put("date", currentDate);
        statusHash.put("time", currentTime);
        statusHash.put("state", state);

        String currentUser = mAuth.getCurrentUser().getUid();
        databaseReference.child("Users")
                .child(currentUser)
                .child("userState")
                .updateChildren(statusHash);

    }
}