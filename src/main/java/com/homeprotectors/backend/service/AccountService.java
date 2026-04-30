package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.repository.BillHistoryRepository;
import com.homeprotectors.backend.repository.BillRepository;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import com.homeprotectors.backend.repository.StockRepository;
import com.homeprotectors.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final ChoreRepository choreRepository;
    private final StockRepository stockRepository;
    private final BillHistoryRepository billHistoryRepository;
    private final BillRepository billRepository;

    @Transactional
    public void deleteCurrentAccount(UUID currentUserId) {
        User user = userRepository.findByPublicId(currentUserId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found."));

        Long internalUserId = user.getId();
        Long groupId = user.getGroupId();

        deviceTokenRepository.deleteByUserId(internalUserId);
        notificationDeliveryLogRepository.deleteByUserId(internalUserId);
        userRepository.delete(user);

        if (userRepository.countByGroupId(groupId) > 0) {
            throw new ApiException("GROUP_HAS_OTHER_MEMBERS", "Cannot delete shared group with this endpoint.");
        }

        choreRepository.deleteAll(choreRepository.findByGroupId(groupId));
        stockRepository.deleteByGroupId(groupId);
        billHistoryRepository.deleteByGroupId(groupId);
        billRepository.hardDeleteByGroupId(groupId);
        groupRepository.deleteById(groupId);
    }
}
