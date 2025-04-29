package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.chore.ChoreCreateRequest;
import com.homeprotectors.backend.dto.chore.ChoreCreateResponse;
import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.service.ChoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chores")
public class ChoreController {

    private final ChoreService choreService;

    @Operation(summary = "Create com.homeprotectors.backend.entity.Chore", description = "Create a new chore")
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

    @Operation(summary = "chore 목록 조회", description = "로그인 사용자의 그룹에 속한 모든 chore 목록을 조회합니다.")
    @GetMapping
    public ResponseDTO<List<ChoreListItemResponse>> getChoreList() {
        List<ChoreListItemResponse> chores = choreService.getChoreList();  // 인증 기반 그룹 필터링 가정
        return new ResponseDTO<>(true, "Chore list retrieved", chores);
    }

}
