package com.simple.ai.view.aiModel;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.aiModel.AiModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AiModelRepository extends BaseMapper<AiModel> {
}
