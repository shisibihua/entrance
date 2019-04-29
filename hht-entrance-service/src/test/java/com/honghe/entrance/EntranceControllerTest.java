package com.honghe.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.honghe.entrance.EntranceServiceApplication;
import com.honghe.entrance.common.util.SpringUtil;
import com.honghe.entrance.common.util.UUIDUtil;
import com.honghe.entrance.dao.UserDao;
import com.honghe.entrance.entity.User;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EntranceServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
//按字母名称顺序
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntranceControllerTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private UserDao userDao;
    private MockMvc mockMvc;
    private final static String URL="/service/cloud/httpCommandService?cmd=entrance&cmd_op=";
    @Before
    public void setUp() throws Exception {
        ApplicationContext app= SpringApplication.run(EntranceServiceApplication.class);
        SpringUtil.setApplicationContext(app);
        SpringUtil.scannerBeans();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    }


    @Test
    public void searchEntranceConfig() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"searchEntranceConfig")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) //打印内容
                .andExpect(status().isOk())   //判断接收到的状态是否是200
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));////使用jsonPath解析返回值，判断具体的内容

        assert result.andReturn().getResponse().getContentAsString().contains("-1");

    }

    @Test
    public void userCheck() throws Exception {
        ResultActions result=  mockMvc.perform(get(URL+"userCheck&loginName=superadmin&userPwd=hitevision.123")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("超级管理员");

    }

    @Test
    public void checkUserIsFirstLogin() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"checkUserIsFirstLogin&loginName=superadmin")
                             .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                             .andDo(print())
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("code").exists())
                             .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("false");

    }

    @Test
    public void checkUserIsUpdatePwd() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"checkUserIsUpdatePwd&loginName=superadmin")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("false");

    }

    @Test
    public void userSearch() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"userSearch&userId=1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("superadmin");

    }

    @Test
    public void getMobileCode() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"getMobileCode&mobile=15076268240")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("0");

    }
    @Test
    public void userUpdate() throws Exception {
        User user=new User();
        user.setUserId(2);
        user.setUserRealName("张三");
        user.setUserName("zhangsan");
        user.setUserType("17");
        user.setUserMobile("18897568541");
        user.setRemark("测试");
        mockMvc.perform(get(URL+"userUpdate&userId="+user.getUserId()+"&userRealName="+user.getUserRealName()
                +"&userName="+user.getUserName()+"&userType="+user.getUserType()+"&userMobile="+user.getUserMobile()
                +"&userRemark="+user.getRemark())
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        ResultActions result=mockMvc.perform(get(URL+"userSearch&userId=2")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("张三");

    }
    @Test
    public void userAdd() throws Exception {
        Random random=new Random();
        int randNum=random.nextInt(10);
        System.out.println(randNum);
        User user=new User();
        String userName="lisi"+randNum;
        String userRealName="李四"+ randNum ;
        user.setUserRealName(userRealName);
        user.setUserName(userName);
        user.setUserType("17");
        String mobileNum="1597568599"+randNum;

        user.setUserMobile(mobileNum);
        user.setRemark("测试2");
        mockMvc.perform(get(URL+"userAdd&userRealName="+user.getUserRealName()
                +"&userName="+user.getUserName()+"&userType="+user.getUserType()+"&userMobile="+user.getUserMobile()
                +"&remark="+user.getRemark())
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        Map<String,Object> userInfo = userDao.searchUserByName(mobileNum);
        ResultActions result=mockMvc.perform(get(URL+"userSearch&userId="+userInfo.get("userId"))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains(userRealName);
    }

    @Test
    public void userDelete() throws Exception {
        String userId="9";
        ResultActions result = mockMvc.perform(get(URL+"userDelete&userId="+userId)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("true ");
    }

    @Test
    public void userChangePassword() throws Exception {
        String userId="10";
        String newPassWord="1234567";
         mockMvc.perform(get(URL+"userChangePassword&userId="+userId+"&oldPassword=123456&newPassword="+newPassWord)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        Map<String,String> userInfo=userDao.findUserById(Integer.parseInt(userId));
        ResultActions result = mockMvc.perform(get(URL+"userCheck&loginName="+userInfo.get("userName")+"&userPwd="+newPassWord)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains(userInfo.get("userName"));
    }

    @Test
    public void userList() throws Exception {
        int pageSize=1;
        int pageCount=10 ;
        String userType = "17";
//        String searchKey="";
        int userId=1;
       ResultActions result= mockMvc.perform(get(URL+"userList&userId="+userId+"&userType="+userType+"&pageSize="+pageSize+
                "&pageCount="+pageCount+"&searchKey=").accept(MediaType.APPLICATION_JSON)
               .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        int count = userDao.searchUserCount(String.valueOf(userId),null);
        assert result.andReturn().getResponse().getContentAsString().contains(count+"");
    }

    @Test
    public void getUserByMobile() throws Exception {
        String mobile="15076268240";
        ResultActions result=mockMvc.perform(get(URL+"getUserByMobile&userMobile="+mobile)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
//        System.out.println(result.andReturn().getResponse().getContentAsString());
        assert result.andReturn().getResponse().getContentAsString().contains(mobile);

    }


    @Test
    public void resetPwd() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"resetPwd&userId=7")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
//        System.out.println(result.andReturn().getResponse().getContentAsString());
        assert result.andReturn().getResponse().getContentAsString().contains("true");

    }



    @Test
    public void updatePermission() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"updatePermission&roleId=7&authorityIds=4,7,8,9,10,11,12,13,")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("1");

    }


    @Test
    public void getMarkedPermissions() throws Exception {
        ResultActions result=mockMvc.perform(get(URL+"getMarkedPermissions&roleId=1&ip=127.0.0.1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        System.out.println(result.andReturn().getResponse().getContentAsString());
        assert result.andReturn().getResponse().getContentAsString().contains("巡课");

    }

    @Test
    public void sysGetAllEnableModule() throws Exception {
        mockMvc.perform(get(URL+"sysGetAllEnableModule&userId=1&searchKey=&status=&ip=127.0.0.1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));

    }

    @Test
    public void updateModuleStatus() throws Exception {
        ResultActions result = mockMvc.perform(get(URL+"updateModuleStatus&userSysModuleIds=4&status=1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("true");

    }

    @Test
    public void themeSet() throws Exception {
        ResultActions result = mockMvc.perform(get(URL+"themeSet&user_id=1&themeTitle=保定一中")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("true");

    }

    @Test
    public void resetTheme() throws Exception {
        ResultActions result = mockMvc.perform(get(URL+"resetTheme")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("true");

    }

    @Test
    public void getCurrentThem() throws Exception {
        ResultActions result = mockMvc.perform(get(URL+"getCurrentThem&ip=127.0.0.1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));
        assert result.andReturn().getResponse().getContentAsString().contains("false");

    }
   /*
    @Test
    public void userCheck() throws Exception {
        mockMvc.perform(post("/config/update")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(JSON.toJSONString(liveConfig)))
                .andDo(print()) //打印内容
                .andExpect(status().isOk())   //判断接收到的状态是否是200
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));////使用jsonPath解析返回值，判断具体的内容
        // ResultActions result
        ResultActions result= mockMvc.perform(get("/config/selectById?id=1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) //打印内容
                .andExpect(status().isOk())   //判断接收到的状态是否是200
                .andExpect(jsonPath("code").exists())
                .andExpect(jsonPath("code").value(0));////使用jsonPath解析返回值，判断具体的内容
        assert result.andReturn().getResponse().getContentAsString().contains("腾讯云测试一修改");

    }
*/


}