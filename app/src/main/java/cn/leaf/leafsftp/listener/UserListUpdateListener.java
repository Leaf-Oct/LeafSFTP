package cn.leaf.leafsftp.listener;

import java.util.List;

import cn.leaf.leafsftp.model.User;

public interface UserListUpdateListener {
    void update(List<User> list);
}
