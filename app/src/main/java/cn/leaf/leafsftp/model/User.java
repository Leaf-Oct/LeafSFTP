package cn.leaf.leafsftp.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ssh_users")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user")
    public String user;
    @ColumnInfo(name = "pwd")
    public String password;
    @ColumnInfo(name = "home")
    public String home;
    @ColumnInfo(name = "label")
    public String label;
    @ColumnInfo(name = "enable")
    public boolean enable;

    public User(String user, String password, String home, String label, boolean enable) {
        this.user = user;
        this.password = password;
        this.home = home;
        this.label = label;
        this.enable = enable;
    }

}
