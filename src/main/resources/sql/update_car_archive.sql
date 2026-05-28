-- 为 car_archive 表添加缺失字段
ALTER TABLE car_archive ADD COLUMN application_id BIGINT COMMENT '关联的道路申请ID' AFTER vin;
ALTER TABLE car_archive ADD COLUMN vehicle_info_id BIGINT COMMENT '关联的查验记录ID' AFTER application_id;
ALTER TABLE car_archive ADD COLUMN vehicle_brand VARCHAR(100) COMMENT '车辆品牌' AFTER enterprise_id;
ALTER TABLE car_archive ADD COLUMN vehicle_model VARCHAR(100) COMMENT '车辆型号' AFTER vehicle_brand;

-- 创建索引
ALTER TABLE car_archive ADD INDEX idx_application_id (application_id);
ALTER TABLE car_archive ADD INDEX idx_vehicle_info_id (vehicle_info_id);
