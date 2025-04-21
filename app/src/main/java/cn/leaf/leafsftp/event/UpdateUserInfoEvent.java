package cn.leaf.leafsftp.event;


import cn.leaf.leafsftp.model.SFTPUser;

public class UpdateUserInfoEvent {
    public SFTPUser u;
    public UpdateUserInfoEvent(SFTPUser u){
        this.u=u;
    }
}
