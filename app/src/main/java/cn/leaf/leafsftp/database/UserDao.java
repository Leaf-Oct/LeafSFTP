package cn.leaf.leafsftp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cn.leaf.leafsftp.model.Password;
import cn.leaf.leafsftp.model.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User sshUser);

    @Update
    void update(User sshUser);

    @Delete
    void delete(User sshUser);

    @Query("SELECT pwd FROM ssh_users WHERE user = :user_name")
    String getPwdFromUser(String user_name);

    @Query("SELECT home FROM ssh_users WHERE user = :user_name")
    String getHomeFromUser(String user_name);

    @Query("SELECT enable FROM ssh_users WHERE user = :user_name")
    boolean getEnableStatusFromUser(String user_name);

    @Query("SELECT * FROM ssh_users")
    List<User> getAllSSHUsers();
}
