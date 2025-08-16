package com.gugugaga.auth.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.gugugaga.auth.dto.CreateUserRequest;
import com.gugugaga.auth.dto.UpdateUserRequest;
import com.gugugaga.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntityUser( CreateUserRequest req );
    void updateEntityFromDto( UpdateUserRequest req, @MappingTarget User user );
}
