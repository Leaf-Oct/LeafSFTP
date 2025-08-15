package cn.leaf.wavingleaf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.database.UserDatabaseSingleton;
import cn.leaf.wavingleaf.event.UpdateFTPUserInfoEvent;
import cn.leaf.wavingleaf.listener.FTPUserListUpdateListener;
import cn.leaf.wavingleaf.model.FTPUser;

public class FTPUserListAdapter extends RecyclerView.Adapter<FTPUserListAdapter.ViewHolder> implements FTPUserListUpdateListener {
    public List<FTPUser> list;
    public Context context;

    public FTPUserListAdapter(Context context, List<FTPUser> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_ftp_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void update(List<FTPUser> l) {
        list=l;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView label, username, password, home;
        SwitchCompat enable, writable;
        Button edit;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label=itemView.findViewById(R.id.label);
            enable=itemView.findViewById(R.id.enable);
            writable=itemView.findViewById(R.id.writable);
            username=itemView.findViewById(R.id.txtusername);
            password=itemView.findViewById(R.id.txtpassword);
            home=itemView.findViewById(R.id.txtpath);
            edit=itemView.findViewById(R.id.edit);
        }
        public void bind(FTPUser u){
            label.setText(u.label);
            username.setText(u.user);
            password.setText(u.password);
            home.setText(u.home);
            enable.setChecked(u.enable);
            writable.setChecked(u.writable);
            enable.setOnCheckedChangeListener((compoundButton, b)->{
                u.enable=b;
                new Thread(()-> UserDatabaseSingleton.getInstance(null).getUserDao().updateFTPUser(u)).start();
            });
            edit.setOnClickListener(v-> EventBus.getDefault().post(new UpdateFTPUserInfoEvent(u)));
            writable.setOnCheckedChangeListener((c, b)->{
                u.writable=b;
                new Thread(()-> UserDatabaseSingleton.getInstance(null).getUserDao().updateFTPUser(u)).start();
            });
        }
    }
}
