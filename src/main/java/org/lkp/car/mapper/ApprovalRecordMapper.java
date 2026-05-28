package org.lkp.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lkp.car.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批流转记录 Mapper 接口
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {
}
