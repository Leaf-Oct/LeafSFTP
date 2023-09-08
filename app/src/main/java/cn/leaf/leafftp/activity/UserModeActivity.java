package cn.leaf.leafftp.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;

import cn.leaf.leafftp.databinding.ActivityUserModeBinding;
import cn.leaf.leafftp.sharedpreferences.Config;

public class UserModeActivity extends AppCompatActivity {
    ActivityUserModeBinding binding;
    SwitchCompat mode_switch, ssl_switch;
    View switch_area;
    View ssl_area;
    ImageButton info_button, auto_detect;
    View custom_area;
    View inner_storage_area, sd_card_area;
    TextView custom_path_textview, inner_path_textview, sd_path_textview;
    Config config = Config.getInstance();


    ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Uri uri=result.getData().getData();
                getContentResolver().takePersistableUriPermission(uri, result.getData().getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            }
        }
    });

    ActivityResultLauncher custom_launcher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        var i=result.getData();
//        未选择
        if(i==null){
            return;
        }
        var uri=i.getData();
        if(uri==null){
            return;
        }
//        选择的文件夹是内部存储还是SD卡需要不同处理
//        内部存储的uri.getPath()是/tree/primary:开头
//        而SD卡会是/tree/XXXX-XXXX这种形式开头
        var uri_path=uri.getPath();
        if(uri_path.startsWith("/tree/primary:")){
            uri_path=uri_path.replace("/tree/primary:", config.inner_storage_path+"/");
        }
        else if(uri_path.startsWith("/tree")&&config.has_SD_card){
            uri_path=config.SD_card_path+File.separator+uri_path.substring(uri_path.indexOf(":")+1);
        }
        else {
            uri_path="dir path error!!!";
        }
        Log.w("path", uri_path);
        config.custom_path=uri_path;
        updateUI();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        initView();
        initAction();
    }

    private void bindView() {
        binding = ActivityUserModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mode_switch = binding.modeSwitch;
        ssl_switch = binding.enableSsl;
        switch_area = binding.modeSwitchArea;
        ssl_area = binding.sslArea;
        info_button = binding.modeInfo;
        custom_area = binding.customPathArea;
        inner_storage_area = binding.innerPathArea;
        sd_card_area = binding.sdPathArea;
        custom_path_textview = binding.customPathTextview;
        inner_path_textview = binding.innerPathTextview;
        sd_path_textview = binding.sdPathTextview;
        auto_detect = binding.autoDetect;
    }

    private void initView() {
        if (config.ssl_enable) {
            ssl_switch.setChecked(true);
        }
        config.detectMedia();
        updateUI();
    }

    private void initAction() {
        info_button.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(UserModeActivity.this).setTitle("用户模式说明").setMessage("Leaf模式: 开发者自己的常用模式, 通过in和sd两个用户名登录(密码也为in sd)并读写内部存储和SD卡\ncustom模式: 自定义路径模式, 选一个路径, 匿名读写\n因为使用场景是局域网内, 所以怎么简单怎么来, 安全性可以放一边.\n此外，SSL配置的相关代码有问题未解决，因此SSL功能暂未启用").setPositiveButton("ok", null).create();
            dialog.show();
        });
//        解决了SSL问题再启用
//        ssl_area.setOnClickListener(view -> {
//            if (config.ssl_enable) {
//                config.ssl_enable = false;
//                ssl_switch.setChecked(false);
//            } else {
//                config.ssl_enable = true;
//                ssl_switch.setChecked(true);
//            }
//        });
        switch_area.setOnClickListener(v -> {
            if (config.leaf_mode) {
                config.leaf_mode = false;
            } else {
                config.leaf_mode = true;
            }
            updateUI();
        });
        auto_detect.setOnClickListener(view -> {
            config.detectMedia();
            StringBuffer sb = new StringBuffer();
            sb.append(config.inner_storage_path + '\n');
            if (config.has_SD_card) {
                sb.append(config.SD_card_path);
            }
            var uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Android%2Fdata");
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            launcher.launch(intent1);
            Log.i("data", uri.toString());
            AlertDialog d = new AlertDialog.Builder(UserModeActivity.this).setTitle("挂载的目录").setMessage(sb.toString()).setPositiveButton("ok", null).create();
            d.show();
            updateUI();
        });
        custom_area.setOnClickListener(view -> {
            var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            var uri=Uri.parse("content://com.android.externalstorage.documents/document/primary:");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            custom_launcher.launch(intent);
        });
        inner_storage_area.setOnClickListener(view -> {

        });
        sd_card_area.setOnClickListener(view -> {

        });
    }

    private void updateUI() {
        if (config.leaf_mode) {
            auto_detect.setVisibility(View.VISIBLE);
            mode_switch.setChecked(false);
            inner_storage_area.setVisibility(View.VISIBLE);
            sd_card_area.setVisibility(View.VISIBLE);
            custom_area.setVisibility(View.GONE);
            inner_path_textview.setText(config.inner_storage_path);
            sd_path_textview.setText(config.SD_card_path);
            if (!config.has_SD_card) {
                sd_card_area.setVisibility(View.GONE);
            }
        } else {
            auto_detect.setVisibility(View.GONE);
            mode_switch.setChecked(true);
            inner_storage_area.setVisibility(View.GONE);
            sd_card_area.setVisibility(View.GONE);
            custom_area.setVisibility(View.VISIBLE);
            custom_path_textview.setText(config.custom_path);
        }
    }

}