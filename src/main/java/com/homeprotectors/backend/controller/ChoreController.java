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

    @Operation(summary = "chore 생성", description = "Create a new chore")
    @PostMapping
    public ResponseEntity<ResponseDTO<ChoreCreateResponse>> createChore(@Valid @RequestBody ChoreCreateRequest request) {
        Chore chore = choreService.createChore(request);

        ChoreCreateResponse response = new ChoreCreateResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getStartDate(),
                chore.getCycleDays(),
                chore.getNextDue(),
                chore.getReminderDays(),
                chore.getReminderDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Chore created successfully", response));
    }

    @Operation(summary = "chore 목록 조회", description = "Retrieve the list of chores in the group that the user belongs to")
    @GetMapping
    public ResponseDTO<List<ChoreListItemResponse>> getChoreList() {
        List<ChoreListItemResponse> chores = choreService.getChoreList();  // 인증 기반 그룹 필터링 가정
        return new ResponseDTO<>(true, "Chore list retrieved", chores);
    }

    @Operation(summary = "chore 수정", description = "Edit an existing chore")
    @PutMapping("/{choreId}")
    public ResponseEntity<ResponseDTO<ChoreCreateResponse>> editChore(
            @PathVariable Long choreId,
            @RequestBody ChoreEditRequest request) {

        Chore updated = choreService.editChore(choreId, request);

        ChoreCreateResponse response = new ChoreCreateResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getStartDate(),
                updated.getCycleDays(),
                updated.getNextDue(),
                updated.getReminderDays(),
                updated.getReminderDate()
        );

        return ResponseEntity.ok(new ResponseDTO<>(true, "Chore updated successfully", response));
    }

    @Operation(summary = "chore 삭제", description = "Delete a chore by ID")
    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(@PathVariable Long choreId) {
        choreService.deleteChore(choreId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @Operation(summary = "chore 완료", description = "Mark a chore as completed and update the next due date")
    @PostMapping("/complete")
    public ResponseEntity<ResponseDTO<ChoreCompleteResponse>> completeChore(
            @Valid @RequestBody ChoreCompleteRequest request) {
        ChoreCompleteResponse response = choreService.completeChore(request);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Chore completed successfully", response));
    }


    @Operation(summary = "chore 완료 취소", description = "Undo the completion of a chore")
    @PostMapping("/undo")
    public ResponseEntity<ResponseDTO<ChoreUndoResponse>> undoChoreCompletion(
            @Valid @RequestBody ChoreUndoRequest request) {
        ChoreUndoResponse response = choreService.undoChoreCompletion(request);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Chore completion undone successfully", response));
    }

    @Operation(summary = "chore history 조회", description = "Retrieve history records of a specific chore by ID")
    @GetMapping("/{choreId}/history")
    public ResponseDTO<ChoreHistoryItemResponse> getChoreHistory(@PathVariable Long choreId) {
        ChoreHistoryItemResponse history = choreService.getChoreHistory(choreId);
        if (history.getHistory().isEmpty()) {
            return new ResponseDTO<>(true, "No history records found for this chore", history);
        }
        return new ResponseDTO<>(true, "Chore history retrieved", history);
    }

}
