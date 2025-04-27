package cn.leaf.wavingleaf.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ports")
public class Port {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public int id;
    @ColumnInfo(name = "sftp")
    public int sftp;
    @ColumnInfo(name = "ftp")
    public int ftp;
    @ColumnInfo(name = "nfs")
    public int nfs;
    @ColumnInfo(name = "webdav")
    public int webdav;
}
