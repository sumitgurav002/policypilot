package com.policypilot.guard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    @Test
    void masksEmail() {
        assertThat(PiiMasker.mask("mail asha.kulkarni@example.com now"))
                .isEqualTo("mail a***@example.com now");
    }

    @Test
    void masksIndianMobileKeepingLastFourDigits() {
        assertThat(PiiMasker.mask("call 9876543210")).isEqualTo("call XXXXXX3210");
        assertThat(PiiMasker.mask("call +91 9876543210")).isEqualTo("call XXXXXX3210");
    }

    @Test
    void masksPanAndAadhaar() {
        assertThat(PiiMasker.mask("PAN ABCDE1234F")).isEqualTo("PAN XXXXX####X");
        assertThat(PiiMasker.mask("Aadhaar 1234 5678 9012")).isEqualTo("Aadhaar XXXX-XXXX-9012");
    }

    @Test
    void leavesNormalTextAlone() {
        assertThat(PiiMasker.mask("Claim CLM-1023 for POL-1001")).isEqualTo("Claim CLM-1023 for POL-1001");
    }
}
