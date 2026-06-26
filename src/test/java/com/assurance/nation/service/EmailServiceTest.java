package com.assurance.nation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailService emailService;

    @Test
    void sendAsync_sendsMailMessage() {
        emailService.sendAsync("patient@test.com", "Sujet test", "Corps du message");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(message.getTo()).containsExactly("patient@test.com");
        org.assertj.core.api.Assertions.assertThat(message.getSubject()).isEqualTo("Sujet test");
        org.assertj.core.api.Assertions.assertThat(message.getText()).isEqualTo("Corps du message");
    }

    @Test
    void sendAsync_doesNotPropagateMailFailure() {
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendAsync("fail@test.com", "Sujet", "Corps"))
                .doesNotThrowAnyException();
    }
}
