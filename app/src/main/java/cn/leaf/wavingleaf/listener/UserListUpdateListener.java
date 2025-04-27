package cn.leaf.wavingleaf.listener;

import java.util.List;

import cn.leaf.wavingleaf.model.SFTPUser;

public interface UserListUpdateListener {
    void update(List<SFTPUser> list);
}
