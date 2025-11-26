package com.early_express.delivery_service.delivery.infrastructure.client;

import com.early_express.delivery_service.delivery.domain.exception.DeliveryErrorCode;
import com.early_express.delivery_service.delivery.domain.exception.DeliveryException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Last Mile Driver Client 에러 디코더
 */
@Slf4j
public class LastMileDriverErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("LastMileDriverClient 에러 - method: {}, status: {}, reason: {}",
                methodKey, response.status(), response.reason());

        return switch (response.status()) {
            case 404 -> new DeliveryException(
                    DeliveryErrorCode.DRIVER_NOT_FOUND,
                    "배송 담당자를 찾을 수 없습니다."
            );
            case 400 -> new DeliveryException(
                    DeliveryErrorCode.DRIVER_ASSIGN_FAILED,
                    "배송 담당자 배정에 실패했습니다."
            );
            case 503 -> new DeliveryException(
                    DeliveryErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                    "배송 담당자 서비스를 사용할 수 없습니다."
            );
            default -> new DeliveryException(
                    DeliveryErrorCode.EXTERNAL_SERVICE_ERROR,
                    "배송 담당자 서비스 오류가 발생했습니다. status: " + response.status()
            );
        };
    }
}