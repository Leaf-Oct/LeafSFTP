package cn.leaf.wavingleaf.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.databinding.ActivityFtpUsersBinding;

public class FtpUsersActivity extends AppCompatActivity {

    ActivityFtpUsersBinding binding;
    RecyclerView user_list;
    FloatingActionButton add_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        initData();
        initAction();

    }

    private void bindView(){

    }

    private void initData(){

    }

    private void initAction(){

    }
}