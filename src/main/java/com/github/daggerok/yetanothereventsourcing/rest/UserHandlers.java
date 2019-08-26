package com.github.daggerok.yetanothereventsourcing.rest;

import com.github.daggerok.yetanothereventsourcing.user.CreateUser;
import com.github.daggerok.yetanothereventsourcing.user.LoadUserQuery;
import com.github.daggerok.yetanothereventsourcing.user.UserCommandHandler;
import com.github.daggerok.yetanothereventsourcing.user.UserQueryHandler;
import io.vavr.collection.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserHandlers {

    private final UserQueryHandler queryHandler;
    private final UserCommandHandler commandHandler;

    public RouterFunction<ServerResponse> routes(RouterFunctions.Builder builder) {
        return builder.GET("/{aggregateId}", this::loadUserQuery)
                      .POST("/", this::createUserCommand)
                      .build();
    }

    private Mono<ServerResponse> loadUserQuery(ServerRequest request) {
        String aggregateId = request.pathVariable("aggregateId");
        UUID uuid = UUID.fromString(aggregateId);
        LoadUserQuery loadUserQuery = new LoadUserQuery(uuid);
        return ServerResponse.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(Mono.just(queryHandler.handle(loadUserQuery))
                                       .map(LoadUserResponse::new), LoadUserResponse.class);
    }

    private Mono<ServerResponse> createUserCommand(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                      .map(r -> new CreateUser(r.getId(), r.getUsername()))
                      .doOnNext(commandHandler::handle)
                      .map(cmd -> LinkedHashMap.of("result", String.format("user created: %s", cmd.getAggregateId().toString()),
                                                   "aggregateId", cmd.getAggregateId().toString())
                                               .toJavaMap())
                      .flatMap(map -> ServerResponse.created(URI.create(Hateoas.baseUrl(request, "/user", map.get("aggregateId"))))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .body(Mono.just(map), Map.class));
    }
}
