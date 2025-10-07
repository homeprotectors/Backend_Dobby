package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.chore.*;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.service.ChoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.Getter;
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

    @Operation(summary = "Chore 생성", description = "새 chore를 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseDTO<ChoreCreateResponse>> createChore(
            @Valid @RequestBody ChoreCreateRequest request) {
        Chore created = choreService.createChore(request);

        ChoreCreateResponse response = new ChoreCreateResponse(
                created.getId(),
                created.getTitle(),
                created.getRecurrenceType(),
                created.getSelectedCycle(),
                created.getRoomCategory(),
                created.getNextDue()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Chore created successfully", response));
    }

    @Operation(summary = "Chore 목록 조회", description = "사용자가 속한 그룹의 chore 목록을 조회합니다.")
    @GetMapping
    public ResponseDTO<List<ChoreListItemResponse>> getChoreList() {
        List<ChoreListItemResponse> chores = choreService.getChoreList();
        return new ResponseDTO<>(true, "Chore list retrieved", chores);
    }

    @Operation(summary = "Chore 수정", description = "기존 chore를 수정합니다.")
    @PutMapping("/{choreId}")
    public ResponseEntity<ResponseDTO<ChoreCreateResponse>> editChore(
            @PathVariable Long choreId,
            @RequestBody ChoreEditRequest request) {

        Chore updated = choreService.editChore(choreId, request);

        ChoreCreateResponse response = new ChoreCreateResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getRecurrenceType(),
                updated.getSelectedCycle(),
                updated.getRoomCategory(),
                updated.getNextDue()
        );

        return ResponseEntity.ok(new ResponseDTO<>(true, "Chore updated successfully", response));
    }

    @Operation(summary = "Chore 삭제", description = "chore를 ID로 삭제합니다.")
    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(@PathVariable Long choreId) {
        choreService.deleteChore(choreId);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "Chore 완료", description = "Chore를 완료 처리하고 다음 주기를 계산합니다.")
    @PostMapping("/complete")
    public ResponseEntity<ResponseDTO<ChoreCompleteResponse>> completeChore(
            @Valid @RequestBody ChoreCompleteRequest request) {
        ChoreCompleteResponse response = choreService.completeChore(request);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Chore completed successfully", response));
    }

    @Operation(summary = "Chore 섹션별 목록 조회", description = "Chore를 섹션별로 구분하여 조회합니다.")
    @GetMapping("/sections")
    public ResponseDTO<ChoreSectionsResponse> getChoreSections(
            @RequestParam(defaultValue = "50") int limitPerSection
    ) {
        var res = choreService.getChoreSections(limitPerSection);
        return new ResponseDTO<>(true, "Chore sections retrieved", res);
    }
}
