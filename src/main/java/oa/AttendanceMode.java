package oa;

import java.util.HashMap;
import java.util.Map;

/**
 * 打卡方式
 * @author liyonghua@vv.cn
 * @version 1.0
 * @date 2019/6/8 9:06
 */
public enum AttendanceMode {
    PASSWORD(0,"密码"),
    FINGERPRINT(1,"指纹"),
    FACE(15,"面部识别");
    private int mode;
    private String description;

    AttendanceMode(int mode, String description) {
        this.mode = mode;
        this.description = description;
    }

    private static Map<Integer,AttendanceMode> modeMap = new HashMap<>();
    static{
        for(AttendanceMode m : AttendanceMode.values()){
            modeMap.put(m.mode,m);
        }
    }
    public static AttendanceMode getAttendanceMode(int mode){
        return modeMap.get(mode);
    }

}
