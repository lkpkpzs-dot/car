-- ============================================================
-- 车管系统测试数据（清空表后执行）
-- 数据库: car_admin
-- 流程: 提交留痕(action_type=1) → 审核留痕(2/3) → /audit/list 聚合
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户 sys_user
-- role_type: 1-交警 2-企业代办 3-市民
-- ----------------------------
INSERT INTO sys_user (user_id, openid, role_type, real_name, phone, auth_enterprise_id, create_time, update_time, is_deleted) VALUES
(30002, 'openid_police_zhang', 1, '张警官', '13800003002', NULL, '2026-05-25 09:00:00', '2026-05-25 09:00:00', 0),
(30003, 'openid_police_li',    1, '李警官', '13800003003', NULL, '2026-05-25 09:00:00', '2026-05-25 09:00:00', 0),
(88001, 'openid_ent_wang',     2, '王经办', '13900008801', 10001, '2026-05-25 09:00:00', '2026-05-25 09:00:00', 0),
(88002, 'openid_ent_zhao',     2, '赵经办', '13900008802', 10002, '2026-05-25 09:00:00', '2026-05-25 09:00:00', 0);

-- ----------------------------
-- 2. 企业 enterprise_info
-- audit_status: 0-待审 1-通过 2-驳回
-- ----------------------------
INSERT INTO enterprise_info (enterprise_id, enterprise_name, credit_code, legal_person, contact_phone, business_license_url, audit_status, create_time, update_time, is_deleted) VALUES
(10001, '上海智行科技有限公司',   '91310000MA1K001001', '王明', '021-66001001', '/oss/license/10001.jpg', 0, '2026-05-25 10:00:00', '2026-05-25 10:00:00', 0),
(10002, '上海海纳百川商贸有限公司', '91310000772233445X', '李海', '021-66001002', '/oss/license/10002.jpg', 0, '2026-05-25 11:00:00', '2026-05-25 11:00:00', 0),
(10003, '北京星辰创新科技有限公司', '91110108MA01STAR03', '赵星', '010-66001003', '/oss/license/10003.jpg', 1, '2026-05-24 15:00:00', '2026-05-25 15:12:00', 0);

-- ----------------------------
-- 3. 车辆档案 car_archive（号牌申请关联 VIN）
-- ----------------------------
INSERT INTO car_archive (vin, enterprise_id, current_plate_type, create_time, update_time, is_deleted) VALUES
('LHGCM82633A000001', 10003, NULL, '2026-05-25 08:00:00', '2026-05-25 08:00:00', 0),
('LHGCM82633A000002', 10003, NULL, '2026-05-25 08:00:00', '2026-05-25 08:00:00', 0);

-- ----------------------------
-- 4. 号牌申请 license_application
-- flow_status: 0-待提交 1-初审 2-终审 3-待查验 4-已发牌 5-驳回
-- ----------------------------
INSERT INTO license_application (apply_id, vin, enterprise_id, apply_plate_type, flow_status, materials_json, route_plan, reviewer_id, audit_comment, create_time, update_time, is_deleted) VALUES
(20001, 'LHGCM82633A000001', 10003, 1, 1, '{"license":"已上传"}', '浦东新区测试路线A', NULL, NULL, '2026-05-25 12:30:00', '2026-05-25 12:30:00', 0),
(20002, 'LHGCM82633A000002', 10003, 2, 0, NULL, NULL, NULL, NULL, '2026-05-25 13:00:00', '2026-05-25 13:00:00', 0);

-- ----------------------------
-- 5. 审批留痕 approval_record
-- business_type: 1-号牌 2-企业资质
-- action_type: 1-提交 2-通过 3-驳回 4-转交
--
-- 【待审核】最新留痕为「提交」的业务:
--   10001 企业、10002 企业、20001 号牌
-- 【已办】民警 30002 审过且最新留痕为「通过」:
--   10003 企业（先驳回再通过）
-- ----------------------------
INSERT INTO approval_record (record_id, apply_id, business_type, node_name, reviewer_id, action_type, comment, snapshot_json, create_time) VALUES
-- 企业 10001：仅提交 → 待审核
(50001, 10001, 2, '提交申请', 88001, 1, '企业提交资质认证申请材料',
 '{"enterpriseName":"上海智行科技有限公司","creditCode":"91310000MA1K001001"}',
 '2026-05-25 10:05:00'),

-- 企业 10002：仅提交 → 待审核
(50002, 10002, 2, '提交申请', 88002, 1, '企业提交资质认证申请材料',
 '{"enterpriseName":"上海海纳百川商贸有限公司","creditCode":"91310000772233445X"}',
 '2026-05-25 11:05:00'),

-- 号牌 20001：仅提交 → 待审核
(50003, 20001, 1, '提交申请', 88001, 1, '企业提交上牌申请',
 '{"vin":"LHGCM82633A000001","applyPlateType":1}',
 '2026-05-25 12:35:00'),

-- 企业 10003：提交 → 驳回 → 通过（最新=通过）→ 不进待审核，30002 的已办
(50004, 10003, 2, '提交申请', 88001, 1, '企业提交资质认证申请材料',
 '{"enterpriseName":"北京星辰创新科技有限公司","creditCode":"91110108MA01STAR03"}',
 '2026-05-25 14:00:00'),
(50005, 10003, 2, '民警审核', 30002, 3, '材料不完整，请补充营业执照复印件', NULL,
 '2026-05-25 14:21:48'),
(50006, 10003, 2, '民警审核', 30002, 2, '材料齐全，予以通过', NULL,
 '2026-05-25 15:12:00');

SET FOREIGN_KEY_CHECKS = 1;

-- 自增起点（可选，避免与测试 ID 冲突）
ALTER TABLE sys_user AUTO_INCREMENT = 90001;
ALTER TABLE enterprise_info AUTO_INCREMENT = 10010;
ALTER TABLE license_application AUTO_INCREMENT = 20010;
ALTER TABLE approval_record AUTO_INCREMENT = 50010;
