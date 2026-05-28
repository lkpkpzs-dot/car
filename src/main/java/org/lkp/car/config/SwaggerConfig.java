package org.lkp.car.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//配置了 Knife4j 接口文档生成，访问路径通常为 http://localhost:8080/doc.html 。
/**
 * Knife4j/Swagger2 接口文档配置类
 * 启动项目后访问：http://localhost:8080/doc.html
 */
@Configuration
@EnableSwagger2WebMvc
@EnableKnife4j
public class SwaggerConfig {

    /**
     * 创建 API 接口选择器
     */
    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) // 设置文档基本信息
                .select()
                // 指定扫描 Controller 的包路径
                .apis(RequestHandlerSelectors.basePackage("org.lkp.car.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 配置文档基本信息（标题、描述、版本等）
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("车辆管理系统 API 文档")
                .description("车辆管理系统接口文档")
                .contact(new Contact("lkp", "", ""))
                .version("1.0")
                .build();
    }
}
