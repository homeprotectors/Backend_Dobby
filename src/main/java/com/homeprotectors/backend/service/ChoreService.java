package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.ChoreCreateRequest;
import com.homeprotectors.backend.dto.chore.ChoreEditRequest;
import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final GroupRepository groupRepository; // TODO: 그룹 기능 추후 추가

    public Chore createChore(@Valid ChoreCreateRequest request) {
        Chore chore = new Chore();
        chore.setGroupId(1L); // TODO: 임시 group ID
        chore.setTitle(request.getTitle());
        chore.setCycleDays(request.getCycleDays());
        chore.setReminderEnabled(request.getReminderEnabled());
        chore.setReminderDays(request.getReminderDays());
        chore.setCreatedBy(1L); // TODO: 임시 user ID. 나중에 JWT 인증 적용해서 동적으로 변경
        chore.setCreatedAt(LocalDateTime.now());

        // 시작일이 null인 경우 오늘로 설정
        LocalDate startDate = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
        chore.setStartDate(startDate);

        // chore 생성 시 nextDue는 시작일로 설정
        chore.setNextDue(startDate);

        // reminderDate 계산
        if (chore.getReminderDays() != null) {
            LocalDate calculatedReminderDate = chore.getNextDue().minusDays(chore.getReminderDays());
            // reminderDate가 오늘 이전인 경우 오늘로 설정
            chore.setReminderDate(calculatedReminderDate.isBefore(LocalDate.now()) ? LocalDate.now() : calculatedReminderDate);
        }

        return choreRepository.save(chore);
    }

    public List<ChoreListItemResponse> getChoreList() {
        Long groupId = 1L; // TODO: 인증 기반으로 동적으로 받을 예정

        List<Chore> chores = choreRepository.findByGroupId(groupId);
        return chores.stream()
                .map(c -> new ChoreListItemResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getCycleDays(),
                        c.getNextDue(),
                        c.getReminderEnabled(),
                        c.getReminderDays(),
                        c.getReminderDate()
                ))
                .collect(Collectors.toList());
    }

    public Chore editChore(Long choreId, ChoreEditRequest request) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));

        if (request.getTitle() != null) {
            chore.setTitle(request.getTitle());
        }
        if (request.getCycleDays() != null) {
            chore.setCycleDays(request.getCycleDays());
        }
        if (request.getStartDate() != null) {
            chore.setStartDate(request.getStartDate());
        }
        if (request.getReminderEnabled() != null) {
            chore.setReminderEnabled(request.getReminderEnabled());
        }
        if (request.getReminderDays() != null) {
            chore.setReminderDays(request.getReminderDays());
        }

        if (request.getStartDate() != null) {
            chore.setStartDate(request.getStartDate());
        } else if (chore.getStartDate() == null) {
            chore.setStartDate(LocalDate.now());
        }

        if (request.getCycleDays() != null) {
            chore.setCycleDays(request.getCycleDays());
        }

        // 시작일이 null인 경우 오늘로 설정
        LocalDate startDate = (chore.getStartDate() != null) ? chore.getStartDate() : LocalDate.now();
        chore.setStartDate(startDate);

        // chore 수정 시 nextDue는 시작일로 설정
        chore.setNextDue(startDate);

        // reminderDate 재계산 (reminderDays 변경 시)
        if (chore.getReminderDays() != null) {
            LocalDate calculatedReminderDate = chore.getNextDue().minusDays(chore.getReminderDays());
            // reminderDate가 오늘 이전인 경우 오늘로 설정
            chore.setReminderDate(calculatedReminderDate.isBefore(LocalDate.now()) ? LocalDate.now() : calculatedReminderDate);
        }


        return choreRepository.save(chore);
    }

    public void deleteChore(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new NoSuchElementException("해당 chore가 존재하지 않습니다."));

        // TODO: 인증 사용자 그룹 소속 여부 확인 필요 (JWT 인증 후 구현 예정)
        choreRepository.delete(chore);
    }



}
