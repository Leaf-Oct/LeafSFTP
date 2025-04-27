package cn.leaf.wavingleaf.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cn.leaf.wavingleaf.model.FTPUser;
import cn.leaf.wavingleaf.model.Port;
import cn.leaf.wavingleaf.model.SFTPUser;

@Database(entities = {SFTPUser.class, FTPUser.class, Port.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao sshUserDao();
}
