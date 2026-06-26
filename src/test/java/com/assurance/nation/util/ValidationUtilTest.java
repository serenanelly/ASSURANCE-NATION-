package com.assurance.nation.util;

import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {

    @Test
    void validateRPPS_valid() {
        assertThatCode(() -> ValidationUtil.validateRPPS("12345678901")).doesNotThrowAnyException();
    }

    @Test
    void validateRPPS_invalid() {
        assertThatThrownBy(() -> ValidationUtil.validateRPPS("123"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateNSS_valid() {
        assertThatCode(() -> ValidationUtil.validateNSS("123456789012345")).doesNotThrowAnyException();
    }

    @Test
    void validateNSS_invalid() {
        assertThatThrownBy(() -> ValidationUtil.validateNSS("abc"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePasswordMatch_ok() {
        assertThatCode(() -> ValidationUtil.validatePasswordMatch("Pass1!", "Pass1!"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePasswordMatch_mismatch() {
        assertThatThrownBy(() -> ValidationUtil.validatePasswordMatch("Pass1!", "Other1!"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateMedecinTraitant_mustBeGeneraliste() {
        Medecin m = new Medecin();
        m.setSpecialite(Specialite.SPECIALISTE);
        assertThatThrownBy(() -> ValidationUtil.validateMedecinTraitant(m))
                .isInstanceOf(ValidationException.class);
    }
}
