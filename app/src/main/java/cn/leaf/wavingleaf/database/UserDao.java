package cn.leaf.wavingleaf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cn.leaf.wavingleaf.model.Port;
import cn.leaf.wavingleaf.model.SFTPUser;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSFTPUser(SFTPUser sshUser);

    @Update
    void updateSFTPUser(SFTPUser sshUser);

    @Delete
    void deleteSFTPUser(SFTPUser sshUser);

    @Query("SELECT pwd FROM sftp_users WHERE user = :user_name")
    String getPwdFromSFTPUser(String user_name);

    @Query("SELECT home FROM sftp_users WHERE user = :user_name")
    String getHomeFromSFTPUser(String user_name);

    @Query("SELECT enable FROM sftp_users WHERE user = :user_name")
    boolean getEnableStatusFromSFTPUser(String user_name);

    @Query("SELECT * FROM sftp_users")
    List<SFTPUser> getAllSFTPUsers();

    @Query("SELECT * FROM ports WHERE id=1")
    Port getAllPorts();

    @Update
    void updatePortConfig(Port p);

    @Query("SELECT sftp FROM ports WHERE id=1")
    int getSFTPPort();

    @Query("SELECT ftp FROM ports WHERE id=1")
    int getFTPPort();

    @Query("SELECT nfs FROM ports WHERE id=1")
    int getNFSPort();

    @Query("SELECT webdav FROM ports WHERE id=1")
    int getWebDavPort();
}
