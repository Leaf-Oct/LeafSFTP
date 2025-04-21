package cn.leaf.leafsftp.listener;

import java.util.List;

import cn.leaf.leafsftp.model.SFTPUser;

public interface UserListUpdateListener {
    void update(List<SFTPUser> list);
}
