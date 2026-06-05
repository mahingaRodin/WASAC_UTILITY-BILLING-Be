package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.NotificationRepository notificationRepository;
    @Mock
    private MailService mailService;
    @Mock
    private CurrentCustomerResolver currentCustomerResolver;
    @Mock
    private com.wasac.utilitybilling.mapper.NotificationMapper notificationMapper;
    @Mock
    private PdfService pdfService;
    @Mock
    private com.wasac.utilitybilling.repository.BillLineItemRepository billLineItemRepository;
    @Mock
    private com.wasac.utilitybilling.mapper.BillMapper billMapper;
    @Mock
    private com.wasac.utilitybilling.repository.CustomerRepository customerRepository;
    @Mock
    private com.wasac.utilitybilling.repository.UserRepository userRepository;
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void listShouldQueryRepository() {
        when(notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(java.util.List.of()));
        notificationService.list(PageRequest.of(0, 10));
        verify(notificationRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    @Test
    void myNotificationsShouldUseResolvedCustomerEmail() {
        Customer customer = Customer.builder().email("me@wasac.rw").build();
        when(currentCustomerResolver.resolve()).thenReturn(customer);
        when(notificationRepository.findByCustomer_EmailOrderByCreatedAtDesc("me@wasac.rw", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(java.util.List.of()));
        notificationService.getMyNotifications(PageRequest.of(0, 10));
        verify(notificationRepository).findByCustomer_EmailOrderByCreatedAtDesc("me@wasac.rw", PageRequest.of(0, 10));
    }
}
