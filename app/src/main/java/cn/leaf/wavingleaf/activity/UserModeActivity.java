package cn.leaf.wavingleaf.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import cn.leaf.wavingleaf.adapter.UserListAdapter;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.databinding.ActivityUserModeBinding;
import cn.leaf.wavingleaf.event.UpdateUserInfoEvent;
import cn.leaf.wavingleaf.fragment.FragmentEditUser;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class UserModeActivity extends AppCompatActivity {
    ActivityUserModeBinding binding;
    RecyclerView user_list;
    FloatingActionButton add_user;
    UserDatabaseSingleton db;
    UserListAdapter adapter;
    final Handler handler=new Handler();


//    永久获取data分区的访问权限
    ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
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
//        initView();
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
        binding = ActivityUserModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user_list=binding.usersRecyclerview;
        add_user=binding.btnAdd;
    }

    private void initData(){
        db=UserDatabaseSingleton.getInstance(this);
        new Thread(() -> {
            adapter=new UserListAdapter(UserModeActivity.this, db.getSSH_UserDao().getAllSSHUsers());
            handler.post(() -> user_list.setAdapter(adapter));

        }).start();

    }

    private void initView() {

    }

    private void initAction() {
        add_user.setOnClickListener(v->{
            var edit_fragment=new FragmentEditUser(true, null, adapter);
            edit_fragment.show(getSupportFragmentManager(), "dialog");
        });
//        auto_detect.setOnClickListener(view -> {
//            config.detectMedia();
//            StringBuffer sb = new StringBuffer();
//            sb.append(config.inner_storage_path + '\n');
//            if (config.has_SD_card) {
//                sb.append(config.SD_card_path);
//            }
//            var uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Android%2Fdata");
//            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
//            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
//            launcher.launch(intent1);
//            Log.i("data", uri.toString());
//            AlertDialog d = new AlertDialog.Builder(UserModeActivity.this).setTitle("挂载的目录").setMessage(sb.toString()).setPositiveButton("ok", null).create();
//            d.show();
//            updateUI();
//        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void userInfoUpdate(UpdateUserInfoEvent u){
        var edit_fragment=new FragmentEditUser(false, u.u, adapter);
        edit_fragment.show(getSupportFragmentManager(), "dialog");
    }

}