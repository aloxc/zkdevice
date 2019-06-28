package oa;

import java.io.Serializable;

/**
 * @author liyonghua@vv.cn
 * @version 1.0
 * @date 2019/6/28 18:48
 */
public class User implements Serializable {
    private static final long serialVersionUID = -2725033550959122066L;
    private String userId;
    private String name;
    //    该用户是否启用中，
    private boolean enabled;
    private String password;
    private Privilege privilege;

    public User(String userId, String name, Boolean enabled, String password, Privilege privilege) {
        this.userId = userId;
        this.name = name;
        this.enabled = enabled;
        this.password = password;
        this.privilege = privilege;
    }

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", password='" + password + '\'' +
                ", privilege=" + privilege +
                '}';
    }
}
