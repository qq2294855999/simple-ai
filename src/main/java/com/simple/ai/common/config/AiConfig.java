package com.simple.ai.common.config;

import com.simple.common.auth.client.common.config.AbsClientAuthConfig;
import com.simple.common.auth.client.common.entity.auth.ClientAuthInfo;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA
 *
 * @author qty
 */
@Component
public class AiConfig extends AbsClientAuthConfig {

    @Override
    @SneakyThrows
    protected void configure(ClientAuthInfo clientAuthInfo) {
        clientAuthInfo.anyClient().openLogin().openAuthentication();
    }

}
