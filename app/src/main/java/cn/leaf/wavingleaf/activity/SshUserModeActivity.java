package cn.leaf.wavingleaf.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.leaf.wavingleaf.adapter.UserListAdapter;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.databinding.ActivitySshUserModeBinding;
import cn.leaf.wavingleaf.event.UpdateUserInfoEvent;
import cn.leaf.wavingleaf.fragment.FragmentEditUser;

public class SshUserModeActivity extends AppCompatActivity {
    ActivitySshUserModeBinding binding;
    RecyclerView user_list;
    FloatingActionButton add_user;
    UserDatabaseSingleton db;
    UserListAdapter adapter;
    final Handler handler=new Handler();


//    永久获取data分区的访问权限。暂时保留，未使用
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Uri uri=result.getData().getData();
                getContentResolver().takePersistableUriPermission(uri, result.getData().getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            }
        }
    });


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
        binding = ActivitySshUserModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user_list=binding.usersRecyclerview;
        add_user=binding.btnAdd;
    }

    private void initData(){
        db=UserDatabaseSingleton.getInstance(this);
        new Thread(() -> {
            adapter=new UserListAdapter(SshUserModeActivity.this, db.getUserDao().getAllSFTPUsers());
            handler.post(() -> user_list.setAdapter(adapter));
        }).start();

    }

    private void initAction() {
        add_user.setOnClickListener(v->{
            var edit_fragment=new FragmentEditUser(true, null, adapter);
            edit_fragment.show(getSupportFragmentManager(), "dialog");
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userInfoUpdate(UpdateUserInfoEvent u){
        var edit_fragment=new FragmentEditUser(false, u.u, adapter);
        edit_fragment.show(getSupportFragmentManager(), "dialog");
    }

}