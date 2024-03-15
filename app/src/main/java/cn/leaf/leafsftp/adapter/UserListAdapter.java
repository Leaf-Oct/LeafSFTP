package cn.leaf.leafsftp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.leaf.leafsftp.R;
import cn.leaf.leafsftp.database.UserDao;
import cn.leaf.leafsftp.database.UserDatabaseSingleton;
import cn.leaf.leafsftp.event.UpdateUserInfoEvent;
import cn.leaf.leafsftp.listener.UserListUpdateListener;
import cn.leaf.leafsftp.model.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> implements UserListUpdateListener {
    public List<User> list;
    public Context context;
    public UserListAdapter(Context context, List<User> list){
        this.context=context;
        this.list=list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var view= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        var data=list.get(position);
        holder.bind(data);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void update(List<User> list) {
        this.list=list;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        static FragmentManager fm;
        TextView label, username, password, home;
        SwitchCompat enable;
        Button edit;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label=itemView.findViewById(R.id.label);
            username=itemView.findViewById(R.id.txtusername);
            password=itemView.findViewById(R.id.txtpassword);
            home=itemView.findViewById(R.id.txtpath);
            enable=itemView.findViewById(R.id.enable);
            edit=itemView.findViewById(R.id.edit);
        }

        public void bind(User user) {
            label.setText(user.label);
            username.setText(user.user);
            password.setText(user.password);
            home.setText(user.home);
            enable.setChecked(user.enable);
            enable.setOnCheckedChangeListener((compoundButton, b) -> {
                user.enable=b;
                new Thread(()->{
                    UserDatabaseSingleton.getInstance(null).getSSH_UserDao().update(user);
                }).start();

            });
            edit.setOnClickListener(v->{
                EventBus.getDefault().post(new UpdateUserInfoEvent(user));
            });
        }

    }


}
