package com.github.daggerok.yetanothereventsourcing.rest;

import io.vavr.collection.LinkedHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class ResourcesConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(UserHandlers userHandlers) {
        return RouterFunctions.route()
                              .nest(path("/user"), userHandlers::routes)
                              .path("/**", this::fallbackRote)
                              .build()
                              .andRoute(path("/**"), this::apiInfo);
    }

    private RouterFunction<ServerResponse> fallbackRote() {
        return RouterFunctions.route()
                              .GET("/**", this::apiInfo)
                              .build();
    }

    private Mono<ServerResponse> apiInfo(ServerRequest request) {
        Map<String, String> info = LinkedHashMap.of("_base", Hateoas.baseUrl(request),
                                                    "_self", Hateoas.baseUrl(request, request.path()))
                                                .toJavaMap();
        return ok().body(Mono.just(info), Map.class);
    }
}
