package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.entity.BillCategory;
import com.homeprotectors.backend.entity.BillHistory;
import com.homeprotectors.backend.entity.BillType;
import com.homeprotectors.backend.entity.DevicePlatform;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.Group;
import com.homeprotectors.backend.entity.NotificationDeliveryLog;
import com.homeprotectors.backend.entity.NotificationDeliveryStatus;
import com.homeprotectors.backend.entity.NotificationType;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.repository.BillHistoryRepository;
import com.homeprotectors.backend.repository.BillRepository;
import com.homeprotectors.backend.repository.ChoreHistoryRepository;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import com.homeprotectors.backend.repository.StockRepository;
import com.homeprotectors.backend.repository.UserRepository;
import com.homeprotectors.backend.service.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Autowired
    private ChoreRepository choreRepository;

    @Autowired
    private ChoreHistoryRepository choreHistoryRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillHistoryRepository billHistoryRepository;

    @MockBean
    private JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        billHistoryRepository.deleteAll();
        billRepository.deleteAll();
        choreHistoryRepository.deleteAll();
        choreRepository.deleteAll();
        stockRepository.deleteAll();
        notificationDeliveryLogRepository.deleteAll();
        deviceTokenRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    void deleteCurrentAccount_deletesUserAndRelatedDataExceptChores() throws Exception {
        Group group = groupRepository.save(new Group(null, null, "My Home", "INVITE01", null, null));
        User user = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setUserId(user.getId());
        deviceToken.setPlatform(DevicePlatform.IOS);
        deviceToken.setPushToken("push-token-1");
        deviceTokenRepository.save(deviceToken);

        NotificationDeliveryLog deliveryLog = new NotificationDeliveryLog();
        deliveryLog.setUserId(user.getId());
        deliveryLog.setNotificationType(NotificationType.DAILY_CHORE_REMINDER);
        deliveryLog.setDeliveryDate(LocalDate.now());
        deliveryLog.setStatus(NotificationDeliveryStatus.SENT);
        deliveryLog.setTitle("title");
        deliveryLog.setBody("body");
        notificationDeliveryLogRepository.save(deliveryLog);

        Stock stock = new Stock();
        stock.setGroupId(group.getId());
        stock.setName("Soap");
        stock.setUnitQuantity(3);
        stock.setUnitDays(7);
        stock.setCreatedBy(user.getId());
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedQuantity(2);
        stock.setUpdatedQuantityDate(LocalDate.now());
        stockRepository.save(stock);

        Bill bill = new Bill();
        bill.setGroupId(group.getId());
        bill.setCreatedBy(user.getId());
        bill.setName("Rent");
        bill.setCategory(BillCategory.values()[0]);
        bill.setType(BillType.values()[0]);
        bill.setAmount(100.0);
        bill.setIsVariable(false);
        bill.setDueDate(5);
        bill = billRepository.save(bill);

        BillHistory billHistory = new BillHistory();
        billHistory.setGroupId(group.getId());
        billHistory.setPaidBy(user.getId());
        billHistory.setBill(bill);
        billHistory.setYearMonth(LocalDate.of(2026, 5, 1));
        billHistory.setAmount(100.0);
        billHistory.setPaidDate(LocalDate.of(2026, 5, 3));
        billHistoryRepository.save(billHistory);

        mockMvc.perform(delete("/api/account")
                        .requestAttr("currentUserId", user.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByPublicId(user.getPublicId())).isEmpty();
        assertThat(groupRepository.existsById(group.getId())).isFalse();
        assertThat(deviceTokenRepository.findAll()).isEmpty();
        assertThat(notificationDeliveryLogRepository.findAll()).isEmpty();
        assertThat(stockRepository.findByGroupId(group.getId())).isEmpty();
        assertThat(billRepository.findAll()).isEmpty();
        assertThat(billHistoryRepository.findAll()).isEmpty();
    }

    @Test
    void deleteCurrentAccount_whenGroupHasOtherMembers_rollsBackDeletion() throws Exception {
        Group group = groupRepository.save(new Group(null, null, "Shared Home", "INVITE02", null, null));
        User currentUser = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));
        User anotherUser = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setUserId(currentUser.getId());
        deviceToken.setPlatform(DevicePlatform.ANDROID);
        deviceToken.setPushToken("push-token-2");
        deviceToken.setLastSeenAt(OffsetDateTime.now());
        deviceTokenRepository.save(deviceToken);

        NotificationDeliveryLog deliveryLog = new NotificationDeliveryLog();
        deliveryLog.setUserId(currentUser.getId());
        deliveryLog.setNotificationType(NotificationType.DAILY_CHORE_REMINDER);
        deliveryLog.setDeliveryDate(LocalDate.now());
        deliveryLog.setStatus(NotificationDeliveryStatus.SENT);
        notificationDeliveryLogRepository.save(deliveryLog);

        mockMvc.perform(delete("/api/account")
                        .requestAttr("currentUserId", currentUser.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete shared group with this endpoint."));

        assertThat(userRepository.findByPublicId(currentUser.getPublicId())).isPresent();
        assertThat(userRepository.findByPublicId(anotherUser.getPublicId())).isPresent();
        assertThat(groupRepository.existsById(group.getId())).isTrue();
        assertThat(deviceTokenRepository.findAll()).hasSize(1);
        assertThat(notificationDeliveryLogRepository.findAll()).hasSize(1);
    }
}
