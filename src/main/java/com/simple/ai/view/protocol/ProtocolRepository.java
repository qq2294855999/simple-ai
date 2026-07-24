package com.simple.ai.view.protocol;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.protocol.Protocol;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行器协议(agent_protocol)数据访问层。
 *
 * @author qty
 */
@Mapper
public interface ProtocolRepository extends BaseMapper<Protocol> {
}