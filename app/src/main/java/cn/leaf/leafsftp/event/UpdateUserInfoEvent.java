package cn.leaf.leafsftp.event;


import cn.leaf.leafsftp.model.User;

public class UpdateUserInfoEvent {
    public User u;
    public UpdateUserInfoEvent(User u){
        this.u=u;
    }
}
