package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.SysUser;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制层
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Api(tags = "文件上传接口")
public class FileUploadController {

    @Value("${file.upload-path}")
    private String uploadPath;
    @Value("${app.base-url}")
    private String baseUrl;

    // 允许上传的文件类型
    private static final Set<String> ALLOWED_FILE_TYPES = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    
    // 最大文件大小: 10MB (与配置一致)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    /**
     * 图片上传接口
     */
    @PostMapping("/upload")
    @ApiOperation("图片上传")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 1. 验证用户登录
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        // 2. 验证文件是否为空
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 3. 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }

        // 4. 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件格式不合法");
        }
        
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_FILE_TYPES.contains(suffix)) {
            return Result.error("只支持上传图片文件: jpg, jpeg, png, gif, bmp, webp");
        }

        // 5. 确保上传目录存在
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 6. 生成新文件名 (防止重复)
        String newFilename = UUID.randomUUID().toString() + suffix;

        // 7. 保存文件到本地
        try {
            File dest = new File(directory.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);
            log.info("文件上传成功: {}, 用户ID: {}", dest.getAbsolutePath(), currentUser.getUserId());

            // 8. 返回可访问的 URL
            String fileUrl = baseUrl + "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件保存失败: " + e.getMessage());
        }
    }
}
