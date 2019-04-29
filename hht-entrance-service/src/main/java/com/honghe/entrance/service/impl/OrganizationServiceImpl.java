package com.honghe.entrance.service.impl;

import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.dao.AreaDao;
import com.honghe.entrance.dao.OrganizationDao;
import com.honghe.entrance.entity.DMArea;
import com.honghe.entrance.service.OrganizationService;
import jodd.util.StringUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.Map;


@Service
public class OrganizationServiceImpl implements OrganizationService {
    Logger logger= LoggerFactory.getLogger(OrganizationServiceImpl.class);
    @Autowired
    private AreaDao areaDao;
    @Autowired
    private OrganizationDao organizationDao;
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result saveRootOrg(String orgName, String code) {
        if(StringUtil.isEmpty(orgName) || StringUtil.isEmpty(code)){
            return new Result(Result.Code.ParamError.value(),"组织机构名称或学校code不能为空");
        }else{
            Result result=new Result(Result.Code.Success.value());
            boolean resultValue=batchAddRootOrg(orgName,code);
            if(resultValue){
                //组织机构添加成功，修改地点模板学校名称
                updateAreaTemplate();
            }
            result.setResult(resultValue);
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result deleteRootOrg() {
        boolean delCampus=organizationDao.delCampus();
        boolean delArea=areaDao.delArea();
        boolean delUser2Area= areaDao.delUser2Area();
        boolean delPatrolGroup=organizationDao.delGroup();
        boolean resultValue=delCampus && delArea && delPatrolGroup && delUser2Area;
        Result result=new Result(Result.Code.Success.value());
        result.setResult(resultValue);
        return result;
    }

    @Override
    public Map<String, String> searchRootOrg() {
        return organizationDao.searchSchool();
    }

    @Override
    public boolean exitsRootOrg() {
        int campusNum=organizationDao.searchCampusNum();
        if(campusNum>0){
            return true;
        }else {
            return false;
        }
    }

    private boolean batchAddRootOrg(String orgName, String code) {
        try {
            boolean addCampusResult = organizationDao.addCampus(orgName);
            boolean addArea = areaDao.addArea(orgName, code);
            boolean addUser2Area = areaDao.addUser2Area();
            boolean addUser2AreaAdmin = areaDao.addUser2AreaAdmin();
            boolean addGroup = organizationDao.addGroup(orgName);
            boolean result = addCampusResult && addArea && addUser2Area && addUser2AreaAdmin && addGroup;
            return result;
        }catch (Exception e){
            logger.error("添加学校异常",e);
            return false;
        }
    }

    //修改地点模板学校名称
    private void updateAreaTemplate() {
        String areaTemplatePath=System.getProperty("user.dir")+ File.separator+"template"+File.separator+"entrance"+
                File.separator+"area_template.xls";
        File file=new File(areaTemplatePath);
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
            Row row = sheet.getRow(2);

            //获取表格一中 第3行的第1列(根据索引)
            Cell cell = row.getCell(0);
            //赋值，获取学校信息
            DMArea rootDmArea=areaDao.getSchoolInfo();
            String name=rootDmArea.getAreaName();
            cell.setCellValue(name);
            OutputStream newFileStream = new FileOutputStream(areaTemplatePath);
            workbook.write(newFileStream);
//            logger.error("修改地点模板学校名称成功，学校名称为："+name);
        }catch (Exception e){
            logger.error("添加组织机构，读取地点模板异常",e);
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

    }
}
