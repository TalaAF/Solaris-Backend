package com.example.lms.logging.assembler;

import com.example.lms.logging.dto.UserActivityLogDTO;
import com.example.lms.logging.model.UserActivityLog;
import com.example.lms.logging.mapper.UserActivityLogMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserActivityLogAssembler {
    private final UserActivityLogMapper mapper;

    public UserActivityLogAssembler(UserActivityLogMapper mapper) {
        this.mapper = mapper;
    }

    public EntityModel<UserActivityLogDTO> toModel(UserActivityLog log) {
        UserActivityLogDTO dto = mapper.toDTO(log);
        return EntityModel.of(dto,
            linkTo(methodOn(com.example.lms.logging.controller.UserActivityLogController.class)
                .getUserLogs(dto.getUserId())).withRel("user-logs"));
    }

    public CollectionModel<EntityModel<UserActivityLogDTO>> toCollection(List<UserActivityLog> logs) {
        List<EntityModel<UserActivityLogDTO>> logModels = logs.stream()
            .map(this::toModel)
            .collect(Collectors.toList());

        return CollectionModel.of(logModels);
    }
}
