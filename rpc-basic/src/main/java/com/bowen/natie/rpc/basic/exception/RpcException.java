package com.bowen.natie.rpc.basic.exception;

import java.lang.reflect.Field;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public class RpcException extends Exception{

    private static final long serialVersionUID = 1L;


    public static final String CALLEE_UNKNOWN_EXCEPTION = "RPC_CALLEE_UNKNOWN_EXCEPTION";
    public static final String GENERAL_EXCEPTION = "GENERAL_EXCEPTION";

    protected String returnCode;
    protected String returnMessage;

    protected String errorGroup = RpcErrorGroup.DEFAULT;

    /** 是否熔断 TODO*/
    protected boolean circuitBroken;
    /** 是否重试 TODO*/
    protected boolean retry;

    protected String errorLevel;
    protected String errorLocation;

    public RpcException() {
        super();
        this.returnCode = GENERAL_EXCEPTION;
    }

    /**
     * The returnCode will be errorCode.name.
     * <p>
     * The returnMessage will be errorCode.returnMessage.
     * <p>
     * The errorGroup will be the errorCode.errorGroup.
     * <p>
     * The circuitBroken will be the errorCode.circuitBroken.
     */
    public RpcException(IErrorCode errorCode) throws RpcException {
        super();
        initReturnInfo(errorCode);
    }

    /** 通过注解@的对象来初始化OspException对象 */
    private void initReturnInfo(IErrorCode errorCode) throws RpcException {
        ErrorCode errorCodeAnnotationInstance = getErrorCodeAnnotationInstance(errorCode);
        this.returnCode = errorCodeAnnotationInstance.name();
        this.returnMessage = errorCodeAnnotationInstance.description();
        this.errorGroup = errorCodeAnnotationInstance.errorGroup();
        this.circuitBroken = errorCodeAnnotationInstance.circuitBroken();
        this.retry = errorCodeAnnotationInstance.retry();
    }

    /**
     * The returnCode will be errorCode.name.
     * <p>
     * The returnMessage will be message of parameters.
     * <p>
     * The errorGroup will be the errorCode.errorGroup.
     * <p>
     * The circuitBroken will be the errorCode.circuitBroken.
     */
    public RpcException(IErrorCode errorCode, String message) throws RpcException {
        super(message);
        initReturnInfo(errorCode);
        this.returnMessage = message;
    }

    /**
     * The returnCode will be errorCode.name.
     * <p>
     * The returnMessage will be errorCode.returnMessage.
     * <p>
     * The errorGroup will be the errorCode.errorGroup.
     * <p>
     * The circuitBroken will be the errorCode.circuitBroken.
     */
    public RpcException(IErrorCode errorCode, Throwable cause) throws RpcException {
        super(cause);
        initReturnInfo(errorCode);
    }

    /**
     * The returnCode will be errorCode.name.
     * <p>
     * The returnMessage will be message of parameters.
     * <p>
     * The errorGroup will be the errorCode.errorGroup.
     * <p>
     * The circuitBroken will be the errorCode.circuitBroken.
     */
    public RpcException(IErrorCode errorCode, String message, Throwable cause) throws RpcException {
        super(cause);
        initReturnInfo(errorCode);
        this.returnMessage = message;
    }

    /**
     * @deprecated {@link #RpcException(IErrorCode)} or {@link #RpcException(IErrorCode, String)} is recommend
     */
    @Deprecated
    public RpcException(String returnCode, String returnMessage) {
        super(returnMessage);
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
    }


    /**
     * 如果异常是OSPException进行数据复制. 否则returnCode为GENERAL_EXCEPTION,returnMessage是exception.toString();
     */
    public RpcException(Throwable cause) {
        super(cause);
        if (cause instanceof RpcException) {
            copyFrom((RpcException) cause);
        } else {
            this.returnCode = GENERAL_EXCEPTION;
            this.returnMessage = cause.toString();
        }
    }

    private void copyFrom(RpcException tmp) {
        this.returnCode = tmp.getReturnCode();
        this.returnMessage = tmp.getReturnMessage();
        this.errorGroup = tmp.getErrorGroup();
        this.errorLevel = tmp.getErrorLevel();
        this.errorLocation = tmp.getErrorLocation();
        this.circuitBroken = tmp.isCircuitBroken();
        this.retry = tmp.isRetry();
    }

    /**
     * The returnCode will be {@code GENERAL_EXCEPTION}
     */
    public RpcException(String returnMessage, Throwable cause) {
        super(returnMessage, cause);
        this.returnCode = GENERAL_EXCEPTION;
        this.returnMessage = returnMessage;
    }

    private static ErrorCode getErrorCodeAnnotationInstance(IErrorCode errorCode) throws RpcException {
        try {
            Field[] fields = errorCode.getClass().getFields();
            Field targetField = null;
            for (Field field : fields) {
                if (field.get(errorCode).equals(errorCode)) {
                    targetField = field;
                    break;
                }
            }

            if(targetField==null){
                throw new IllegalArgumentException("Unknow errorCode "+ errorCode);
            }
            return targetField.getAnnotation(ErrorCode.class);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public String getMessage() {
        return new StringBuilder(100).append("[errorGroup:").append(errorGroup).append("],[circuitBroken:")
                .append(circuitBroken).append("],[retry:").append(retry).append("],[returnCode:").append(returnCode)
                .append("],[returnMessage:").append(returnMessage).append("]").toString();
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String retMessage) {
        this.returnMessage = retMessage;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String retCode) {
        this.returnCode = retCode;
    }

    public String getErrorGroup() {
        return errorGroup;
    }

    public void setErrorGroup(String errorGroup) {
        this.errorGroup = errorGroup;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getErrorLocation() {
        return errorLocation;
    }

    public void setErrorLocation(String errorLocation) {
        this.errorLocation = errorLocation;
    }

    public boolean isCircuitBroken() {
        return circuitBroken;
    }

    public void setCircuitBroken(boolean circuitBroken) {
        this.circuitBroken = circuitBroken;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public boolean is200() {
        try {
            int code = Integer.parseInt(getErrorGroup());
            return code >= 200 && code <= 299;
        } catch (Exception e) {	// NOSONAR
        }
        return false;
    }
}

