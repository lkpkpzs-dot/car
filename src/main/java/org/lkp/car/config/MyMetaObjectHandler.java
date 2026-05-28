package org.lkp.car.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
//实现了 MyBatis-Plus 的自动填充功能，自动管理 create_time 和 update_time 。
/**
 * MyBatis-Plus 自动填充功能处理器
 * 用于自动管理数据库中的 create_time 和 update_time 字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 当执行插入操作时，自动为 createTime 和 updateTime 字段填充当前系统时间
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 当执行更新操作时，自动为 updateTime 字段填充当前系统时间
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
