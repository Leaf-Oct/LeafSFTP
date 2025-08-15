package cn.leaf.wavingleaf.event;

import cn.leaf.wavingleaf.model.FTPUser;

public class UpdateFTPUserInfoEvent {
    public FTPUser user;

    public UpdateFTPUserInfoEvent(FTPUser user) {
        this.user = user;
    }
}
