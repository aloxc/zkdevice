package cn.vv.oa;

import java.util.HashMap;
import java.util.Map;

/**
 * 考勤机权限控制枚举
 */
public enum Privilege {
    //    0为普通用户，1，2,4为自定义用户角色，3超级管理员。
    //1 自定义权限无法使用
    General(0,"普通用户"),
    Custom2 (2,"自定义权限2"),
    Custom4(4,"自定义权限3"),
    Administrator(3,"超级管理员");

    private int flag;
    private String description;

    public int getFlag() {
        return flag;
    }

    public String getDescription() {
        return description;
    }

    Privilege(int flag, String description){
        this.flag = flag;
        this.description = description;
    }

    private static Map<Integer,Privilege> privilegeMap = new HashMap<>();
    static{
        for(Privilege p : Privilege.values()){
            privilegeMap.put(p.flag,p);
        }
    }
    public static Privilege getPrivilegeByFlag(int flag){
        return privilegeMap.get(flag);
    }

    @Override
    public String toString() {
        return "Privilege{" +
                "flag=" + flag +
                ", description='" + description + '\'' +
                '}';
    }
}
