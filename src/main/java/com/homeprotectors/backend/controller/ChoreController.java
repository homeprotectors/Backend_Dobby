package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.chore.ChoreCreateRequest;
import com.homeprotectors.backend.dto.chore.ChoreCreateResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.service.ChoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chores")
public class ChoreController {

    private final ChoreService choreService;

    @Operation(summary = "Create com.homeprotectors.backend.entity.Chore", description = "새로운 집안일(chore)을 등록합니다.")
    @PostMapping
    public ResponseEntity<ResponseDTO<ChoreCreateResponse>> createChore(@Valid @RequestBody ChoreCreateRequest request) {
        Chore chore = choreService.createChore(request);

        ChoreCreateResponse response = new ChoreCreateResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getCycleDays(),
                chore.getReminderEnabled(),
                chore.getReminderDays()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Chore created successfully", response));
    }
}
