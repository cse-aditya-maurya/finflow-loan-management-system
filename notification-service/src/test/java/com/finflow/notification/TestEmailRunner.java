package com.finflow.notification;

import com.finflow.notification.repository.NotificationRepository;
import com.finflow.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
class TestEmailRunner {

    @Autowired
    private EmailService emailService;

    // We mock the repository so Spring doesn't complain about missing Database
    @MockBean
    private NotificationRepository notificationRepository;

    @Test
    void sendTestEmail() {
        System.out.println("==================================================");
        System.out.println("🚀 MOCKING DATABASE & SENDING TEST EMAIL...");
        System.out.println("==================================================");
        
        emailService.sendLoanSubmissionEmailToUser(
                "mishraamresh2345@gmail.com",
                "Amresh Mishra",
                "APP-987654",
                "PERSONAL",
                500000.0,
                36
        );

        System.out.println("✅ Check your Inbox at mishraamresh2345@gmail.com!");
    }
}
