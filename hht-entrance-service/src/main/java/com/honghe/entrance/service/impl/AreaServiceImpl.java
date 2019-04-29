package com.honghe.entrance.service.impl;

import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.common.util.SerialNumUtil;
import com.honghe.entrance.common.util.UUIDUtil;
import com.honghe.entrance.common.util.ValidatorUtil;
import com.honghe.entrance.entity.DMArea;
import com.honghe.entrance.dao.AreaDao;
import com.honghe.entrance.service.AreaService;
import com.honghe.entrance.util.ExcelTools;
import jodd.util.StringUtil;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class AreaServiceImpl implements AreaService {
    private Logger logger= Logger.getLogger(AreaServiceImpl.class);
    //学校下区域
    private static final  int TYPE_AREA_UNDER_SCHOOL = 2;
    private static String MAX_AREA_CODE="";
    private static int AREA_ID=1;
    private final static String EXCEL_PARENT_AREA_TITLE="上级地点*";
    private final static String EXCEL_CHILD_AREA_TITLE="地点名称*";
    private final static String EXCEL_REMARK_TITLE="备注";
    //校区id
    private static String SCHOOL_ZONE_ID="";
    @Autowired
    private AreaDao areaDao;

    @Override
    public Result importAreas(String fileName) {
        if(StringUtil.isEmpty(fileName)) {
            return new Result(Result.Code.ParamError.value(),"参数错误：文件名不能为空");
        }
        InputStream stream = null;
        Workbook workbook = null;
        String filePath=System.getProperty("user.dir")+File.separator+"upload_dir"+File.separator+fileName;
        File file=new File(filePath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                ;
            }
        }
        String result="";
        try {
            // 文件流指向excel文件
            stream = new FileInputStream(file);
            //HSSF只能打开2003，XSSF只能打开2007，WorkbookFactory.create兼容以上两个版本
            workbook = WorkbookFactory.create(stream);

            //设置CELL格式为文本格式 防止科学计数问题
            CellStyle cellStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            cellStyle.setDataFormat(format.getFormat("@"));
            Sheet sheet = workbook.getSheetAt(0);// 得到工作表
            Result addBatchAreaResult=importHandler(sheet);
            if(addBatchAreaResult!=null && addBatchAreaResult.getCode()==Result.Code.Success.value()) {
                Map<String,Object> areaMap=(Map<String,Object>)addBatchAreaResult.getResult();
                if(areaMap!=null && !areaMap.isEmpty()) {
                    List<DMArea> failedResult = (List<DMArea>) areaMap.get("failedList");
                    if (failedResult != null && !failedResult.isEmpty()) {
                        result = exportExcel(failedResult);
                    } else {
                        //导入成功时删除模板文件
                        FileUtils.forceDelete(file);
                        result = "success";
                    }
                }
            }else{
                return addBatchAreaResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(Result.Code.UnKnowError.value(),"导入地点异常");
        } finally {
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
        Result resultValue=new Result(Result.Code.Success.value());
        resultValue.setResult(result);
        return resultValue;
    }
    /**
     * 将导入失败的数据回写到excel文件中
     *
     * @param areaList   插入数据库异常数据
     * @return String
     * @throws IllegalArgumentException
     */
    private String exportExcel(List<DMArea> areaList) throws IllegalArgumentException {
        // 生成表头
        String[] headers = {"上级地点*", "地点名称*", "备注","错误原因"};
        String fileName = "错误地点列表";
        ExcelTools excel = new ExcelTools();
        List<String[]> excelList = new ArrayList<>();
        // 插入数据
        for (int i = 0; i < areaList.size(); i++) {
            String[] strList = new String[headers.length];
            DMArea area = areaList.get(i);
            strList[0] = StringUtil.isEmpty(area.getAreaParentName()) ? "" : area.getAreaParentName();
            strList[1] = StringUtil.isEmpty(area.getAreaName())? "" : area.getAreaName();
            strList[2] = StringUtil.isEmpty(area.getAreaRemark()) ? "" : area.getAreaRemark();
            strList[3] = StringUtil.isEmpty(area.getErrorMsg())?"":area.getErrorMsg();
            excelList.add(strList);
        }
        Date date = new Date();
        String filePath = System.getProperty("user.dir") + File.separator + "upload_dir"+ File.separator;
        String name = "area_failed_" + date.getTime();
        try {
            filePath = excel.exportTableByFile(fileName, headers, excelList, filePath, name)[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        //文件上传到upload_dir,文件下载路径：http://ip:port/upload_dir/tempDownExcel1232.xls
        return name + ".xls";
    }

    /**
     * 导入excel表
     * ps:地点名称不能重复，否则无法区分地点
     */
    public Result importHandler(Sheet sheet) {
        Row row;// 对应excel的行
        int totalRow = sheet.getLastRowNum();// 得到excel的总记录条数
        //获取最大的地点id
        DMArea rootDmArea=areaDao.getSchoolInfo();
        LinkedMap<String,DMArea> dmAreaMap=new LinkedMap<>();
        Map<String,Object> resultMap=new HashMap<>();
        List<DMArea> failedList = new LinkedList<>();
        if(!checkExcelIsTrue(sheet.getRow(1))){
            return new Result(Result.Code.ParamError.value(),"地点模板错误");
        }
        List<Map<String,String>> allAreaList=getAllAreas();
        MAX_AREA_CODE = areaDao.getMaxAreaCode();
        int maxAreaId=Integer.parseInt(areaDao.getMaxAreaId());
        //保存最大的地点id
        Map<String,Object> idMap=new HashMap<>();
        idMap.put("maxAreaId",maxAreaId);
        for (int i = 2; i <= totalRow; i++) {
            row = sheet.getRow(i);//获取单行数据
            String parentAreaName = row.getCell(0) == null ? "" : row.getCell(0).toString();
            String childAreaName = row.getCell(1) == null ? "" : row.getCell(1).toString();
            String remark = row.getCell(2) == null ? "" : row.getCell(2).toString();
            //处理Excel空行
            if ("".equals(parentAreaName) && "".equals(childAreaName)) {
                continue;
            }
            List<String> areaList=new ArrayList<>();
            areaList.add(parentAreaName);
            areaList.add(childAreaName);
            areaList.add(remark);
            String errorMsg=checkAreaIsValid(areaList);
            if(!StringUtil.isEmpty(errorMsg)){
                getFailedList(parentAreaName,childAreaName,remark,errorMsg,failedList);
                resultMap.put("failedList",failedList);
                continue;
            }
            /**
             * 查询校区是否存在，如果存在，则地区可能部分更新、部分新增；否则新增数据
             */
            Map<String, String> childArea=new HashMap<>();
            for(Map<String,String> areas:allAreaList) {
                if(areas.get("areaName").equals(childAreaName)) {
                    if(areas.get("areaParentName").equals(parentAreaName))
                    {
                        childArea.putAll(areas);
                        break;
                    }
                    else
                    {
                        getFailedList(parentAreaName,childAreaName,remark,"地点名称重复，插入失败",failedList);
                        resultMap.put("failedList",failedList);
                        childArea.put("error","error");
                        continue;
                    }
                }
            }
            if(childArea!=null && !childArea.isEmpty()){
                if(!childArea.containsKey("error")) {
                    AREA_ID = Integer.parseInt(String.valueOf(childArea.get("areaId")));
                }else{
                    continue;
                }
            }else{
                AREA_ID=(int)idMap.get("maxAreaId")+1;
                idMap.put("maxAreaId",AREA_ID);
            }
            //i=2,学校地点信息
            if(i==2) {
                SCHOOL_ZONE_ID=childArea.get("areaId")==null?null:String.valueOf(childArea.get("areaId"));
                if(rootDmArea!=null && parentAreaName.equals(rootDmArea.getAreaName())) {
                    dmAreaMap.put(rootDmArea.getAreaCode(), rootDmArea);
                    getChildAreaData(AREA_ID,childAreaName, remark, rootDmArea, dmAreaMap);
                }else{
                    return new Result(-4,"学校信息设置错误！");
                }
            }else{
                Iterator iterator= dmAreaMap.keySet().iterator();
                boolean checkArea=false;
                while (iterator.hasNext()){
                    String key= (String) iterator.next();
                    DMArea areaEntry=dmAreaMap.get(key);
                    if(parentAreaName.equals(areaEntry.getAreaName())){
                        getChildAreaData(AREA_ID,childAreaName, remark, areaEntry, dmAreaMap);
                        checkArea=true;
                        break;
                    }else{
                        continue;
                    }
                }
                if(!checkArea){
                    getFailedList(parentAreaName,childAreaName,remark,"地点设置错误，插入失败",failedList);
                    resultMap.put("failedList",failedList);
                    childArea.put("error","error");
                }
            }

        }
        if(dmAreaMap!=null && !dmAreaMap.isEmpty()){
            resultMap= addBatchAreas(dmAreaMap,resultMap);
        }
        Result result=new Result(Result.Code.Success.value());
        result.setResult(resultMap);
        return result;
    }

    private List<Map<String,String>> getAllAreas() {
        List<Map<String,String>> areaList=areaDao.getAllAreas();
        for(Map<String,String> allArea:areaList){
            Map<String,String> area = areaDao.getAreaById(allArea.get("areaParentId"));
            if(area!=null && !area.isEmpty()) {
                allArea.put("areaParentName", area.get("areaName"));
            }else{
                allArea.put("areaParentName","");
            }
        }
        return areaList;
    }

    private List<DMArea> getFailedList(String parentAreaName,String childAreaName,String remark,String errorMsg,
                                       List<DMArea> failedList) {
        DMArea area=new DMArea();
        area.setAreaParentName(parentAreaName);
        area.setAreaName(childAreaName);
        area.setAreaRemark(remark);
        area.setErrorMsg(errorMsg);
        failedList.add(area);
        return failedList;
    }

    /**
     * 验证地点模板是否正确
     * @param row
     * @return
     */
    private boolean checkExcelIsTrue(Row row) {
        String parentAreaName = row.getCell(0).toString();
        String childAreaName = row.getCell(1).toString();
        String remark = row.getCell(2).toString();
        if(EXCEL_PARENT_AREA_TITLE.equals(parentAreaName) && EXCEL_CHILD_AREA_TITLE.equals(childAreaName)
                && EXCEL_REMARK_TITLE.equals(remark)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 验证数据是否合法
     * @param areaList  地点信息
     * @return
     */
    private String checkAreaIsValid(List<String> areaList) {
        StringBuffer stringBuffer=new StringBuffer();
        for(int i=0;i<areaList.size();i++) {
            if(i==0) {
                if (areaList.get(i).length() > 15) {
                    stringBuffer.append("上级地点名称超出长度限制,");
                }else{
                    if(!ValidatorUtil.matchRegexContent(areaList.get(i))) {
                        stringBuffer.append("上级地点名称格式错误，包含特殊字符串,");
                    }
                }
            }else if(i==1){
                if (areaList.get(i).length() > 15) {
                    stringBuffer.append("地点名称超出长度限制,");
                }else{
                    if(!ValidatorUtil.matchRegexContent(areaList.get(i))) {
                        stringBuffer.append("地点名称格式错误，包含特殊字符串,");
                    }
                }
            }else{
                if (areaList.get(i).length() > 50) {
                    stringBuffer.append("备注超出长度限制,");
                }else{
                    if(!StringUtil.isEmpty(areaList.get(i)) && !ValidatorUtil.matchRegexContent(areaList.get(i))){
                        stringBuffer.append("备注格式错误，包含特殊字符串,");
                    }
                }
            }
        }
        String result=stringBuffer.toString();
        if(result.endsWith(",")){
            result=result.substring(0,result.lastIndexOf(","));
        }
        return result;
    }

    /**
     * 批量保存地点信息
     * @param dmAreaMap    地点数据
     * @author caoqian
     * @date 2018/11/27
     * @return
     */
    private Map<String,Object> addBatchAreas(Map<String,DMArea> dmAreaMap,Map<String,Object> resultMap){
        List<Map<String,String>> areaAllList=new ArrayList<>();
        List<String> areaIdsList = new ArrayList<>();
        List<String> areaNamesList = new ArrayList<>();
        if(!StringUtil.isEmpty(SCHOOL_ZONE_ID)) {
            areaAllList.add(areaDao.getAreaById(SCHOOL_ZONE_ID));
            areaAllList = getAllAreaByParentId(SCHOOL_ZONE_ID, areaAllList);
        }else{
            areaAllList=areaDao.getAllAreaIds();
        }
        for (Map<String, String> area : areaAllList) {
            areaIdsList.add(String.valueOf(area.get("areaId")));
        }
        Iterator iterator=dmAreaMap.keySet().iterator();
        List<DMArea> list=new ArrayList<>();
        List<DMArea> failedList=new ArrayList<>();
        while (iterator.hasNext()){
            DMArea area=dmAreaMap.get(iterator.next());
            if(area.getAreaId()!=1 && !areaIdsList.contains(String.valueOf(area.getAreaId()))) {
                if(!areaNamesList.contains(area.getAreaName())) {
                    //地点去重
                    areaNamesList.add(area.getAreaName());
                    list.add(area);
                }else{
                    area.setErrorMsg("地点名称重复");
                    failedList.add(area);
                }
            }
        }
        boolean addAreaResult=false;
        if(!list.isEmpty()){
            //保存地点信息
            addAreaResult= areaDao.addAreas(list);
            if(!exitsRootUserRArea()) {
                areaDao.addUser2Area();
            }
            if(addAreaResult){
                logger.info("批量保存地点信息成功!");
            }else{
                logger.error("批量保存地点信息失败!");
            }
        }
        if(resultMap.containsKey("failedList")){
            List<DMArea> failedAreaList=(List<DMArea>)resultMap.get("failedList");
            failedAreaList.addAll(failedList);
            resultMap.put("failedList",failedAreaList);
        }else{
            resultMap.put("failedList",failedList);
        }
        resultMap.put("addBatchArea",addAreaResult);
        return resultMap;
    }

    private boolean exitsRootUserRArea() {
        int exit=areaDao.exitsRootUserRArea();
        boolean re_value=false;
        if(exit>0){
            re_value=true;
        }else{
            re_value = false;
        }
        return re_value;
    }

    private List<Map<String,String>> getAllAreaByParentId(String parentAreaId, List<Map<String, String>> areaList) {
        List<Map<String,String>>  childAreaList=areaDao.getAllAreaByParentId(parentAreaId);
        if(childAreaList!=null && !childAreaList.isEmpty()){
            areaList.addAll(childAreaList);
            for(Map<String,String> area:childAreaList){
                String parentAreaId1=String.valueOf(area.get("areaId"));
                getAllAreaByParentId(parentAreaId1,areaList);
            }
        }
        return areaList;
    }

    /**
     * 封装地点信息
     * @param childAreaName    子级地点名称
     * @param remark           子级地点备注
     * @param parentDmArea     父级地点
     * @param dmAreaMap        保存地点信息
     * @author caoqian
     * @date 2018/11/27
     * @return
     */
    private Map<String,DMArea> getChildAreaData(int areaId,String childAreaName,String remark,DMArea parentDmArea,Map<String,DMArea> dmAreaMap){
        //子级地点
        DMArea childDmArea=new DMArea();
        childDmArea.setAreaId(areaId);
        childDmArea.setAreaName(childAreaName);
        childDmArea.setAreaUuid(UUIDUtil.getUUID());
        childDmArea.setAreaParentId(String.valueOf(parentDmArea.getAreaId()));
        childDmArea.setAreaParentName(parentDmArea.getAreaName());
        int level=parentDmArea.getAreaLevel()+1;
        childDmArea.setAreaLevel(level);
        childDmArea.setAreaType(TYPE_AREA_UNDER_SCHOOL);
        childDmArea.setAreaRemark(remark);
        //判断父节点是否有作息策略
        if (parentDmArea.getScheduleId()!=0) {
            childDmArea.setScheduleId(parentDmArea.getScheduleId());
        }
        String areaCode= SerialNumUtil.getNumber("AR", MAX_AREA_CODE);
        MAX_AREA_CODE=areaCode;
        childDmArea.setAreaCode(areaCode);
        String parentPath = parentDmArea.getAreaParentPath() + parentDmArea.getAreaId() + "/";
        childDmArea.setAreaParentPath(parentPath);
        dmAreaMap.put(childDmArea.getAreaCode(),childDmArea);
        return dmAreaMap;
    }
}
