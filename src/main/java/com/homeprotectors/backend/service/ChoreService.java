package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.ChoreCreateRequest;
import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // nextDue 계산
        LocalDate baseDate = (request.getStartDate() != null) ? request.getStartDate() : LocalDate.now();
        if (request.getCycleDays() != null) {
            chore.setNextDue(baseDate.plusDays(request.getCycleDays()));
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
                        c.getReminderEnabled()
                ))
                .collect(Collectors.toList());
    }

}
