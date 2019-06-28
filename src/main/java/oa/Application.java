package oa;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author liyonghua@vv.cn
 * @version 1.0
 * @date 2019/6/28 18:32
 */
public class Application {
    private static ActiveXComponent zkem;
    public static void main(String[] args) throws Exception {
        int flag = 0;
        if(args.length == 0){
            System.out.println("please input an operation flag[number]:");
            System.out.println("\t\t\t1: send user list to attendance device");
            System.out.println("\t\t\t2: backup all data");
            System.out.println("\t\t\t3: recover user and fingerprint list");
            InputStream in = System.in;
            flag = in.read();
        }else{
            try{
                flag = Integer.parseInt(args[0]);
            }catch (Exception e){
                System.out.println("argument should be a number.");
                System.exit(1);
            }
        }
        if(flag<1 || flag >3){
            System.out.println("please rerun");
            System.exit(1);
        }
        if(flag ==1 ){
            initUser();
        }else if(flag == 2){
            backupData();
        }else if(flag == 3){
            recoverData();
        }

        zkem = new ActiveXComponent("zkemkeeper.zkem.1");
        zkem.invoke("Connect_NET", "172.16.12.10", 80).getBoolean();

    }

    private static void initUser() throws Exception{
        String path = "d:\\vvoa\\singaporevvuserlist.xlsx";
        FileInputStream fis =null;
        Workbook wookbook = null;
        fis = new FileInputStream(path);
        wookbook = new XSSFWorkbook(fis);//得到工作簿
        //得到一个工作表
        Sheet sheet = wookbook.getSheet("list");
        int rowStart  = 1;
        int rowEnd = 29;
        for (int rIndex = rowStart; rIndex < rowEnd ; rIndex++) {
            Row row = sheet.getRow(rIndex);
            String name = row.getCell(2).getStringCellValue();
            String no = row.getCell(4).getStringCellValue();
            setUserInfo(no,name,"",Privilege.General.getFlag(),true);
            System.out.println(no + "\t" + name);
        }
        clearAdministrators();
    }




    public static boolean powerOffDevice() {
        Variant v0 = new Variant(1);
        boolean result = zkem.invoke("PowerOffDevice", v0).getBoolean();
        return result;
    }

    /**
     * 读取最后的错误信息
     * @return
     */
    public static int getLastError() {
        Variant v0 = new Variant((int)1,true);
        zkem.invoke("GetLastError", v0);
        return v0.getIntRef();
    }

    /**
     * 重启设备
     * @return
     */
    public static boolean restartDevice() {
        Variant v0 = new Variant(1);
        boolean result = zkem.invoke("RestartDevice", v0).getBoolean();
        return result;
    }
    /**
     * 读取机器是否支持门禁功能，为0是代表没有门禁功能， 1为简单门禁，2为中级门禁，6为高级门禁，14为高级门禁+常开功能,15为高级门禁
     * @return
     */
    public static boolean getACFun() {
        Variant lock = new Variant(0,true);
        boolean result = zkem.invoke("GetACFun", lock).getBoolean();
        System.out.println("是否支持门禁功能"+lock.getIntRef());
        return result;
    }




    /**
     * 从备份文件中恢复数据
     * @throws IOException
     */
    private static void recoverData() throws IOException {
        File file = new File("c:\\zkdata.txt");
        if(!file.exists()){
            System.out.println("备份文件不存在");
            return;
        }
        List<String> list = FileUtils.readLines(file, Charset.forName("utf-8"));

        for (String line : list){
            String json = line.split("====")[1];
            if(line.startsWith("userList")){
                List<User> userList = JsonUtil.toBeanList(json, User.class);
                for(User user : userList){
                    setUserInfo(user.getUserId(),user.getName(),user.getPassword(),user.getPrivilege().getFlag(),user.isEnabled());
                }
            }else if(line.startsWith("fingerprint___")){
                List<Fingerprint> fingerprintList = JsonUtil.toBeanList(json,Fingerprint.class);
                String userId = line.split("====")[0].replace("fingerprint___","");
                for(Fingerprint fingerprint : fingerprintList){
                    setFingerprint(userId,fingerprint);
                }
            }else if(line.startsWith("face___")){
                String userId = line.split("====")[0].replace("face___","");
                setUserFace(userId,json);
            }
        }
        System.out.println("恢复完毕");
    }

