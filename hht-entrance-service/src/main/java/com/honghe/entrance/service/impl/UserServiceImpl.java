package com.honghe.entrance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.honghe.dao.PageData;
import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.redis.RedisUtil;
import com.honghe.entrance.common.util.*;
import com.honghe.entrance.dao.*;
import com.honghe.entrance.entity.User;
import com.honghe.entrance.hitecloud.DataSynchronizationService;
import com.honghe.entrance.service.EntranceConfigService;
import com.honghe.entrance.service.OrganizationService;
import com.honghe.entrance.service.SysModuleService;
import com.honghe.entrance.service.UserService;
import com.honghe.entrance.util.ExcelTools;
import com.honghe.entrance.util.HttpServiceUtil;
import com.honghe.entrance.util.UserTypeEnum;
import jodd.io.FileUtil;
import jodd.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private Logger logger= LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private User2RoleDao user2RoleDao;
    @Autowired
    private EntranceConfigService entranceConfigService;
    @Autowired
    private DataSynchronizationService dataSynchronizationService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserSysPositionDao userSysPositionDao;
    @Autowired
    private OrganizationDao campusDao;
    @Autowired
    private UserSysPositionDao positionDao;
    @Autowired
    private SysModuleService sysModuleService;
    @Autowired
    private RedisUtil redisUtil;
    @Value("${entrance.module.ywIp}")
    private String ywIp;

    @Override
    public Result importUsers(String fileName) {
        return importUserInfo(fileName);
    }
    /**
     * 导入教师
     * @param fileName 文件名
     */
    private Result importUserInfo(String fileName) throws IllegalArgumentException {
        if (StringUtil.isEmpty(fileName)) {
            return new Result(Result.Code.ParamError.value(),"文件名称不能为空");
        }
        String filePath =  System.getProperty("user.dir") + File.separator + "upload_dir" + File.separator + fileName;
        String result="";
        try {
            // 文件流指向excel文件
            InputStream stream = new FileInputStream(filePath);
            // HSSF只能打开2003，XSSF只能打开2007，WorkbookFactory.create兼容以上两个版本
            Workbook workbook = WorkbookFactory.create(stream);
            // 得到索引为0的工作表
            Sheet sheet = workbook.getSheetAt(0);

            // 错误的用户信息
            List<User> failedDataList = new ArrayList();

            Row row;
            int totalRow = sheet.getLastRowNum();// 得到excel的总记录条数
            if(!checkExcelIsTrue(sheet.getRow(1))){
                return new Result(Result.Code.ParamError.value(),"用户模板错误");
            }
            for (int i = 2; i <= totalRow; i++) {
                //获取单行数据
                row = sheet.getRow(i);

                Cell userRealNameRow = row.getCell(0);  //姓名*
                Cell userMobileRow = row.getCell(1);    //账号(手机号)*
                Cell userTypeRow = row.getCell(2);      //用户类型*

                //当所有列都为空时则继续
                if (StringUtil.isEmpty(getCellValue(userRealNameRow)) &&
                        StringUtil.isEmpty(getCellValue(userMobileRow)) &&
                        StringUtil.isEmpty(getCellValue(userTypeRow))){
                    continue;
                }

                String userMobile  = getCellValue(userMobileRow);
                // 手机号代替用户名
                String userName=userMobile;
                String remark="";
                String userType=switchUserType(getCellValue(userTypeRow));
                Map<String,Object> addUsersResult=userAdd(userName,getCellValue(userRealNameRow),userMobile,userType,remark);
                if(addUsersResult!=null && !addUsersResult.isEmpty() && addUsersResult.containsKey("failedList")){
                    failedDataList.addAll((List<User>)addUsersResult.get("failedList"));
                }
            }
            if(failedDataList.isEmpty()){
                FileUtil.delete(filePath);
                result="success";
            }else{
                result = exportFailedUserExcel(failedDataList);
            }
        } catch (Exception e) {
            return new Result(-3,"文件读取失败");
        }
        Result resultValue=new Result(Result.Code.Success.value());
        resultValue.setResult(result);
        return resultValue;
    }
    private final String EXCEL_NAME_TITLE="姓名*";
    private final String EXCEL_MOBILE_TITLE="账号*";
    private final String EXCEL_USERTYPE_TITLE="用户类型*";

    /**
     * 将填入模板的用户类型转成数据库存储的类型
     * @param userType
     * @return
     */
    private String switchUserType(String userType) {
        switch (userType){
            case "0":
                return UserTypeEnum.USERTYPE_MANAGER.getType();
            case "1":
                return UserTypeEnum.USERTYPE_TEACHER.getType();
            default:
                return "error";
        }
    }
    /**
     * 验证模板是否正确
     * @param row
     * @return
     */
    private boolean checkExcelIsTrue(Row row) {
        String userRealName = row.getCell(0).toString();  //姓名
        String userMobile = row.getCell(1).toString();    //账号
        String userType = row.getCell(2).toString();      //用户类型
        if(EXCEL_NAME_TITLE.equals(userRealName) && EXCEL_MOBILE_TITLE.equals(userMobile)
                && EXCEL_USERTYPE_TITLE.equals(userType)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 获取cell中的数据
     * @param cell
     * @return java.lang.String
     * @Create Wangzy 2018/8/3 10:03
     * @Update Wangzy 2018/8/3 10:03
     */
    private String getCellValue(Cell cell){
        String cellValue = "";
        if (cell != null)  {
            int type = cell.getCellType();
            if (type == Cell.CELL_TYPE_NUMERIC) {
                double doubleVal = cell.getNumericCellValue();
                long longVal = Math.round(doubleVal);
                if (Double.parseDouble(longVal + ".0") == doubleVal) {
                    cellValue = String.valueOf(longVal);
                } else{
                    cellValue = String.valueOf(doubleVal);
                }
            }else if (type == Cell.CELL_TYPE_STRING){
                cellValue = cell.getStringCellValue();
            }else if (type == Cell.CELL_TYPE_BOOLEAN){
                cellValue = String.valueOf(cell.getBooleanCellValue());
            }
        }
        return cellValue;
    }

    /**
     * 将用户类型转换成模板填入的id
     * @param userType
     * @return
     */
    private String switchUserTypeToTemplateType(String userType) {
        switch (userType){
            case "管理员":
                return "0";
            case  "普通用户":
                return "1";
        }
        return "";
    }

    /**
     * 将用户id转换成中文
     * @param userType
     * @return
     */
    private String switchUserTypeToName(String userType) {
        switch (userType){
            case "3":
                return UserTypeEnum.USERTYPE_MANAGER_NAME.getType();
            case  "17":
                return UserTypeEnum.USERTYPE_TEACHER_NAME.getType();
        }
        return "";
    }
    /**
     * 将导入失败的数据回写到excel文件中
     *
     * @param userList   插入数据库异常数据
     * @return String
     * @throws IllegalArgumentException
     */
    private String exportFailedUserExcel(List<User> userList) throws IllegalArgumentException {
        // 生成表头
        String[] headers = {"姓名*", "账号*", "用户类型*","错误原因"};
        String fileName = "错误用户列表";
        ExcelTools excel = new ExcelTools();
        List<String[]> excelList = new ArrayList<>();
        // 插入数据
        for (int i = 0; i < userList.size(); i++) {
            String[] strList = new String[headers.length];
            User user = userList.get(i);
            strList[0] = StringUtil.isEmpty(user.getUserRealName()) ? "" : user.getUserRealName();
            strList[1] = StringUtil.isEmpty(user.getUserMobile())? "" : user.getUserMobile();
            strList[2] = StringUtil.isEmpty(String.valueOf(user.getUserType())) ? "" :
                    switchUserTypeToTemplateType(String.valueOf(user.getUserType()));
            strList[3] = StringUtil.isEmpty(user.getErrorMsg())?"":user.getErrorMsg();
            excelList.add(strList);
        }
        Date date = new Date();
        String filePath = System.getProperty("user.dir") + File.separator + "upload_dir"+ File.separator;
        String name = "user_failed_"+date.getTime();

        try {
            filePath = excel.exportTableByFile(fileName, headers, excelList, filePath, name)[0];
        } catch (IOException e) {
            ;
        }
        //文件上传到upload_dir,文件下载路径：http://ip:port/upload_dir/tempDownExcel1232.xls
        return name + ".xls";
    }

    /**
     * 封装错误用户信息
     * @param userRealName
     * @param userMobile
     * @param userType
     * @param remark
     * @param errorMsg
     * @return
     */
    private User getFailedUser(String userRealName, String userMobile, String userType, String remark, String errorMsg) {
        User failedUser=new User();
        failedUser.setUserRealName(userRealName);
        failedUser.setUserMobile(userMobile);
        failedUser.setUserType(switchUserTypeToName(userType));
        failedUser.setRemark(remark);
        if(errorMsg.endsWith(",")){
            errorMsg=errorMsg.substring(0,errorMsg.lastIndexOf(","));
        }
        failedUser.setErrorMsg(errorMsg);
        return failedUser;
    }
    private static final int MAX_LENGTH = 255;
    private static final String DEFAULT_PASSWORD = "123456";
    public static final String LOCAL_USER = "1";

    @Override
    public synchronized Map<String,Object> userAdd(String userName,String userRealName, String userMobile, String userType,String remark)
            throws IllegalArgumentException{
        Map<String,Object> resultMap=new HashMap<>();
        List<User> failedList=new ArrayList<>();
        String errorMsg="";
        if(StringUtil.isEmpty(userRealName)){
            errorMsg+="用户真实姓名不能为空,";
        }
        if(!StringUtil.isEmpty(userRealName) && userRealName.length()>10){
            errorMsg+="用户真实姓名不能超过10个字符,";
        }
        if(StringUtil.isEmpty(userMobile)){
            errorMsg+="手机号码不能为空,";
        }
        if (!StringUtil.isEmpty(userMobile) && (!ValidatorUtil.isMobile(userMobile) && !ValidatorUtil.checkTelephone(userMobile))) {
            errorMsg+="手机号格式非法,";
        }
        if (StringUtil.isEmpty(userType)) {
            errorMsg+="请先选择用户类型,";
        }
        if ("error".equals(userType)) {
            errorMsg+="用户类型错误,";
        }
        if(!StringUtil.isEmpty(remark) && remark.length()>MAX_LENGTH){
            errorMsg+="用户备注信息超长,";
        }
        if(!StringUtil.isEmpty(errorMsg)) {
            failedList.add(getFailedUser(userRealName,userMobile,userType,remark,errorMsg));
            resultMap.put("failedList", failedList);
            resultMap.put("addUserId",0);
            return resultMap;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userType", userType);
        map.put("userPwd", DEFAULT_PASSWORD);
        map.put("userName", userMobile);
        map.put("userRealName", userRealName);
        //性别未知
        map.put("userGender", 1);
        map.put("userMobile", userMobile);
        map.put("userSource",LOCAL_USER);
        map.put("userUuid", UUIDUtil.getUUID());
        map.put("remark",remark);
        if (userDao.isExits(map)>0) {
            failedList.add(getFailedUser(userRealName,userMobile,userType,remark,"用户名或者手机号重复"));
            resultMap.put("failedList",failedList);
            resultMap.put("addUserId",-4);
            return resultMap;
        }
        int userId = addUser(map);
        if (userId == 0){
            failedList.add(getFailedUser(userRealName,userMobile,userType,remark,"添加失败"));
            resultMap.put("failedList",failedList);
            resultMap.put("addUserId",0);
            return resultMap;
        }else{
            resultMap.put("addUserId",userId);
        }
        //添加各种关系
        user2RoleDao.deleteByUserId(String.valueOf(userId));
        userSysPositionDao.deleteSysModulesByUserId(String.valueOf(userId));
        //默认添加角色及用户与角色的对应关系，为了解决用户与权限的直接对应关系
        //user2RoleDao.addUser2Role(String.valueOf(userId),String.valueOf(userId));
        roleDao.addRole(String.valueOf(userId),null,"1");
        user2RoleDao.addUser2Role(String.valueOf(userId),String.valueOf(userId));
        //增加用户与根节点组织机构关系
        userDao.addCampus2User(userId);
        //根据角色获取权限并添加用户权限模块
//        List<String> moduleIdList = role2PermissionDao.findModuleIdsByRoleIds(roleIdList);
        //查询默认模块布局
        List<Map<String,Object>> positionList = userSysPositionDao.getDefaultPosition();
        List<Map<String,Object>> positionSqlList=new ArrayList<>();
        if(positionList!=null && !positionList.isEmpty()) {
            int isDelete=0;
            for (Map<String,Object> position : positionList) {
                if("4".equals(position.get("moduleId")) || "6".equals(position.get("moduleId"))){
                    isDelete=1;
                }else{
                    isDelete=0;
                }
                Map<String,Object> params=new HashMap<>();
                params.put("userId",userId);
                params.put("moduleId",(int)position.get("moduleId"));
                params.put("x",position.get("x"));
                params.put("y",position.get("y"));
                params.put("w",position.get("w"));
                params.put("h",position.get("h"));
                params.put("minW",position.get("minW"));
                params.put("minH",position.get("minH"));
                params.put("isDelete",isDelete);
                params.put("isDraggable",1);
                params.put("isResizable",0);
                positionSqlList.add(params);
            }
            if(!positionSqlList.isEmpty()){
                //添加用户和默认模块的对应关系
                userSysPositionDao.addUser2Position(positionSqlList);
            }
            //添加用户与默认模块关系
            sysModuleService.addRole2DefaultSysModule(userId);
        }
        return resultMap;
    }


    @Override
    public int addUser(Map<String, Object> data) {
        String orgCode = "5422a0354ec549f78973d8422e5b14c4";
       if (data.containsKey("orgCode")) {
            orgCode = data.get("orgCode").toString().trim();
        }
        //用户编号
        String userCode = UUID.randomUUID().toString().replace("-", "");
        if (data.containsKey("userCode")) {
            userCode = data.get("userCode").toString().trim();
        }
        int userStatus = 1;
        if (data.containsKey("userStatus") && !data.get("userStatus").toString().trim().equals("")) {
            userStatus = Integer.parseInt(data.get("userStatus").toString().trim());
        }
        //教师、管理员
        String type = String.valueOf(data.get("userType")).trim();
        int userSource=Integer.parseInt(String.valueOf(data.get("userSource")));
        String userName=data.get("userName").toString();
        String userRealName=data.get("userRealName").toString();
        String userPwd=data.get("userPwd").toString();
        String userMobile=data.get("userMobile").toString();
        int userGender=Integer.parseInt(String.valueOf(data.get("userGender")));
        String remark=data.get("remark")==null?"":data.get("remark").toString();
        String uuid=data.get("userUuid").toString();
        //向user表中添加数据
        String salt = SaltRandom.runVerifyCode(6);
        userPwd = SaltRandom.createPassword(userPwd, salt);
        User user=new User(userName, userRealName, userMobile, type, userStatus, userPwd, userGender,
                userSource, uuid, userCode,salt,orgCode,remark);
        userDao.add(user);
        return user.getUserId();
    }

    @Override
    public Result userCheck(String userName, String userPwd, String userCode) {
        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(userPwd)){
            return new Result(Result.Code.ParamError.value(),"用户名或密码不能为空");
        }else if(!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(userPwd)){
            Result result=new Result(Result.Code.Success.value());
            //获取token
            String userToken = IDUtil.getToken(userName);
            if (userToken == null) {
                return result;
            }
            //验证用户信息
            List<Map<String, String>> userList = findUserInfo(userName);
            //登录验证
            result = checkUserLogin(userName,userPwd,userCode,userList,result);
            Map<String,String> userMap=(Map<String,String>)result.getResult();
            if (userMap!=null && userMap.size() < 1) {
                return result;
            }
            try {
                //存入string
                /*if (!RedisUtil.setString(userToken, RedisUtil.KEYTIMEOUT, String.valueOf(userMap.get("userId")))) {
                    return result;
                }*/
                //往redis里赋值
                redisUtil.hset(RedisUtil.KEY,userToken, JSON.toJSONString(userList.get(0)));
            }catch (Exception e){
                result.setOther("读取redis用户信息异常");
                return result;
            }
            //TODO：为了与原入口代码保持一致，将整型改为字符串
            changMapValueToStr(userMap);
            //补充用户信息
            userMap.put("userToken", userToken);
            //目前只有校园版：1为教育机构，2为学校，3为教育部门
            userMap.put("orgType", "2");
            //登录验证方式，本地登录验证为1，其他为爱学登录验证
            userMap.put("entranceConfig", String.valueOf(entranceConfigService.getEntranceConfig().getResult()));
            result.setResult(userMap);
            return result;
        }
        else {
            return new Result(Result.Code.ParamError.value(),"用户名、密码不能为空");
        }
    }

    /**
     * 将用户信息的值由整型转化为字符串，为了和原代码保持一致
     * @param userMap
     * @return
     */
    private Map<String, String>  changMapValueToStr(Map<String, String> userMap) {
        if(userMap!=null && !userMap.isEmpty()) {
            if(userMap.containsKey("userId")) {
                String userId=String.valueOf(userMap.get("userId"));
                userMap.put("userId", userId);
                if("1".equals(userId) || "2".equals(userId)){
                    userMap.put("loginCount","1");
                }else {
                    if(userMap.containsKey("loginCount")) {
                        userMap.put("loginCount", String.valueOf(userMap.get("loginCount")));
                    }else{
                        userMap.put("loginCount","");
                    }
                }
            }else{
                userMap.put("userId","");
            }
            if(userMap.containsKey("userStatus")) {
                userMap.put("userStatus", String.valueOf(userMap.get("userStatus")));
            }else{
                userMap.put("userStatus","");
            }
            if(userMap.containsKey("userGender")) {
                userMap.put("userGender", String.valueOf(userMap.get("userGender")));
            }
            else{
                userMap.put("userGender","");
            }
            if(userMap.containsKey("userType")) {
                userMap.put("userType", String.valueOf(userMap.get("userType")));
            }else{
                userMap.put("userType","");
            }
            if(userMap.containsKey("userSource")) {
                userMap.put("userSource", String.valueOf(userMap.get("userSource")));
            }else{
                userMap.put("userSource","");
            }
            if(userMap.containsKey("roleId")) {
                userMap.put("roleId", String.valueOf(userMap.get("roleId")));
            }else{
                userMap.put("roleId","");
            }
        }
        return userMap;
    }

    /**
     * 根据用户查询用户信息
     * @param name
     * @return
     */
    private List<Map<String,String>> findUserInfo(String name) {
        /* String condition = "";
       if (ValidatorUtil.isEmail(name)) {
            condition = " user_email = '" + name + "' ";
        } else if (ValidatorUtil.isMobile(name)) {
            condition = " user_mobile = '" + name + "' ";
        } else {
            condition = " (user_name='" + name + "' or user_num = '" + name + "')";
        }*/
        return userDao.findUserInfo(name);
    }

    private Result checkUserLogin(String name, String userPwd, String code, List<Map<String, String>> list, Result result) {
        Map<String, String> resultMap = new HashMap<>();
        if (list.size() == 1) {
            resultMap = list.get(0);
            userLogin(name,userPwd,code,resultMap,result);
        } else if (list.size() == 0) {
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("用户名不存在");
        } else {
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("用户名重复，请联系管理员处理");
        }
        return result;
    }
    //爱学账户
    public static final String HITE_CLOUD_USER = "0";

    private Result userLogin(String name, String password, String code, Map<String,String> resultMap, Result result){
        if (resultMap.isEmpty()) {
            return result;
        }
        String status=String.valueOf(resultMap.get("userStatus"));
        if(status.equals("2")){
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("用户已被禁用，请联系管理员处理");
        }else if(status.equals("0")){
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("用户未激活，请联系管理员处理");
        }else if (status.equals("3")){
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("用户未激活且被禁用，请联系管理员处理");
        }
        /**
         * 入口登录验证
         */
        //本地验证，所有用户均使用本地账号才能登录
        String userSalt = String.valueOf(resultMap.get("userSalt"));
        String userType = String.valueOf(resultMap.get("userType"));
        int checkConfig=(int)entranceConfigService.getEntranceConfig().getResult();
        if(checkConfig==1 || UserTypeEnum.SUPER_USERTYPE_ADMIN.getType().equals(userType)){
            checkUserIsValid(name,password,userSalt,resultMap,result);
            if(Result.Code.Success.value()!=result.getCode()){
                return result;
            }else {
                result.setResult(resultMap);
            }
        }
        //需要绑定爱学账户并用本地账号登录
        else{
            String userSource=String.valueOf(resultMap.get("userSource"));
            //已绑定到爱学的用户直接本地密码登录
            if(HITE_CLOUD_USER.equalsIgnoreCase(userSource)){
                checkUserIsValid(name,password,userSalt,resultMap,result);
                if(Result.Code.Success.value()!=result.getCode()){
                    return result;
                }else {
                    result.setResult(resultMap);
                }
            }
            //未绑定到爱学的用户，如果爱学有账号，则激活；如果爱学无账号则注册爱学用户并激活
            else{
                //首次登录，不能登录成功
                if(StringUtil.isEmpty(code)){
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("首次登录请输入验证码");
                }
                //用户获取验证码，绑定用户到爱学并验证码登录
                else {
                    String userName=resultMap.get("userName").toString();
                    password = SaltRandom.createPassword(password, userSalt);
                    //验证密码是否正确
                    if(name.equals(userName) && password.equalsIgnoreCase(resultMap.get("userPwd").toString())){
                        if(loginByCode(name,code,resultMap)){
                            Result errorResult = synLocalUser(checkConfig,resultMap);
                            if(errorResult.getCode()!= Result.Code.Success.value()){
                                return errorResult;
                            }else{
                                result.setResult(resultMap);
                            }
                        }else{
                            result.setCode(-4);
                            result.setOther("绑定爱学用户失败");
                        }
                    }else{
                        result.setCode(Result.Code.ParamError.value());
                        result.setOther("密码不正确");
                    }
                }
            }
        }
        return result;
    }
    /**
     * 验证码登录
     * @param name        用户名
     * @param code        验证码
     * @param result      用户信息
     * @author  caoqian
     * @return
     */
    private boolean loginByCode(String name,String code,Map<String,String> result){
        boolean loginResult=dataSynchronizationService.checkMobileCode(name,code);
        if (loginResult) {
            result.remove("userPwd");
            result.remove("userSalt");
        }
        return loginResult;
    }
    /**
     * 验证用户是否可以登录
     * @param name        用户名
     * @param password    密码
     * @param userSalt    盐值
     * @param result      用户信息
     * @author caoqian
     * @return
     */
    private Result checkUserIsValid(String name, String password, String userSalt, Map<String,String> resultMap, Result result){
        String userName=resultMap.get("userName").toString();
        password = SaltRandom.createPassword(password, userSalt);
        if(name.equals(userName) && password.equalsIgnoreCase(resultMap.get("userPwd").toString())){
            resultMap.remove("userPwd");
            resultMap.remove("userSalt");
        }else{
            result.setCode(-4);
            result.setOther("用户名或密码不正确,登录失败");
        }
        return result;
    }
    /**
     * 同步本地用户到爱学班班
     * @author caoqian
     * @return
     */
    private Result synLocalUser(int checkConfig, Map<String,String> resultMap) {
        Result result=new Result(Result.Code.Success.value());
        if(StringUtil.isEmpty(resultMap.get("userSource")) || StringUtil.isEmpty(String.valueOf(resultMap.get("userId"))) ||
                StringUtil.isEmpty(resultMap.get("userMobile"))){
            result.setCode(Result.Code.ParamError.value());
            result.setOther("用户id或用户来源或手机号码不能为空");
        }
        //首先判断验证方式，验证方式为本地时，不能同步用户
        if(checkConfig==1){
            result.setCode(Result.Code.ParamError.value());
            result.setOther("本地验证方式不能同步用户");
        }else{
            String userId=String.valueOf(resultMap.get("userId"));
            //爱学班班用户不能同步用户
            String userSource=String.valueOf(resultMap.get("userSource"));
            if(HITE_CLOUD_USER.equals(userSource)){
                result.setCode(Result.Code.ParamError.value());
                result.setOther("爱学用户不能同步用户");
            }else{
                //本地用户同步爱学班班
                String userMobile=resultMap.get("userMobile").toString();
                if(StringUtil.isEmpty(userMobile)){
                    result.setCode(Result.Code.ParamError.value());
                    result.setOther("手机号为空不能拉取爱学用户信息");
                }else{
                    try {
                        Map<String, Object> hiteCloudUser = getHiteCloudUserByMobile(userMobile);
                        //爱学班班已注册该用户，需要绑定用户
                        if (hiteCloudUser != null && !hiteCloudUser.isEmpty()) {
                            String userRealName=hiteCloudUser.get("userRealName").toString();
                            //更新返回结果的真实姓名
                            resultMap.put("userRealName",userRealName);
                            boolean sysValue = userDao.synUserData(Integer.parseInt(userId), userRealName);
                            result.setResult(sysValue);
                            return result;
                        }
                        //爱学班班未注册该用户，则首先注册用户并同步数据
                        else {
                            Map<String,String> userInfo=userDao.findUserById(Integer.parseInt(userId));
                            if(userInfo!=null && !userInfo.isEmpty()) {
                                String userRealName=userInfo.get("userRealName");
                                JSONObject regResult = dataSynchronizationService.registerUser(userRealName,
                                        DEFAULT_PASSWORD, userMobile);
                                if (regResult != null && regResult.size() > 0) {
                                    boolean sysUser = userDao.synUserData(Integer.parseInt(userId), userRealName);
                                    result.setResult(sysUser);
                                    return result;
                                } else {
                                    result.setCode(-4);
                                    result.setOther("注册爱学用户失败");
                                }
                            }
                        }
                    }catch (Exception e){
                        result.setCode(-4);
                        result.setOther("拉取爱学用户信息失败");
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<String,Object> getHiteCloudUserByMobile(String mobile){
        Map<String, Object> valueMap = new HashMap<>();
        if(!StringUtil.isEmpty(mobile)){
            JSONObject result = dataSynchronizationService.getUserByMobile(mobile);
            if (result != null && result.size() > 0) {
                valueMap.put("userName", result.get("username") == null ? "" : result.get("username"));
                valueMap.put("userRealName", result.get("realname") == null ? "" : result.get("realname"));
                valueMap.put("userMobile", result.get("mobile"));
                valueMap.put("userType", result.get("usertype"));
            }
        }
        return valueMap;
    }

    @Override
    public Result checkUserIsFirstLoginByName(String loginName) {
        if(StringUtils.isEmpty(loginName)){
            return new Result(Result.Code.ParamError.value(),"用户名不能为空");
        }
        Map<String,Object> resultMap = userDao.searchUserByName(loginName);
        Result result=new Result(Result.Code.Success.value());
        if(resultMap!=null && !resultMap.isEmpty()){
            String userType=String.valueOf(resultMap.get("userType"));
            //超级管理员与引导项添加的管理员不用验证是否首次登录
            if(!UserTypeEnum.SUPER_USERTYPE_ADMIN.getType().equals(userType) &&
                    UserServiceImpl.LOCAL_USER.equals(String.valueOf(resultMap.get("userSource")))) {
                result.setResult(true);
            }else{
                result.setResult(false);
            }
        }else{
            result.setCode(-4);
            result.setOther("用户不存在");
        }
        return result;
    }

    @Override
    public Result checkUserIsUpdatePwd(String loginName) {
        if(StringUtil.isEmpty(loginName)){
            return new Result(Result.Code.ParamError.value(),"用户名不能为空");
        }
        Result result=new Result(Result.Code.Success.value());
        Map<String,Object> resultMap = userDao.searchUserByName(loginName);
        if(resultMap!=null && !resultMap.isEmpty()) {
            //用登录次数验证是否修改过密码，修改了密码增加1
            int userCount = Integer.parseInt(String.valueOf(resultMap.get("loginCount")));
            if(userCount>0){
                result.setResult(true);
            }else{
                result.setResult(false);
            }
        }else{
            result.setCode(-4);
            result.setOther("用户不存在");
        }
        return result;
    }

    @Override
    public Result userSearch(String userId) {
        Map<String, String> userInfo=userDao.findUserById(Integer.parseInt(userId));
        Map<String,Object> userResult=new HashMap<>();
        userResult.putAll(changMapValueToStr(userInfo));
        userResult.put("exitsRootOrg", organizationService.exitsRootOrg());
        Result result=new Result(Result.Code.Success.value());
        result.setResult(userResult);
        return result;
    }

    @Override
    public Result userUpdate(String userId, String userRealName, String userName, String userType, String userMobile, String userRemark) {
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(userType)) {
            return new Result(Result.Code.ParamError.value(), "用户id或用户类型不能为空");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        if (userRealName != null ) {
            map.put("userRealName", userRealName);
        }
        if (userName != null ) {
            map.put("userName", userName);
        }
        if (!StringUtil.isEmpty(userMobile)) {
            if(ValidatorUtil.isMobile(userMobile)) {
                map.put("userMobile", userMobile);
            }else{
                return new Result(Result.Code.ParamError.value(),"手机号格式非法");
            }
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        if (userRemark != null) {
            map.put("userRemark", userRemark);
        }
        Result result=new Result(Result.Code.Success.value());
        if (userDao.updateUser(map)){
            result.setResult(0);
        }else {
            result.setCode(Result.Code.ParamError.value());
            result.setOther("更新失败");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result userDelete(String userId) {
        if(StringUtils.isEmpty(userId)){
            return new Result(Result.Code.ParamError.value(),"用户id不能为空");
        }
        String userIds[] = userId.split(",");
        try {
            //删除用户
            userDao.deleteUsers(userIds);
            //删除角色
            roleDao.deleteRoles(userIds);
            //删除角色与模块关联
            roleDao.deleteRole2Modules(userIds);
            //删除用户角色关联
            userDao.deleteUser2Role(userIds);
            //删除用户模块关联
            positionDao.deletePositions(userIds);
            //删除用户与根节点组织机构的关系
            campusDao.deleteCampus2Users(userIds);
            return new Result(Result.Code.Success.value(),true);
        }catch (Exception e){
            return new Result(Result.Code.UnKnowError.value(),"删除用户异常");
        }
    }
    private static final String SUCCESS = "0";//成功
    private static final String FAIL = "2";//失败
    private static final String MISTAKE = "1";//授权系统名称
    @Override
    public Result userChangePassword(String userId, String oldPassword, String newPassword) {
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(newPassword)){
            return new Result(Result.Code.ParamError.value(),"用户id或密码不能为空");
        }
        Result result=new Result(Result.Code.Success.value());
        int uid=Integer.parseInt(userId);
        Map<String, String> userInfo = userDao.findUserById(uid);
        if (userInfo.isEmpty()) {
            result.setResult(FAIL);
            return result;
        }
        String salt = SaltRandom.runVerifyCode(6);
        String newPwd = SaltRandom.createPassword(newPassword, salt);

        //不是初始化密码，需要判断原密码是否正确
        boolean re_value=false;
        if (!StringUtil.isEmpty(oldPassword)) {
            String userSalt = userInfo.get("userSalt");
            String password = SaltRandom.createPassword(oldPassword, userSalt);
            if (!userInfo.get("userPwd").equalsIgnoreCase(password)) {
                result.setResult(MISTAKE);
            }
            re_value = userDao.updateUserPassword(uid,salt,newPwd);
        } else {
            re_value = userDao.updateUserPassword(uid,salt,newPwd);
            //初始化时修改了密码，登录次数置为1
            userDao.updateLoginCount(uid);
        }
        if (re_value) {
            result.setResult(SUCCESS);
            //入口修改密码，通知运维平台同步密码，只有admin通知即可
            if("2".equals(userId)) {
                updatePassword(uid, newPassword);
            }
        }
        return result;
    }

    @Override
    public Result userList(int pageSize, int pageCount, String userType, String searchKey, String userId) {
        //查询用户数量
        int count = userDao.searchUserCount(userId,searchKey);
        int start = PageData.calcFirstItemIndexOfPage(pageCount, pageSize, count);
        List<Map<String,String>> userList=userDao.userPageList(userId,searchKey,start,pageSize,true);
        PageData pageData = new PageData(pageCount, count, pageSize, userList);
        Result result=new Result(Result.Code.Success.value());
        result.setResult(pageData);
        return result;
    }

    @Override
    public Result exportUsers(String userId, String userType, String searchKey) {
        List<Map<String,String>> userList=userDao.userPageList(userId,searchKey,0,0,false);
        if (userList == null || userList.size()==0){
            return new Result(Result.Code.NoSuchMethod.value(),"没有找到需要导出的数据");
        }
        // 导出数据
        String fileName = exportTeacherExcel(userList);
        if (fileName == null || fileName.equals("")){
            return new Result(Result.Code.UnKnowError.value(),"导出数据内部错误");
        }
        Result result=new Result(Result.Code.Success.value());
        result.setResult(fileName);
        return result;
    }

    @Override
    public Result resetPwd(String userId) {
        if(StringUtils.isEmpty(userId)){
            return new Result(Result.Code.ParamError.value(),"用户id不能为空");
        }
        String salt = SaltRandom.runVerifyCode(6);
        String userPwd = SaltRandom.createPassword(DEFAULT_PASSWORD, salt);
        if("2".equals(userId)){
            updatePassword(Integer.parseInt(userId), userPwd);
        }
        boolean resetPwd=userDao.resetPwd(salt,userPwd,userId);
        Result result=new Result(Result.Code.Success.value());
        result.setResult(resetPwd);
        return result;
    }

    @Override
    public Result keepAlive() {
        Map<String, String> re_value = new HashMap<>();
        re_value.put("token", "ENTRANCE");
        Result result=new Result(Result.Code.Success.value());
        result.setResult(re_value);
        return result;
    }

    @Override
    public Result searchUserInfoByToken(String userToken) {
        if (StringUtils.isEmpty(userToken)) {
           return new Result(Result.Code.ParamError.value(),"userToken参数不能为空");
        }
        Result result=new Result(Result.Code.Success.value());
        //修改存入为String后的获取
//        String userId = RedisUtil.getString("userCheck_" + userToken);
        JSONObject userInfo=JSONObject.parseObject(redisUtil.hget(RedisUtil.KEY,userToken)) ;

        if (userInfo == null || userInfo.isEmpty()) {
            return result;
        }
        //设置有效时长
        redisUtil.expire(RedisUtil.KEY,RedisUtil.KEYTIMEOUT);
        Map userMap = new HashMap();
        userMap.put("userId", userInfo.getString("userId"));
        result.setResult(userMap);
        return result;
    }

    @Override
    public Result clearRedis(String token) {
        if(StringUtil.isEmpty(token)){
            return new Result(Result.Code.ParamError.value(),"token不能为空");
        }
        Result result=new Result(Result.Code.Success.value());
        try {
            redisUtil.hdel(RedisUtil.KEY, token);
            result.setResult(true);
        }catch (Exception e){
            result.setCode(Result.Code.UnKnowError.value());
            result.setOther("根据token清空redis异常");
            result.setResult(false);
            logger.error("根据token清空redis异常",e);
        }
        return result;
    }

    //导出的用户列表与模板一致
    private String exportTeacherExcel(List userList) {
        String fileName="user_"+System.currentTimeMillis()+".xls";
        String userTemplatePath=System.getProperty("user.dir")+ File.separator+"template"+File.separator+"entrance"+
                File.separator+"teacher_template.xls";
        String userTemplateTargetPath=System.getProperty("user.dir")+ File.separator+"upload_dir"+File.separator+fileName;
        //将用户模板拷贝到upload_dir文件夹
        copyUserTemplate(userTemplatePath,userTemplateTargetPath);
        File file=new File(userTemplateTargetPath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                ;
            }
        }
        InputStream stream = null;
        Workbook workbook = null;
        try {
            stream = new FileInputStream(file);
            workbook = WorkbookFactory.create(stream);
            //获取表格一的工作表
            Sheet sheet = workbook.getSheetAt(0);

            //获取表格一中 第3行(根据索引)
            if (userList != null && userList.size()>0){
                int totalCount = userList.size();
                for (int i = 0; i < totalCount; i++) {
                    try {
                        Map item = (Map) userList.get(i);
                        Row row = sheet.createRow(2+i);
                        row.setHeight((short) 300);
                        //获取表格一中 第3行的第1列(根据索引)
                        if (item.containsKey("userRealName")) {
                            Cell cell = row.createCell(0);
                            cell.setCellValue(item.get("userRealName").toString());
                        }
                        if (item.containsKey("userMobile")) {
                            Cell cell = row.createCell(1);
                            cell.setCellValue(item.get("userMobile").toString());
                        }
                        if (item.containsKey("userType")) {
                            Cell cell = row.createCell(2);
                            String userType = String.valueOf(item.get("userType"));
                            if ("17".equals(userType)) {
                                userType = "1";
                            }else{
                                userType = "0";
                            }
                            cell.setCellValue(userType);
                        }
                    }catch (Exception e){
                        logger.error("导出用户模板异常",e);
                    }
                }
            }else{
                Row row = sheet.getRow(2);
                for(int i=0;i<3;i++){
                    Cell cell = row.getCell(i);
                    cell.setCellValue("");
                }
            }
            OutputStream newFileStream = new FileOutputStream(userTemplateTargetPath);
            workbook.write(newFileStream);
        }catch (Exception e){
            logger.error("导出用户列表异常",e);
        }finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                }
            }
        }
        return fileName;
    }

    /**
     * 复制用户模板
     * @param filePath
     * @param targetPath
     * @author  caoqian
     */
    private void copyUserTemplate(String filePath,String targetPath){
        try {
            FileUtil.copy(filePath,targetPath);
        } catch (IOException e) {
            logger.error("复制用户模板异常");
        }
    }

    /**
     * 调用运维接口
     * @param userId 用户id
     * @param newPassword 新密码
     */
    private String updatePassword(int userId,String newPassword){
        Map<String,Object> params =new HashMap<>();
        params.put("userId",userId);
        params.put("newPassword",newPassword);
        String intefaceUrl = "http://" + ywIp + ":8308/user/updatePassword"+"?"+ HttpServiceUtil.convertParams(params);
        JSONObject result = HttpServiceUtil.httpGetOutTimeAndRetry(intefaceUrl, null , 3);
        String resultStr="";
        if (result != null && !result.isEmpty() && "0".equals(result.get("code").toString())) {
            resultStr = result.getString("result");
        }
        return resultStr;
    }

}
