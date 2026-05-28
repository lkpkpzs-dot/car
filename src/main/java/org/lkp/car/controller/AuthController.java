package org.lkp.car.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.lkp.car.common.JwtUtils;
import org.lkp.car.common.Result;
import org.lkp.car.dto.LoginRequest;
import org.lkp.car.dto.LoginResponse;
import org.lkp.car.dto.MyEnterpriseStatusResponse;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.EnterpriseInfoService;
import org.lkp.car.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证授权控制层
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证授权接口")
public class AuthController {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 小程序一键登录
     */
    @PostMapping("/login")
    @ApiOperation("小程序一键登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. 调用微信接口换取 openid
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(loginRequest.getCode());
            String openid = session.getOpenid();
            
            // 2. 数据库查询用户
            SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getOpenid, openid));
            
            // 3. 如果用户不存在，则自动注册
            if (user == null) {
                user = new SysUser();
                user.setOpenid(openid);
                user.setRoleType(3); // 默认设置为普通市民
                sysUserService.save(user);
                log.info("新用户注册: openid={}", openid);
            }
            
            // 4. 生成 Token
            String token = jwtUtils.createToken(user.getUserId());
            
            // 5. 封装返回结果
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUser(user);

            // 6. 补充企业资质信息（复用 Service 逻辑）
            MyEnterpriseStatusResponse status = enterpriseInfoService.getMyStatus(user.getUserId());
            response.setEnterpriseName(status.getEnterpriseName());
            response.setQualificationStatus(status.getQualificationStatus());

            return Result.success(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}
