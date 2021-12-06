package uz.soft.whatsapp.activities;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import uz.soft.whatsapp.R;

public class VideoCallActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener {


    private static String TOKEN = "T1==cGFydG5lcl9pZD00NzM5NjM3MSZzaWc9NGIwMjgxNWM2ZDcxYjg2MTU2ODU4MzIzOTQwZDM4MGExMDk4YWU1ZTpzZXNzaW9uX2lkPTFfTVg0ME56TTVOak0zTVg1LU1UWXpPRFV4TVRjek5UQTNNSDV0WTNSRVQwbDBUMXBuWkRocU9VcHVZVmRZYkhwa1QycC1mZyZjcmVhdGVfdGltZT0xNjM4NTExNzg5Jm5vbmNlPTAuMzg5MjM0OTI0MTgyNDQ1NTQmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTY0MTEwMzc4OCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static String API_KEY = "47396371";
    private static String SESSION_ID = "1_MX40NzM5NjM3MX5-MTYzODUxMTczNTA3MH5tY3RET0l0T1pnZDhqOUpuYVdYbHpkT2p-fg";
    private static String LOG_TAG = VideoCallActivity.class.getSimpleName();
    private static final int RC_CALL_ID = 141;

    private FrameLayout mPubController, mSubController;
    private FloatingActionButton cancelBtn;
    private DatabaseReference userRef;
    private String userId;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        cancelBtn = findViewById(R.id.vc_cancel_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(userId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("ComingCall")) {
                                    userRef.child(userId).child("ComingCall")
                                            .removeValue();
                                    if (mPublisher != null) {
                                        mPublisher.destroy();
                                    }
                                    if (mSubscriber != null) {
                                        mSubscriber.destroy();
                                    }
                                    startActivity(new Intent(VideoCallActivity.this, RegisterActivity.class));
                                    finish();
                                }
                                if (snapshot.hasChild("GoOutCall")) {
                                    userRef.child(userId).child("GoOutCall")
                                            .removeValue();
                                    if (mPublisher != null) {
                                        mPublisher.destroy();
                                    }
                                    if (mSubscriber != null) {
                                        mSubscriber.destroy();
                                    }
                                    startActivity(new Intent(VideoCallActivity.this, RegisterActivity.class));
                                    finish();

                                } else {
                                    if (mPublisher != null) {
                                        mPublisher.destroy();
                                    }
                                    if (mSubscriber != null) {
                                        mSubscriber.destroy();
                                    }
                                    startActivity(new Intent(VideoCallActivity.this, RegisterActivity.class));
                                    finish();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);
    }

    @AfterPermissionGranted(RC_CALL_ID)
    public void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this, perms)) {
            mPubController = findViewById(R.id.pub_container);
            mSubController = findViewById(R.id.sub_container);

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoCallActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Need Camera and audi permission...", RC_CALL_ID, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPubController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {


        Log.i(LOG_TAG, "onStreamReceived: receiver");

        if (mSubscriber == null) {

            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubController.addView(mSubscriber.getView());

        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "onStreamReceived: dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}