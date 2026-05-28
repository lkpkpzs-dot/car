-- ============================================================
-- 安全员监管表创建脚本
-- 数据库: car_admin
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 安全员资质监管表 safety_officer
-- ----------------------------
CREATE TABLE IF NOT EXISTS safety_officer (
    officer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '安全员ID',
    enterprise_id BIGINT NOT NULL COMMENT '所属企业ID',
    applicant_id BIGINT NOT NULL COMMENT '提交人用户ID',

    officer_name VARCHAR(50) NOT NULL COMMENT '安全员姓名',
    id_card_no VARCHAR(32) NOT NULL COMMENT '身份证号',
    phone VARCHAR(30) COMMENT '联系电话',
    age INT NOT NULL COMMENT '年龄',
    license_type VARCHAR(10) NOT NULL COMMENT '驾驶证类型：C1、C2或A、B类',
    driver_license_no VARCHAR(50) COMMENT '驾驶证号',
    first_license_date DATE NOT NULL COMMENT '初次领证日期',

    no_full_score_record TINYINT DEFAULT 0 COMMENT '最近连续三个记分周期无记满12分记录：0-否，1-是',
    no_major_accident_record TINYINT DEFAULT 0 COMMENT '无致人死亡或重伤的交通责任事故记录：0-否，1-是',
    no_dui_record TINYINT DEFAULT 0 COMMENT '无酒后或醉酒驾驶机动车记录：0-否，1-是',
    no_crime_record TINYINT DEFAULT 0 COMMENT '无犯罪记录：0-否，1-是',
    healthy TINYINT DEFAULT 0 COMMENT '身心健康且无危及行车安全疾病史：0-否，1-是',
    no_alcohol_drug_record TINYINT DEFAULT 0 COMMENT '无酗酒、吸毒行为记录：0-否，1-是',

    id_card_url VARCHAR(500) COMMENT '身份证材料URL',
    driver_license_url VARCHAR(500) COMMENT '机动车驾驶证材料URL',
    health_certificate_url VARCHAR(500) COMMENT '机动车驾驶人身体条件证明URL（3个月内）',
    no_crime_certificate_url VARCHAR(500) COMMENT '无犯罪记录证明URL',
    no_violation_accident_certificate_url VARCHAR(500) COMMENT '无相应交通违法及事故证明URL',
    no_alcohol_drug_certificate_url VARCHAR(500) COMMENT '无酗酒、吸毒记录证明URL',

    status INT DEFAULT 0 COMMENT '资质状态：0-待审核，1-有效，2-驳回，3-暂停，4-取消',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    review_comment VARCHAR(500) COMMENT '审核/驳回意见',
    suspend_start_date DATE COMMENT '暂停开始时间',
    suspend_end_date DATE COMMENT '暂停结束时间',
    penalty_reason VARCHAR(500) COMMENT '处分原因',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_id_card_no (id_card_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全员资质监管表';

-- ----------------------------
-- 安全员事故处分记录表 safety_officer_penalty
-- ----------------------------
CREATE TABLE IF NOT EXISTS safety_officer_penalty (
    penalty_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '处分记录ID',
    officer_id BIGINT NOT NULL COMMENT '安全员ID',
    enterprise_id BIGINT NOT NULL COMMENT '所属企业ID',
    accident_date DATE COMMENT '事故日期',
    liability_level INT NOT NULL COMMENT '责任等级：1-无责，2-次责，3-同等责任，4-主责，5-全责',
    casualty_type INT NOT NULL COMMENT '伤亡情况：0-无伤亡，1-受伤，2-死亡',
    penalty_type INT NOT NULL COMMENT '处分类型：1-暂停3个月，2-暂停半年，3-取消资格',
    start_date DATE COMMENT '处分开始日期',
    end_date DATE COMMENT '处分结束日期',
    handler_id BIGINT COMMENT '处理民警ID',
    reason VARCHAR(500) COMMENT '事故及处理说明',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    INDEX idx_officer_id (officer_id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_penalty_type (penalty_type),
    INDEX idx_casualty_type (casualty_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全员事故处分记录表';

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE safety_officer AUTO_INCREMENT = 10001;
ALTER TABLE safety_officer_penalty AUTO_INCREMENT = 10001;
