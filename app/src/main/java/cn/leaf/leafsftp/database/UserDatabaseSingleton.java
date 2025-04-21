package cn.leaf.leafsftp.database;

import android.content.Context;

import androidx.room.Room;

public class UserDatabaseSingleton {
    private static volatile UserDatabaseSingleton instance;
    private static final String DATABASE_NAME = "user_db";
    private UserDatabase user_database;
    private UserDao dao;

    private UserDatabaseSingleton(Context context) {
        user_database = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME).build();
        dao = user_database.sshUserDao();
    }

    public UserDao getUserDao() {
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
