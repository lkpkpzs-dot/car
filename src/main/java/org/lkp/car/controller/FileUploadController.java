package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.lkp.car.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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

    /**
     * 图片上传接口
     */
    @PostMapping("/upload")
    @ApiOperation("图片上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 1. 确保上传目录存在
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. 生成新文件名 (防止重复)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件到本地
        try {
            File dest = new File(directory.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);
            log.info("文件上传成功: {}", dest.getAbsolutePath());

            // 4. 返回可访问的 URL
            // 拼接方式: 协议://域名:端口/访问前缀/文件名
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            
//            String fileUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + "/uploads/" + newFilename;

            String fileUrl = baseUrl + "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件保存失败: " + e.getMessage());
        }
    }
}
