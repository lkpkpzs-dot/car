-- ============================================================
-- 车辆牌照表创建脚本
-- 数据库: car_admin
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 车辆牌照表 vehicle_plate
-- 记录车辆的所有牌照历史和当前状态
-- ----------------------------
CREATE TABLE IF NOT EXISTS vehicle_plate (
    plate_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '牌照记录ID',
    vin VARCHAR(50) NOT NULL COMMENT '车辆识别代码（车架号）',
    enterprise_id BIGINT NOT NULL COMMENT '所属企业ID',
    application_id BIGINT COMMENT '关联的道路申请ID',
    vehicle_info_id BIGINT COMMENT '关联的查验记录ID',
    
    plate_type INT NOT NULL COMMENT '牌照类型：1-道路测试，2-示范应用，3-应用试点',
    plate_number VARCHAR(20) NOT NULL COMMENT '车牌号',
    
    issue_date DATE COMMENT '发牌日期',
    expiry_date DATE COMMENT '到期日期',
    
    issuer_id BIGINT COMMENT '发牌民警ID',
    issue_comment VARCHAR(500) COMMENT '发牌备注',
    
    status INT DEFAULT 1 COMMENT '状态：1-有效，2-已过期，3-已注销',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    INDEX idx_vin (vin),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_plate_number (plate_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆牌照表';

SET FOREIGN_KEY_CHECKS = 1;

-- 自增起点
ALTER TABLE vehicle_plate AUTO_INCREMENT = 10001;
