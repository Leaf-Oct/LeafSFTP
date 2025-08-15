package cn.leaf.wavingleaf.listener;

import java.util.List;

import cn.leaf.wavingleaf.model.FTPUser;

public interface FTPUserListUpdateListener {
    void update(List<FTPUser> l);
}
