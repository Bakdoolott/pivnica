package com.github.bakdoolott.gateway.error;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (!isDownstreamUnavailable(ex)) {
            return Mono.error(ex);
        }

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = """
                {"error":"SERVICE_UNAVAILABLE","message":"Downstream service is unavailable"}
                """.trim().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private boolean isDownstreamUnavailable(Throwable ex) {
        Throwable current = ex;

        while (current != null) {
            if (current instanceof UnknownHostException || current instanceof ConnectException) {
                return true;
            }

            String className = current.getClass().getName();
            if (className.equals("io.netty.channel.ConnectTimeoutException")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
