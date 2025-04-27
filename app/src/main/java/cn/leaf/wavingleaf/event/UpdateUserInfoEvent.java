package cn.leaf.wavingleaf.event;


import cn.leaf.wavingleaf.model.SFTPUser;

public class UpdateUserInfoEvent {
    public SFTPUser u;
    public UpdateUserInfoEvent(SFTPUser u){
        this.u=u;
    }
}
