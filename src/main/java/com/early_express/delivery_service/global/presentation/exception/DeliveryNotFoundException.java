package com.early_express.delivery_service.global.presentation.exception;

public class DeliveryNotFoundException extends GlobalException{

    public DeliveryNotFoundException(String message) {
        super(GlobalErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
