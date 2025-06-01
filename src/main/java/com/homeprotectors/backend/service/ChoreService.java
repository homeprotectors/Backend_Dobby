package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.*;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.entity.ChoreHistory;
import com.homeprotectors.backend.repository.ChoreHistoryRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final GroupRepository groupRepository; // TODO: 그룹 기능 추후 추가
    private final ChoreHistoryRepository choreHistoryRepository;

    public Chore createChore(@Valid ChoreCreateRequest request) {
        Chore chore = new Chore();
        chore.setGroupId(1L); // TODO: 임시 group ID
        chore.setTitle(request.getTitle());
        chore.setCycleDays(request.getCycleDays());
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
        chore.setReminderDays(request.getReminderDays());

        if (request.getCycleDays() != null) {
            chore.setCycleDays(request.getCycleDays());
        }

        // 시작일이 null인 경우 오늘로 설정
        LocalDate startDate = (chore.getStartDate() != null) ? chore.getStartDate() : LocalDate.now();
        chore.setStartDate(startDate);

        // cycleDays가 변경되면 nextDue 재계산 - lastDone + cycleDays / lastDone이 없을 경우 startDate / 오늘 이전인 경우 오늘로 설정
        if (chore.getLastDone() != null) {
            LocalDate newNextDue = chore.getLastDone().plusDays(chore.getCycleDays());
            chore.setNextDue(newNextDue.isBefore(LocalDate.now()) ? LocalDate.now() : newNextDue);
        } else { // lastDone이 없을 경우
            if (startDate.isAfter(LocalDate.now())) { // 시작일이 오늘 이후면 nextDue를 시작일로 설정
                chore.setNextDue(startDate);
            } else { // 시작일이 오늘 이전이면 nextDue를 오늘로 설정
                chore.setNextDue(LocalDate.now());
            }
        }

        // reminderDate 재계산 (reminderDays 변경 시)
        if (chore.getReminderDays() != null) {
            LocalDate calculatedReminderDate = chore.getNextDue().minusDays(chore.getReminderDays());
            // reminderDate가 오늘 이전인 경우 오늘로 설정
            chore.setReminderDate(calculatedReminderDate.isBefore(LocalDate.now()) ? LocalDate.now() : calculatedReminderDate);
        } else {
            chore.setReminderDate(null); // reminderDays가 null인 경우 reminderDate는 null로 설정
        }


        return choreRepository.save(chore);
    }

    public void deleteChore(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new NoSuchElementException("해당 chore가 존재하지 않습니다."));

        // TODO: 인증 사용자 그룹 소속 여부 확인 필요 (JWT 인증 후 구현 예정)
        choreRepository.delete(chore);
    }

    public ChoreCompleteResponse completeChore(@Valid ChoreCompleteRequest request) {
        Chore chore = choreRepository.findById(request.getChoreId())
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));

        LocalDate doneDate = request.getDoneDate();
        LocalDate today = LocalDate.now();

        // 2주 이전 또는 미래 완료는 허용하지 않음
        if (doneDate.isBefore(today.minusDays(14)) || doneDate.isAfter(today)) {
            throw new IllegalArgumentException("완료 날짜는 오늘로부터 과거 14일 이내여야 합니다.");
        }

        // 히스토리 기록
        ChoreHistory history = new ChoreHistory();
        history.setChore(chore);
        history.setDoneDate(doneDate);
        history.setIsDone(true);
        history.setDoneBy(1L); // TODO: JWT 적용 후 대체
        history.setCreatedAt(LocalDateTime.now());

        // scheduledDate는 기존 nextDue로 설정
        history.setScheduledDate(chore.getNextDue());

        // nextDue & reminderDate 갱신: doneDate가 가장 최근일 때만
        if (chore.getLastDone() == null || doneDate.isAfter(chore.getLastDone())) {
            chore.setLastDone(doneDate);

            // nextDue 갱신 시 nextDue가 오늘보다 이전이면 오늘로 설정
            LocalDate newNextDue = doneDate.plusDays(chore.getCycleDays());
            chore.setNextDue(newNextDue.isBefore(today) ? today : newNextDue);

            // reminderDate 갱신
            if (chore.getReminderDays() != null) {
                LocalDate newReminderDate = newNextDue.minusDays(chore.getReminderDays());
                chore.setReminderDate(newReminderDate.isBefore(today) ? today : newReminderDate);
            } else {
                chore.setReminderDate(null);
            }
        }

        choreHistoryRepository.save(history);
        choreRepository.save(chore);

        return new ChoreCompleteResponse(
                chore.getId(),
                chore.getGroupId(),
                chore.getTitle(),
                history.getScheduledDate(),
                chore.getNextDue(),
                chore.getReminderDate(),
                1L // TODO: JWT 적용 후 대체
        );
    }

    public ChoreUndoResponse undoChoreCompletion(ChoreUndoRequest request) {
        Long userId = 1L; // TODO: JWT 적용 후 대체
        LocalDate today = LocalDate.now();

        Chore chore = choreRepository.findById(request.getChoreId())
                .orElseThrow(() -> new EntityNotFoundException("해당 Chore를 찾을 수 없습니다."));

        // 14일 이내만 취소 허용
        if (request.getDoneDate().isBefore(today.minusDays(14))) {
            throw new IllegalArgumentException("14일 이전 완료 기록은 취소할 수 없습니다.");
        }

        // 해당 날짜의 히스토리 조회
        ChoreHistory targetHistory = choreHistoryRepository
                .findByChoreIdAndDoneDate(request.getChoreId(), request.getDoneDate())
                .orElseThrow(() -> new EntityNotFoundException("해당 날짜의 완료 기록이 없습니다."));

        choreHistoryRepository.delete(targetHistory);

        // 삭제한 날짜가 chore.lastDone과 같으면, 다시 계산 필요
        if (chore.getLastDone() != null && chore.getLastDone().equals(request.getDoneDate())) {
            // 남아 있는 히스토리 중 가장 최근 doneDate 찾기
            Optional<ChoreHistory> latestHistoryOpt = choreHistoryRepository
                    .findTopByChoreIdAndIsDoneTrueOrderByDoneDateDesc(chore.getId());

            if (latestHistoryOpt.isPresent()) {
                LocalDate latestDoneDate = latestHistoryOpt.get().getDoneDate();
                chore.setLastDone(latestDoneDate);

                // nextDue, reminderDate 재계산 - 오늘 이전이면 오늘로 설정
                LocalDate newNextDue = latestDoneDate.plusDays(chore.getCycleDays());
                chore.setNextDue(newNextDue.isBefore(today) ? today : newNextDue);

                if (chore.getReminderDays() != null) {
                    LocalDate newReminderDate = newNextDue.minusDays(chore.getReminderDays());
                    chore.setReminderDate(newReminderDate.isBefore(today) ? today : newReminderDate);
                } else {
                    chore.setReminderDate(null);
                }
            } else {
                // 기록이 하나도 없다면 초기 상태로
                chore.setLastDone(null);
                chore.setNextDue(today);
                chore.setReminderDate(null);
            }

            choreRepository.save(chore);
        }
        return new ChoreUndoResponse(
                chore.getId(),
                chore.getNextDue(),
                chore.getReminderDate(),
                chore.getLastDone()
        );
    }



}
