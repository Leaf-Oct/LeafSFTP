package cn.leaf.leafsftp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cn.leaf.leafsftp.model.FTPUser;
import cn.leaf.leafsftp.model.Port;
import cn.leaf.leafsftp.model.SFTPUser;

@Database(entities = {SFTPUser.class, FTPUser.class, Port.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao sshUserDao();
}