    private static void backupData() throws IOException {
        File file = new File("c:\\zkdata.txt");
        System.out.println("备份文件路径"+ file.getAbsolutePath());
        if(file.exists()){
            file.delete();
        }
        file.createNewFile();
        String key = "userList";
        List<User> userList = getUserList();
        FileUtils.writeStringToFile(file,key + "====" + JsonUtil.toJson(userList),Charset.forName("utf-8"),true);
        FileUtils.writeStringToFile(file,"\n",Charset.forName("utf-8"),true);
        for (User user : userList){
            List<Fingerprint> fingerprintList = getFingerprintList(user.getUserId());
            if(fingerprintList != null) {
                key = "fingerprint___" + user.getUserId();
                FileUtils.writeStringToFile(file, key + "====" + JsonUtil.toJson(fingerprintList), Charset.forName("utf-8"),true);
                FileUtils.writeStringToFile(file, "\n", Charset.forName("utf-8"),true);
            }
            String userFace = getUserFace(user.getUserId());
            if(userFace != null) {
                key = "face___" + user.getUserId();
                FileUtils.writeStringToFile(file, key + "====" + userFace, Charset.forName("utf-8"),true);
                FileUtils.writeStringToFile(file, "\n", Charset.forName("utf-8"),true);
            }
        }
    }



    /**
     * 读取考勤记录到pc缓存。配合getGeneralLogData使用
     *
     * @return
     */
    public static boolean readGeneralLogData() {
        boolean result = zkem.invoke("ReadGeneralLogData", 4).getBoolean();
        return result;
    }

    /**
     * 开锁
     * @return
     */
    public static boolean acUnlock() {
        Variant devNum = new Variant(1);
        Variant delay = new Variant(10);
        boolean result = zkem.invoke("ACUnlock",devNum,delay).getBoolean();
        return result;
    }

    /**
     * 读取序列号
     * @return
     */
    public static String getSerialNumber() {
        Variant devNum = new Variant(1);
        Variant sSerialNumber = new Variant("",true);
        boolean result = zkem.invoke("GetSerialNumber", devNum,sSerialNumber).getBoolean();
        String serialNumber = null;
        if(result) {
            serialNumber = sSerialNumber.getStringRef();
        }
        return serialNumber;
    }
    /**
     * 删除时间段内的打卡记录
     *
     * @param start
     * @param end
     * @return
     */
    public static boolean deleteAttlogBetweenTheDate(Date start, Date end) {
        Variant dwMachineNumber = new Variant(1, true);//机器号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Variant bStart = new Variant(sdf.format(start), true);
        Variant bEnd = new Variant(sdf.format(end), true);
        boolean result = zkem.invoke("DeleteAttlogBetweenTheDate", dwMachineNumber, bStart, bEnd).getBoolean();
        return result;
    }

    /**
     * 按给定的时间点（精确到秒），删除这个时间点前的所有老的考勤记录
     *
     * @param end
     * @return
     */
    public static boolean deleteAttlogByTime(Date end) {
        Variant dwMachineNumber = new Variant(1, true);//机器号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Variant bEnd = new Variant(sdf.format(end), true);
        boolean result = zkem.invoke("DeleteAttlogByTime", dwMachineNumber, bEnd).getBoolean();
        return result;
    }

    /**
     * 读取该时间之后的最新考勤数据。 配合getGeneralLogData使用。//网上说有这个方法，但是我用的开发文档没有这个方法，也调用不到，我在controller中处理获取当天数据
     *
     * @param lastest
     * @return
     */
    public static boolean readLastestLogData(Date lastest) {

        boolean result = zkem.invoke("ReadLastestLogData", 2018 - 07 - 24).getBoolean();
        return result;
    }

