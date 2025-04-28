package cn.leaf.wavingleaf.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.leaf.wavingleaf.database.UserDao;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.databinding.FragmentEditPortBinding;

public class FragmentEditPort extends DialogFragment {
    FragmentEditPortBinding binding;
    EditText sftp_port, ftp_port, nfs_port, webdav_port;
    Button sftp_default, ftp_default, nfs_default, webdav_default, btn_confirm, btn_cancel;


    UserDao dao = UserDatabaseSingleton.getInstance(getContext()).getUserDao();

    public FragmentEditPort() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentEditPortBinding.inflate(getLayoutInflater());
        initLayout();
        return super.onCreateDialog(savedInstanceState);
    }

    private void initLayout() {
        sftp_port = binding.sftpPort;
        sftp_default = binding.sftpDefault;
        ftp_port = binding.ftpPort;
        ftp_default = binding.ftpDefault;
        nfs_port = binding.nfsPort;
        nfs_default = binding.nfsDefault;
        webdav_port = binding.webdavPort;
        webdav_default = binding.webdavDefault;
        btn_confirm = binding.editPortConfirm;
        btn_cancel = binding.editPortCancel;

        var p = dao.getAllPorts();
        sftp_port.setText(String.valueOf(p.sftp));
        ftp_port.setText(String.valueOf(p.ftp));
        nfs_port.setText(String.valueOf(p.nfs));
        webdav_port.setText(String.valueOf(p.webdav));

        sftp_default.setOnClickListener(i -> {
            sftp_port.setText("2222");
        });
        ftp_default.setOnClickListener(i -> {
            ftp_port.setText("2121");
        });
        nfs_default.setOnClickListener(i -> {
            nfs_port.setText("2049");
        });
        webdav_default.setOnClickListener(i -> {
            webdav_port.setText("8080");
        });
        btn_cancel.setOnClickListener(i -> {
            dismiss();
        });
        btn_confirm.setOnClickListener(i -> {
            String sftp_port_string = sftp_port.getText().toString(),
                    ftp_port_string = ftp_port.getText().toString(),
                    nfs_port_string = nfs_port.getText().toString(),
                    webdav_port_string = webdav_port.getText().toString();
            try {
                int sftp_port_number = Integer.parseInt(sftp_port_string),
                        ftp_port_number = Integer.parseInt(ftp_port_string),
                        nfs_port_number = Integer.parseInt(nfs_port_string),
                        webdav_port_number = Integer.parseInt(webdav_port_string);
                if (sftp_port_number > 1024 && sftp_port_number < 65535 &&
                        ftp_port_number > 1024 && ftp_port_number < 65535 &&
                        nfs_port_number > 1024 && nfs_port_number < 65535 &&
                        webdav_port_number > 1024 && webdav_port_number < 65535) {
                    p.sftp = sftp_port_number;
                    p.ftp = ftp_port_number;
                    p.nfs = nfs_port_number;
                    p.webdav = webdav_port_number;
                    new Thread(() -> {
                        dao.updatePortConfig(p);
                    }).start();
                    Toast.makeText(getContext(), "更新成功，需要重启服务才能生效", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
                Toast.makeText(getContext(), "端口范围1025~65535", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "非法端口, 请输入数字", Toast.LENGTH_LONG).show();

            }

        });
    }
}
