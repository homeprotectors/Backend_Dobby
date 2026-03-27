package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.Group;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GuestRegisterService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DefaultItemSeedService defaultItemSeedService;

    public GuestRegisterService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            DefaultItemSeedService defaultItemSeedService
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.defaultItemSeedService = defaultItemSeedService;
    }

    @Transactional
    public UUID registerGuest(UUID installId) {
        var existing = userRepository.findByInstallId(installId);
        if (existing.isPresent()) {
            return existing.get().getPublicId();
        }

        Long createdGroupId = null;
        try {
            Group group = createPersonalGroup();
            createdGroupId = group.getId();

            User user = new User(UUID.randomUUID(), installId, group.getId());
            userRepository.saveAndFlush(user);
            defaultItemSeedService.seedDefaults(group.getId(), user.getId());
            return user.getPublicId();
        } catch (DataIntegrityViolationException e) {
            cleanupOrphanGroup(createdGroupId);
            return userRepository.findByInstallId(installId)
                    .map(User::getPublicId)
                    .orElseThrow(() -> new ApiException("USER_REGISTER_FAILED", "Guest registration failed unexpectedly."));
        }
    }

    private Group createPersonalGroup() {
        Group group = new Group();
        group.setName("My Home");
        group.setInviteCode(generateInviteCode());
        return groupRepository.saveAndFlush(group);
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private void cleanupOrphanGroup(Long groupId) {
        if (groupId == null) return;
        try {
            if (userRepository.countByGroupId(groupId) == 0) {
                groupRepository.deleteById(groupId);
            }
        } catch (Exception ignored) {
            // Best-effort cleanup; registration fallback still handled by installId lookup.
        }
    }
}
