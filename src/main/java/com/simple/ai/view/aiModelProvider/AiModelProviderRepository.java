package com.simple.ai.view.aiModelProvider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型供应商数据访问层。
 *
 * @author qty
 */
@Mapper
public interface AiModelProviderRepository extends BaseMapper<AiModelProvider> {
}
