package com.honghe.entrance.service.impl;

import com.honghe.entrance.common.pojo.model.Result;
import com.honghe.entrance.dao.ConfigDao;
import com.honghe.entrance.service.EntranceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EntranceConfigServiceImpl implements EntranceConfigService {
    @Autowired
    private ConfigDao configDao;
    @Override
    public Result getEntranceConfig() {
        Map<String,String> map=configDao.getEntranceConfig();
        Result result=new Result(Result.Code.Success.value());
        if(map!=null && !map.isEmpty() && map.containsKey("source")){
            int resultValue=Integer.parseInt(String.valueOf(map.get("source")));
            result.setResult(resultValue);
        }else{
            //默认为云端验证
            result.setResult(-1);
        }
        return result;
    }
}
