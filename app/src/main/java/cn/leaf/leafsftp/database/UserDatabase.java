package cn.leaf.leafsftp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cn.leaf.leafsftp.model.User;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao sshUserDao();
}
