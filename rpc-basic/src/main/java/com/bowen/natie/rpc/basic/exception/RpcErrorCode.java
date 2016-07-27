package com.bowen.natie.rpc.basic.exception;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public enum RpcErrorCode implements IErrorCode{


    @ErrorCode(name = "RPC_SERVICE_NOT_EXISTS") RPC_SERVICE_NOT_EXISTS, // Proxy

    @ErrorCode(name = "RPC_SERVICE_CONNECT_FAILED") RPC_SERVICE_CONNECT_FAILED, // Proxy,Client

    @ErrorCode(name = "RPC_SERVICE_CONNECT_TIMEOUT") RPC_SERVICE_CONNECT_TIMEOUT, // Proxy,Client

    @ErrorCode(name = "RPC_DESERIALIZE_FAILED") RPC_DESERIALIZE_FAILED, // Server,Client

    @ErrorCode(name = "RPC_SERIALIZE_FAILED") RPC_SERIALIZE_FAILED, // All

    @ErrorCode(name = "GENERAL_EXCEPTION") GENERAL_EXCEPTION, // All

    @ErrorCode(name = "RPC_CALLEE_UNKNOWN_EXCEPTION") RPC_CALLEE_UNKNOWN_EXCEPTION, // 遗留代码

    @ErrorCode(name = "RPC_CALLER_UNKNOWN_EXCEPTION") RPC_CALLER_UNKNOWN_EXCEPTION, // 遗留代码

    @ErrorCode(name = "RPC_CONNECTION_POOL_ERROR") RPC_CONNECTION_POOL_ERROR, // Proxy,Client

    @ErrorCode(name = "RPC_PROXY_SHUTDOWNING") RPC_PROXY_SHUTDOWNING, // Proxy

    @ErrorCode(name = "RPC_NO_AVAILABLE_PROXY") RPC_NO_AVAILABLE_PROXY, // Client

    @ErrorCode(name = "RPC_CALLEE_SERVICE_DEGRADED", description = "this service is degraded.", errorGroup = RpcErrorGroup.IGNORE) RPC_CALLEE_SERVICE_DEGRADED, // Server

    @ErrorCode(name = "ROUTE_EVALUATE_EXCEPTION") ROUTE_EVALUATE_EXCEPTION, // Proxy

    @ErrorCode(name = "RPC_CONNECT_CONFIGURATION_CENTER_FAILED") RPC_CONNECT_CONFIGURATION_CENTER_FAILED, // Proxy,Server

    @ErrorCode(name = "RPC_SERVICE_CALLEE_TIMEOUT", description = "call service timeout.", retry = true) RPC_SERVICE_CALLEE_TIMEOUT,


    @ErrorCode(name = "RPC_PROXY_RETRY_FAILED") RPC_PROXY_RETRY_FAILED,

    @ErrorCode(name = "RPC_SERVICE_OVERLOAD", description = "RPC serivce container overload.") RPC_SERVICE_OVERLOAD, // Container

    @ErrorCode(name = "RPC_SERVICE_THROTTLING", description = "RPC serivce container overload.", errorGroup = RpcErrorGroup.IGNORE) RPC_SERVICE_THROTTLING, // Container

    @ErrorCode(name = "RPC_IP_CONFIG_EXCEPTION") RPC_IP_CONFIG_EXCEPTION, // Proxy

    @ErrorCode(name = "RPC_HTTP_PROTOCOL_ERROR", description = "proxy's protocol is error.", errorGroup = "450") RPC_HTTP_PROTOCOL_ERROR, // Proxy

}