    /**
     * 获取缓存中的考勤数据。配合readGeneralLogData / readLastestLogData使用。
     *
     * @return 返回的map中，包含以下键值：
     * "EnrollNumber"   人员编号
     * "Time"           考勤时间串，格式: yyyy-MM-dd HH:mm:ss
     * "VerifyMode"     验证方式  1：指纹 2：面部识别
     * "InOutMode"      考勤状态 0：上班 1：下班 2：外出 3：外出返回 4：加班签到 5：加班签退
     * "Year"          考勤时间：年
     * "Month"         考勤时间：月
     * "Day"           考勤时间：日
     * "Hour"            考勤时间：时
     * "Minute"        考勤时间：分
     * "Second"        考勤时间：秒
     */
    public static List<Attendance> getGeneralLogData() {
        Variant dwMachineNumber = new Variant(1, true);//机器号

        Variant dwUserId = new Variant("", true);
        Variant dwVerifyMode = new Variant(0, true);
        Variant dwInOutMode = new Variant(0, true);
        Variant dwYear = new Variant(0, true);
        Variant dwMonth = new Variant(0, true);
        Variant dwDay = new Variant(0, true);
        Variant dwHour = new Variant(0, true);
        Variant dwMinute = new Variant(0, true);
        Variant dwSecond = new Variant(0, true);
        Variant dwWorkCode = new Variant(0, true);
        List<Attendance> attendanceList = new ArrayList<>();
        boolean newresult = false;
        do {
            Variant vResult = Dispatch.call(zkem, "SSR_GetGeneralLogData", dwMachineNumber, dwUserId, dwVerifyMode, dwInOutMode, dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond, dwWorkCode);
            newresult = vResult.getBoolean();
            if (newresult) {
                String userId = dwUserId.getStringRef();

                //如果没有编号，则跳过。
                if (StringUtils.isEmpty(userId)) continue;
                Attendance attendance = new Attendance();
                attendance.setUserNo(userId);
                attendance.setAttendanceMode(AttendanceMode.getAttendanceMode(dwVerifyMode.getIntRef()));
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, dwYear.getIntRef());
                calendar.set(Calendar.MONTH, dwMonth.getIntRef());
                calendar.set(Calendar.DATE, dwDay.getIntRef());
                calendar.set(Calendar.HOUR_OF_DAY, dwHour.getIntRef());
                calendar.set(Calendar.MINUTE, dwMinute.getIntRef());
                calendar.set(Calendar.SECOND, dwSecond.getIntRef());
                attendance.setCreateDate(calendar.getTime());
                attendanceList.add(attendance);

            }
        } while (newresult == true);
        return attendanceList;
    }


    /**
     * 读取考勤机时间
     * @return
     */
    public static Date getDeviceTime() {
        Variant dwMachineNumber = new Variant(1, true);//机器号
        Variant dwYear = new Variant(0, true);
        Variant dwMonth = new Variant(0, true);
        Variant dwDay = new Variant(0, true);
        Variant dwHour = new Variant(0, true);
        Variant dwMinute = new Variant(0, true);
        Variant dwSecond = new Variant(0, true);
        Date date = null;
        boolean newresult = false;
        Variant vResult = Dispatch.call(zkem, "GetDeviceTime", dwMachineNumber, dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond);
        newresult = vResult.getBoolean();
        if (newresult) {
            date = new Date(dwYear.getIntRef() - 1900, dwMonth.getIntRef() -1, dwDay.getIntRef(), dwHour.getIntRef(), dwMinute.getIntRef(), dwSecond.getIntRef());
        }
        return date;
    }

    /**
     * 读取考勤机时间
     * @return
     */
    public static boolean setDeviceTime(int year,int month,int day,int hour,int minute,int second) {
        Variant dwMachineNumber = new Variant(1, true);//机器号
        Variant dwYear = new Variant(year, true);
        Variant dwMonth = new Variant(month, true);
        Variant dwDay = new Variant(day, true);
        Variant dwHour = new Variant(hour, true);
        Variant dwMinute = new Variant(minute, true);
        Variant dwSecond = new Variant(second, true);
        boolean newresult = false;
        Variant vResult = Dispatch.call(zkem, "SetDeviceTime2", dwMachineNumber, dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond);
        newresult = vResult.getBoolean();
        return newresult;
    }
    /**
     * 获取用户信息
     *
     * @return 返回的Map中，包含以下键值:
     * "EnrollNumber"  人员编号
     * "Name"          人员姓名
     * "Password"      人员密码
     * "Privilege"     特权 0位普通 3特权
     * "Enabled"       是否启用
     */
    public static List<User> getUserList() {
        List<User> userList = new LinkedList<>();
        //将用户数据读入缓存中。
        boolean result = zkem.invoke("ReadAllUserID", 1).getBoolean();

        Variant v0 = new Variant(1);
        Variant sdwEnrollNumber = new Variant("", true);
        Variant sName = new Variant("", true);
        Variant sPassword = new Variant("", true);
        Variant iPrivilege = new Variant(0, true);
        Variant bEnabled = new Variant(false, true);

        while (result) {
            //从缓存中读取一条条的用户数据
//            result = zkem.invoke("SSR_GetAllUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
            result = zkem.invoke("SSR_GetAllUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();

            //如果没有编号，跳过。
            String enrollNumber = sdwEnrollNumber.getStringRef();
            if (enrollNumber == null || enrollNumber.trim().length() == 0)
                continue;

            //由于名字后面会产生乱码，所以这里采用了截取字符串的办法把后面的乱码去掉了，以后有待考察更好的办法。
            //只支持2位、3位、4位长度的中文名字。
            String name = sName.getStringRef();
            int index = name.indexOf("\0");
            if (index > -1) {
                name = name.substring(0, index);
            }
            //如果没有名字，跳过。
            if (name.trim().length() == 0)
                continue;
            User user = new User();
            user.setUserId(enrollNumber);
            user.setName(name);
            user.setPassword(sPassword.getStringRef());
            user.setPrivilege(Privilege.getPrivilegeByFlag(iPrivilege.getIntRef()));
            user.setEnabled((Boolean) bEnabled.getBooleanRef());
            userList.add(user);
        }
        return userList;
    }


    /**
     * 刷新机器内数据，一般在上传用户信息或指纹后调用，这样能使所作的修改立即起作用，起到同步作用
     * @return
     */
    public static boolean refreshData() {
        Variant v0 = new Variant(1);
        boolean result = zkem.invoke("RefreshData", v0).getBoolean();
        return result;
    }

    /**
     * 清除机器里面所有管理人员权限
     * @return
     */
    public static boolean clearAdministrators() {
        Variant v0 = new Variant(1);
        boolean result = zkem.invoke("ClearAdministrators", v0).getBoolean();
        return result;
    }
    /**
     * 设置用户信息
     *
     * @param number
     * @param name
     * @param password
     * @param isPrivilege 0為普通用戶,3為管理員;
     * @param enabled     是否啟用
     * @return
     */
    public static boolean setUserInfo(String number, String name, String password, int isPrivilege, boolean enabled) {
        Variant v0 = new Variant(1);
        Variant sdwEnrollNumber = new Variant(number, true);
        Variant sName = new Variant(name,false);
        Variant sPassword = new Variant(password, true);
        Variant iPrivilege = new Variant(isPrivilege, true);
        Variant bEnabled = new Variant(enabled, true);

        boolean result = zkem.invoke("SSR_SetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
        return result;
    }

    /**
     * 设置用户状态,该方法会导致考勤机启用管理功能，但是没有管理员，
     *
     * @param number
     * @param enabled
     * @return
     */
    public static boolean setUserStatus(int number, boolean enabled) {
        Variant v0 = new Variant(1);
        Variant sdwEnrollNumber = new Variant(number, true);
        Variant bEnabled = new Variant(enabled, enabled);

        boolean result = zkem.invoke("SSR_EnableUser", v0, sdwEnrollNumber, bEnabled).getBoolean();
        System.out.println("设置用户状态结果" + result);
        return result;
    }

    /**
     * 获取用户信息
     *
     * @param userId 考勤号码
     * @return
     */
    public static User getUserInfoByNumber(String userId) {
        Variant v0 = new Variant(1);
        Variant sdwEnrollNumber = new Variant(userId, true);
        Variant sName = new Variant("", true);
        Variant sPassword = new Variant("", true);
        Variant iPrivilege = new Variant(0, true);
        Variant bEnabled = new Variant(false, true);
        boolean result = zkem.invoke("SSR_GetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
        User user = null;
        if (result) {
            user = new User();
            user.setUserId(userId);
            String name = sName.getStringRef();
            int index = name.indexOf("\0");
            if (index > -1) {
                name = name.substring(0, index);
            }
            user.setName(name);
            user.setPassword(sPassword.getStringRef());
            user.setPrivilege(Privilege.getPrivilegeByFlag(iPrivilege.getIntRef()));
            user.setEnabled(bEnabled.getBooleanRef());
        }
        return user;
    }

    /**
     * 读取指纹列表
     *
     * @param userId
     * @return
     */
    public static List<Fingerprint> getFingerprintList(String userId) {
        Variant v0 = new Variant(1L);
        Variant sdwEnrollNumber = new Variant(userId, true);
        List<Fingerprint> fingerprintList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Variant indexV = new Variant((long) i, true);
            Variant flagV = new Variant(i, true);
            Variant tempV = new Variant("", true);
            Variant lenV = new Variant(0, true);
            boolean result = zkem.invoke("GetUserTmpExStr", v0, sdwEnrollNumber, indexV, flagV, tempV, lenV).getBoolean();
            if (result) {
                fingerprintList.add(new Fingerprint(i, tempV.getStringRef(),flagV.getIntRef()));
            }
        }
        return fingerprintList;
    }

    /**
     *
     * @param fingerprint
     * @return
     */
    public static boolean setFingerprint(String userId,Fingerprint fingerprint) {
        Variant v0 = new Variant(1L);
        Variant sdwEnrollNumber = new Variant(userId, true);
        Variant indexV = new Variant((long) fingerprint.getIndex(), true);
        Variant flagV = new Variant(fingerprint.getFlag(), true);
        Variant tempV = new Variant(fingerprint.getFingerprint(), true);
        boolean result = zkem.invoke("SetUserTmpExStr", v0, sdwEnrollNumber, indexV, flagV, tempV).getBoolean();
        return result;
    }


    /**
     * 读取用户的面部识别数据
     * @param userId
     * @return
     */
    public static String getUserFace(String userId) {
        Variant v0 = new Variant(1L);
        Variant sdwEnrollNumber = new Variant(userId, true);
        String face = null;
        Variant indexV = new Variant((long) 50, true);
        Variant tempV = new Variant("", true);
        Variant lenV = new Variant(0, true);
        boolean result = zkem.invoke("GetUserFaceStr", v0, sdwEnrollNumber, indexV, tempV, lenV).getBoolean();
        if (result) {
            face = tempV.getStringRef();
        }
        return face;
    }


    /**
     * 更新用户的面部识别数据
     * @param userId
     * @param face
     * @return
     */
    public static boolean setUserFace(String userId,String face) {
        Variant v0 = new Variant(1L);
        Variant sdwEnrollNumber = new Variant(userId, true);
        Variant indexV = new Variant((long) 0, true);
        Variant faceV = new Variant(face, true);
        Variant lenV = new Variant((long)face.length(), true);
        boolean result = zkem.invoke("SetUserFaceStr", v0, sdwEnrollNumber, indexV, faceV, lenV).getBoolean();
        return result;
    }

    /**
     * 删除用户;
     */
    public static Boolean delectUserById(String dwEnrollNumber) {
        Variant v0 = new Variant(1);
        Variant sdwEnrollNumber = new Variant(dwEnrollNumber, true);
        /**
         * sdwBackupNumber：
         * 一般范围为 0-9，同时会查询该用户是否还有其他指纹和密码，如都没有，则删除该用户
         * 当为 10 是代表删除的是密码，同时会查询该用户是否有指纹数据，如没有，则删除该用户
         * 11 和 13 是代表删除该用户所有指纹数据，
         * 12 代表删除该用户（包括所有指纹和卡号、密码数据）
         */
        Variant sdwBackupNumber = new Variant(12);
        /**
         * 删除登记数据，和 SSR_DeleteEnrollData 不同的是删除所有指纹数据可用参数 13 实现，该函数具有更高效率
         */
        return zkem.invoke("SSR_DeleteEnrollDataExt", v0, sdwEnrollNumber, sdwBackupNumber).getBoolean();
    }
}
