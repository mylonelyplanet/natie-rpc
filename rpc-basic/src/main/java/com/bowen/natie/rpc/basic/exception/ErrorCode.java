package com.bowen.natie.rpc.basic.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface ErrorCode {
    String name();

    String description() default "";

    String detail() default "";

    String errorGroup() default RpcErrorGroup.DEFAULT;

    boolean circuitBroken() default false;
    /**是否重试*/
    boolean retry() default false;
}