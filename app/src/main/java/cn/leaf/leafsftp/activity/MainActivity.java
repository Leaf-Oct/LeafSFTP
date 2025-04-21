package cn.leaf.leafsftp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.sshd.common.util.security.SecurityUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.Security;
import java.util.ArrayList;

import cn.leaf.leafsftp.NetworkUtil;
import cn.leaf.leafsftp.R;
import cn.leaf.leafsftp.databinding.ActivityMainBinding;
import cn.leaf.leafsftp.event.SFTPStatusSwitchEvent;
import cn.leaf.leafsftp.fragment.FragmentInfo;
import cn.leaf.leafsftp.service.SFTPServerService;
import cn.leaf.leafsftp.sharedpreferences.Config;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Switch sftp_switch, ftp_switch, nfs_switch, webdav_switch;
    TextView sftp_address_text, ftp_address_text, nfs_address_text, webdav_address_text, port_text;
    View port_area, mode_area, keep_active_area, battery_area;
    CheckBox keep_active;
    Config config;
    ArrayList<String> ips;
    PowerManager.WakeLock wake_lock;
    FloatingActionButton info_btn;

    IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    BroadcastReceiver net_change_receiver=new NetChangeReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        config.saveConfig();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData();
        bindView();
//        initView();
        initAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (config.is_running) {
            stopService(new Intent(MainActivity.this, SFTPServerService.class));
            config.is_running = false;
            unregisterReceiver(net_change_receiver);
        }
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.gc();
        System.exit(0);
    }

    //    加載wake lock实例. 检查配置文件, 检查jks证书
    private void initData() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wake_lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "leaf:ftp server");
        config = Config.getInstance();
        Security.removeProvider("BC");
        System.setProperty("org.apache.sshd.security.provider.BC.enabled", "false");
        System.setProperty("org.apache.sshd.security.registrars", "none");
    }

    private void bindView() {
//        ftp_switch = binding.ftpSwitch;
//        address_text = binding.address;
        sftp_switch=binding.sftpSwitch;
        sftp_address_text=binding.sftpAddress;
        port_area = binding.portArea;
        mode_area = binding.modeArea;
        keep_active_area = binding.keepActiveArea;
        battery_area = binding.batteryArea;
        keep_active = binding.keepActive;
        port_text = binding.port;
        info_btn=binding.info;
    }

    private void initView() {
        port_text.setText(config.port + "");
        keep_active.setChecked(config.keep_alive);
        address_text.setText("未启动");
        updateUI();
    }

    private void initAction() {
        ftp_switch.setOnClickListener(v -> {
            if (!checkPermission()) {
                Toast.makeText(MainActivity.this, "no permission", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!config.is_running) {
                startService(new Intent(MainActivity.this, SFTPServerService.class));
            } else {
                stopService(new Intent(MainActivity.this, SFTPServerService.class));
            }
        });
        port_area.setOnClickListener(v -> {
            if (config.is_running) {
                Log.w("thread", Thread.currentThread().toString());
                Toast.makeText(MainActivity.this, "FTP服务运行中, 请先关闭再修改", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input_port = new EditText(MainActivity.this);
            input_port.setHint("port");
            input_port.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            AlertDialog input_port_dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("设置端口")
                    .setView(input_port)
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        var port = input_port.getText().toString();
                        try {
                            int port_int = Integer.parseInt(port);
                            if (port_int < 1025 || port_int > 65535) {
                                Toast.makeText(MainActivity.this, "端口范围1025~65535", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            config.port = port_int;
                            port_text.setText(port);
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "非法端口, 请输入数字", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        config.saveConfig();
                    }).create();
            input_port_dialog.setOnShowListener(dialogInterface -> {
                input_port.setText(config.port + "");
            });
            input_port_dialog.show();
        });
        keep_active_area.setOnClickListener(view -> {
            if (config.keep_alive) {
                keep_active.setChecked(false);
                config.keep_alive = false;
                if (wake_lock.isHeld()) {
                    wake_lock.release();
                    Log.i("wake lock", "release");
                }
            } else {
                keep_active.setChecked(true);
                config.keep_alive = true;
                wake_lock.acquire();
                Log.i("wake lock", "acquire");
            }
        });
        battery_area.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23) {
                PowerManager power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!power_manager.isIgnoringBatteryOptimizations(getPackageName())) {
                    var i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "已设置", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mode_area.setOnClickListener(view -> {
            if (config.is_running) {
                Toast.makeText(MainActivity.this, "FTP服务运行中, 请先关闭再修改", Toast.LENGTH_SHORT).show();
                return;
            }
            var i = new Intent(MainActivity.this, UserModeActivity.class);
            startActivity(i);
        });
        address_text.setOnClickListener(view -> {
            if (!config.is_running) {
                return;
            }
            StringBuffer sb = new StringBuffer();
            for (var ip : ips) {
                sb.append("sftp://" + ip + ":" + config.port + "\n");
            }
            var dialog = new AlertDialog.Builder(MainActivity.this).setTitle("可用地址").setMessage(sb.toString()).setPositiveButton("ok", null).create();
            dialog.show();
        });
        if (config.keep_alive) {
            wake_lock.acquire();
            Log.i("wake lock", "acquire");
        }
        info_btn.setOnClickListener(v->new FragmentInfo().show(getSupportFragmentManager(), "Leaf SFTP Server"));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void serverStateUpdate(SFTPStatusSwitchEvent e) {
        updateUI();
        if (config.is_running) {
            registerReceiver(net_change_receiver, filter);
        } else {
            unregisterReceiver(net_change_receiver);
        }
    }

    public void updateUI(){
        if (config.is_running) {
            ftp_switch.setBackground(getResources().getDrawable(R.drawable.circle_on, null));
            ftp_switch.setImageDrawable(getResources().getDrawable(R.drawable.power_on, null));
            address_text.setClickable(true);
            ips = NetworkUtil.getAllAddress();
            address_text.setText("sftp://" + ips.get(0) + ":" + config.port);
        } else {
            ftp_switch.setBackground(getResources().getDrawable(R.drawable.circle, null));
            ftp_switch.setImageDrawable(getResources().getDrawable(R.drawable.power, null));
            address_text.setClickable(false);
            address_text.setText("未启动");
        }
    }

    private boolean checkPermission() {
        //        R是Android11
//      读写存储文件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "need inner storage permission", Toast.LENGTH_SHORT).show();
                var i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                i.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(i);
                return false;
            }
        } else {
            int read_storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int write_storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (read_storage != PackageManager.PERMISSION_GRANTED || write_storage != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
                return false;
            }
        }
        return true;
    }

    class NetChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ips = NetworkUtil.getAllAddress();
            address_text.setText("sftp://" + ips.get(0) + ":" + config.port);
        }
    }
}