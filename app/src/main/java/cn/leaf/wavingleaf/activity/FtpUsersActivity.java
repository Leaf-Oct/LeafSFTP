package cn.leaf.wavingleaf.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.adapter.FTPUserListAdapter;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.databinding.ActivityFtpUsersBinding;
import cn.leaf.wavingleaf.event.UpdateFTPUserInfoEvent;
import cn.leaf.wavingleaf.event.UpdateUserInfoEvent;
import cn.leaf.wavingleaf.fragment.FragmentEditFTPUser;
import cn.leaf.wavingleaf.fragment.FragmentEditUser;

public class FtpUsersActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityFtpUsersBinding binding;
    RecyclerView user_list;
    FloatingActionButton add_user;
    UserDatabaseSingleton db;
    FTPUserListAdapter adapter;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        initData();
        initAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    private void bindView() {
        binding = ActivityFtpUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user_list = binding.ftpUsersRecyclerview;
        add_user = binding.btnAddFtpUser;
    }

    private void initData() {
        db = UserDatabaseSingleton.getInstance(this);
        new Thread(() -> {
            adapter = new FTPUserListAdapter(FtpUsersActivity.this, db.getUserDao().getAllFTPUsers());
            handler.post(() -> user_list.setAdapter(adapter));
        }).start();
    }

    private void initAction() {
        add_user.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new FragmentEditFTPUser(true, null, adapter).show(getSupportFragmentManager(), "dialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userInfoUpdate(UpdateFTPUserInfoEvent u) {
        new FragmentEditFTPUser(false, u.user, adapter).show(getSupportFragmentManager(), "dialog");
    }
}