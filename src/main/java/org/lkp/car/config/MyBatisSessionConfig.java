package org.lkp.car.config;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 确保 SqlSessionTemplate 为单例，复用 SqlSessionFactory，避免每次请求重复创建 Factory。
 * Spring 事务内同一 SqlSession 会被绑定到当前线程，配合 REUSE 执行器减少 Statement 重建。
 */
@Configuration
public class MyBatisSessionConfig {

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.REUSE);
    }
}
