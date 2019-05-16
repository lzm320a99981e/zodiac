package com.github.lzm320a99981e.cloud.shared.endpoint;

import com.alibaba.fastjson.JSON;
import com.github.lzm320a99981e.component.token.Token;
import com.github.lzm320a99981e.component.token.TokenManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = {"99-示例接口"})
@RestController
public class DemoEndpoint {
    @Autowired
    private TokenManager tokenManager;

    @ApiOperation("用户登录")
    @PostMapping("user/login")
    public Token userLogin(@RequestBody Map<String, Object> payload) {
        return tokenManager.generate(payload);
    }

    @ApiOperation("用户信息")
    @PostMapping("user/info")
    public Map<String, Object> userInfo(@RequestBody UserInfoRequest request) {
        return tokenManager.verify(request.getAccessToken());
    }

    @ApiOperation("get测试")
    @GetMapping("/api/reg")
    @ResponseBody
    public Map<String, Object> reg(UserRegRequest request) {
        return JSON.parseObject(JSON.toJSONString(request));
    }

    @Data
    public static class UserRegRequest {
        private String username;
        private String password;
    }

    @Data
    public static class UserInfoRequest {
        private String accessToken;
    }
}