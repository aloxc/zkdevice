package oa;

import java.util.Date;

/**
 * @author liyonghua@vv.cn
 * @version 1.0
 * @date 2019/6/28 18:54
 */
public class Attendance {
    /**
     * 考勤时间
     */
    private Date createDate;
    /**
     * 考勤方式
     */
    private AttendanceMode attendanceMode;
    private String userNo;
    private String deviceSN;

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public AttendanceMode getAttendanceMode() {
        return attendanceMode;
    }

    public void setAttendanceMode(AttendanceMode attendanceMode) {
        this.attendanceMode = attendanceMode;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public Attendance(){};

    @Override
    public String toString() {
        return "Attendance{" +
                "createDate=" + createDate +
                ", attendanceMode=" + attendanceMode +
                ", userNo='" + userNo + '\'' +
                '}';
    }
}
