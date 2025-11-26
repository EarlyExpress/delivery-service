package com.early_express.delivery_service.delivery.infrastructure.client;

import com.early_express.delivery_service.global.config.FeignConfig;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Last Mile Driver Client 설정
 * - 글로벌 FeignConfig 상속 (timeout, retry, logging 등)
 * - ErrorDecoder만 커스텀
 */
public class LastMileDriverClientConfig extends FeignConfig {

    /**
     * Last Mile Driver 전용 에러 디코더
     */
    @Bean
    @Override
    public ErrorDecoder errorDecoder() {
        return new LastMileDriverErrorDecoder();
    }
}