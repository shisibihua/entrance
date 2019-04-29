package com.honghe.entrance.init;

import com.honghe.entrance.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化入口数据
 * @author caoqian
 * @date 20190329
 */

@Component
public class InitEntranceData implements CommandLineRunner {

    @Autowired
    private InitService init;

    @Override
    public void run(String... strings) throws Exception {
        init.initModuleData();
    }
}
