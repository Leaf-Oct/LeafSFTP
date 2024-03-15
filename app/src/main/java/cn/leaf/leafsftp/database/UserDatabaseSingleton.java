package cn.leaf.leafsftp.database;

import android.content.Context;

import androidx.room.Room;

public class UserDatabaseSingleton {
    private static volatile UserDatabaseSingleton instance;
    private static final String DATABASE_NAME = "ssh_user_db";
    private UserDatabase sshUserDatabase;
    private UserDao dao;

    private UserDatabaseSingleton(Context context) {
        sshUserDatabase = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME).build();
        dao = sshUserDatabase.sshUserDao();
    }

    public UserDao getSSH_UserDao() {
        return dao;
    }

    public static UserDatabaseSingleton getInstance(Context context) {
        if (instance == null) {
            synchronized (UserDatabaseSingleton.class) {
                instance = new UserDatabaseSingleton(context);
            }
        }
        return instance;
    }
}
