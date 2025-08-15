package cn.leaf.wavingleaf.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.databinding.FragmentEditFtpUserBinding;
import cn.leaf.wavingleaf.listener.FTPUserListUpdateListener;
import cn.leaf.wavingleaf.model.FTPUser;
import cn.leaf.wavingleaf.sharedpreferences.Config;

public class FragmentEditFTPUser extends DialogFragment {

    boolean is_new;
    FTPUser user;
    EditText input_label, input_user, input_password, input_home;
    Button btn_browse, btn_in, btn_sd, btn_delete, btn_cancel, btn_apply;
    SwitchCompat switch_writable;
    FragmentEditFtpUserBinding binding;
    FTPUserListUpdateListener listener;

    Config config=Config.getInstance();

    UserDao dao= UserDatabaseSingleton.getInstance(getContext()).getUserDao();




    public FragmentEditFTPUser(boolean is_new, FTPUser user, FTPUserListUpdateListener l) {
        // Required empty public constructor
        this.is_new=is_new;
        this.user=user;
        listener=l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding=FragmentEditFtpUserBinding.inflate(getLayoutInflater());
        initLayout();
        return new AlertDialog.Builder(getActivity()).setTitle(is_new?"New User":"Edit").setView(binding.getRoot()).create();
    }

    private void initLayout(){
        input_label=binding.inputLabel;
        input_user=binding.inputUsername;
        input_password=binding.inputPassword;
        input_home=binding.inputHome;
        btn_browse=binding.btnBrowse;
        btn_in=binding.btnInternal;
        btn_sd=binding.btnSD;
        btn_delete=binding.btnDelete;
        btn_cancel=binding.btnCancel;
        btn_apply=binding.btnApply;
        switch_writable=binding.writable;
        if (!is_new){
            btn_delete.setVisibility(View.VISIBLE);
            input_label.setText(user.label);
            input_user.setText(user.user);
            input_user.setEnabled(false);
            input_password.setText(user.password);
            input_home.setText(user.home);
            switch_writable.setChecked(user.writable);
        }
        if(config.has_SD_card){
            btn_sd.setVisibility(View.VISIBLE);
            btn_sd.setOnClickListener(view -> {
                input_home.setText(config.SD_card_path);
            });
        }
        btn_browse.setOnClickListener(v->{
            var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            var uri= Uri.parse("content://com.android.externalstorage.documents/document/primary:");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            custom_launcher.launch(intent);
        });
        btn_in.setOnClickListener(view -> {
            input_home.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        });
        btn_delete.setOnClickListener(v->{
            delete();
            dismiss();
        });
        btn_cancel.setOnClickListener(v->{
            dismiss();
        });
        btn_apply.setOnClickListener(v->{
            apply();
        });
    }

    private void delete(){
        new Thread(()->{
            dao.deleteFTPUser(user);
            var list=dao.getAllFTPUsers();
            getActivity().runOnUiThread(()-> listener.update(list));
        }).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void apply(){
        var label=input_label.getText().toString();
        var username=input_user.getText().toString();
        var pwd=input_password.getText().toString();
        var home=input_home.getText().toString();
        var writable=switch_writable.isChecked();
        if(label.isBlank()||username.isBlank()||pwd.isBlank()||home.isBlank()){
            Toast.makeText(getActivity(), "不能留空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (is_new){
            new Thread(()->{
                var saved_pwd=dao.getPwdFromFTPUser(username);
                if (saved_pwd!=null){
                    getActivity().runOnUiThread(()-> Toast.makeText(getContext(), "用户名已存在", Toast.LENGTH_SHORT).show());
                    return;
                }
                user=new FTPUser(username, pwd, home, label, true, writable);
                dao.insertFTPUser(user);
                var list=dao.getAllFTPUsers();
                getActivity().runOnUiThread(()->{
                    listener.update(list);
                    dismiss();
                });
            }).start();
            return;
        }
        user.label=label;
        user.password=pwd;
        user.home=home;
        user.writable=writable;
        new Thread(()->{
            dao.updateFTPUser(user);
            var list=dao.getAllFTPUsers();
            getActivity().runOnUiThread(()-> listener.update(list));
        }).start();
        dismiss();
    }

    ActivityResultLauncher<Intent> custom_launcher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
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
            uri_path=uri_path.replace("/tree/primary:", Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator);
        }
        else if(uri_path.startsWith("/tree")&&config.has_SD_card){
            uri_path=config.SD_card_path+ File.separator+uri_path.substring(uri_path.indexOf(":")+1);
        }
        else {
            uri_path="dir path error!!!";
        }
        input_home.setText(uri_path);
    });
}