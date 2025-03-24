package com.example.lms.logging.controller;

import com.example.lms.logging.assembler.UserActivityLogAssembler;
import com.example.lms.logging.model.UserActivityLog;
import com.example.lms.logging.service.UserActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-logs")
public class UserActivityLogController {

    @Autowired
    private UserActivityLogService logService;

    @Autowired
    private UserActivityLogAssembler assembler;

    @GetMapping("/{userId}")
    public ResponseEntity<CollectionModel<?>> getUserLogs(@PathVariable Long userId) {
        List<UserActivityLog> logs = logService.getUserLogs(userId);
        return ResponseEntity.ok(assembler.toCollection(logs));
    }
}
