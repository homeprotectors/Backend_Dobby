package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public Long requireInternalUserId(UUID publicUserId) {
        return requireUser(publicUserId).getId();
    }

    public Long requireGroupId(UUID publicUserId) {
        Long groupId = requireUser(publicUserId).getGroupId();
        if (groupId == null) {
            throw new ApiException("USER_GROUP_NOT_SET", "User does not belong to any group.");
        }
        return groupId;
    }

    public UUID requireGroupPublicId(UUID publicUserId) {
        Long groupId = requireGroupId(publicUserId);
        return groupRepository.findById(groupId)
                .map(group -> group.getPublicId())
                .orElseThrow(() -> new ApiException("GROUP_NOT_FOUND", "Group not found for user."));
    }

    public User requireUser(UUID publicUserId) {
        return userRepository.findByPublicId(publicUserId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found for X-USER-ID."));
    }
}
