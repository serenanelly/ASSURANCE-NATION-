package com.assurance.nation.util;

import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PdfGeneratorTest {

    @Autowired
    private PdfGenerator pdfGenerator;

    @Test
    void generateJustificatif_createsPdfInTargetDir() throws Exception {
        Reimbursement reimbursement = Reimbursement.builder()
                .numRemboursement("REM-2026-000001")
                .montantTotal(new BigDecimal("150.00"))
                .tauxRemboursement(100)
                .montantRembourse(new BigDecimal("150.00"))
                .status(ReimbursementStatus.PENDING)
                .build();

        String path = pdfGenerator.generateJustificatif(reimbursement);

        Path file = Paths.get(path);
        assertThat(Files.exists(file)).isTrue();
        assertThat(Files.size(file)).isGreaterThan(0);
        assertThat(path).contains("target/test-justificatifs");
        assertThat(file.getFileName().toString()).isEqualTo("REM-2026-000001.pdf");
    }
}
