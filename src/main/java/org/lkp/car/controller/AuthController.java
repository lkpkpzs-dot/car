package org.lkp.car.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.lkp.car.common.JwtUtils;
import org.lkp.car.common.Result;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.LoginRequest;
import org.lkp.car.dto.LoginResponse;
import org.lkp.car.dto.MyEnterpriseStatusResponse;
import org.lkp.car.dto.RefreshTokenRequest;
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
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(loginRequest.getCode());
            String openid = session.getOpenid();
            
            SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getOpenid, openid));
            
            if (user == null) {
                user = new SysUser();
                user.setOpenid(openid);
                user.setRoleType(RoleEnum.CITIZEN_CODE);
                sysUserService.save(user);
                log.info("新用户注册: openid={}", openid);
            }
            
            String accessToken = jwtUtils.createAccessToken(user.getUserId());
            String refreshToken = jwtUtils.createRefreshToken(user.getUserId());
            
            LoginResponse response = new LoginResponse();
            response.setToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setUser(user);

            MyEnterpriseStatusResponse status = enterpriseInfoService.getMyStatus(user.getUserId());
            response.setEnterpriseName(status.getEnterpriseName());
            response.setQualificationStatus(status.getQualificationStatus());

            return Result.success(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新 Token")
    public Result<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            if (!jwtUtils.validateToken(refreshToken)) {
                return Result.error(401, "刷新令牌已过期，请重新登录");
            }
            
            if (!jwtUtils.isRefreshToken(refreshToken)) {
                return Result.error(400, "无效的刷新令牌");
            }
            
            Long userId = jwtUtils.getUserId(refreshToken);
            SysUser user = sysUserService.getById(userId);
            
            if (user == null) {
                return Result.error(401, "用户不存在，请重新登录");
            }
            
            String newAccessToken = jwtUtils.createAccessToken(userId);
            String newRefreshToken = jwtUtils.createRefreshToken(userId);
            
            LoginResponse response = new LoginResponse();
            response.setToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setUser(user);

            MyEnterpriseStatusResponse status = enterpriseInfoService.getMyStatus(user.getUserId());
            response.setEnterpriseName(status.getEnterpriseName());
            response.setQualificationStatus(status.getQualificationStatus());

            return Result.success(response);
        } catch (Exception e) {
            log.error("刷新 Token 失败", e);
            return Result.error("刷新 Token 失败: " + e.getMessage());
        }
    }
}
