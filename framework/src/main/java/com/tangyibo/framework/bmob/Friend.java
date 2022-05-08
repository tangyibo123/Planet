package com.tangyibo.framework.bmob;
import cn.bmob.v3.BmobObject;

public class Friend {

    //我自己
    private PlanetUser user;
    //好友
    private PlanetUser friendUser;

    public PlanetUser getUser() {
        return user;
    }

    public void setUser(PlanetUser user) {
        this.user = user;
    }

    public PlanetUser getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(PlanetUser friendUser) {
        this.friendUser = friendUser;
    }

}
