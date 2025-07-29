package com.example.myapplication.security;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for InputValidator class
 * Tests input validation and sanitization functionality
 */
public class InputValidatorTest {

    @Test
    public void testIsSafeText_ValidInput_ReturnsTrue() {
        // Test valid text inputs
        assertTrue(InputValidator.isSafeText("Hello World"));
        assertTrue(InputValidator.isSafeText("Test123"));
        assertTrue(InputValidator.isSafeText("Romania, Bucuresti."));
        assertTrue(InputValidator.isSafeText("Café & Restaurant"));
    }

    @Test
    public void testIsSafeText_InvalidInput_ReturnsFalse() {
        // Test invalid text inputs with dangerous characters
        assertFalse(InputValidator.isSafeText("<script>alert('xss')</script>"));
        assertFalse(InputValidator.isSafeText("'; DROP TABLE users; --"));
        assertFalse(InputValidator.isSafeText("file:///etc/passwd"));
        assertFalse(InputValidator.isSafeText("javascript:alert('xss')"));
    }

    @Test
    public void testIsSafeText_NullInput_ReturnsFalse() {
        assertFalse(InputValidator.isSafeText(null));
    }

    @Test
    public void testSanitizeText_RemovesDangerousCharacters() {
        String input = "<script>alert('xss')</script>Hello World";
        String sanitized = InputValidator.sanitizeText(input);
        assertEquals("Hello World", sanitized);
    }

    @Test
    public void testSanitizeText_NullInput_ReturnsEmptyString() {
        assertEquals("", InputValidator.sanitizeText(null));
    }

    @Test
    public void testIsValidEmail_ValidEmails_ReturnsTrue() {
        assertTrue(InputValidator.isValidEmail("test@example.com"));
        assertTrue(InputValidator.isValidEmail("user.name@domain.co.uk"));
        assertTrue(InputValidator.isValidEmail("test+tag@example.org"));
    }

    @Test
    public void testIsValidEmail_InvalidEmails_ReturnsFalse() {
        assertFalse(InputValidator.isValidEmail("invalid-email"));
        assertFalse(InputValidator.isValidEmail("@example.com"));
        assertFalse(InputValidator.isValidEmail("test@"));
        assertFalse(InputValidator.isValidEmail(""));
        assertFalse(InputValidator.isValidEmail(null));
    }

    @Test
    public void testIsNumeric_ValidNumbers_ReturnsTrue() {
        assertTrue(InputValidator.isNumeric("123"));
        assertTrue(InputValidator.isNumeric("0"));
        assertTrue(InputValidator.isNumeric("999999"));
    }

    @Test
    public void testIsNumeric_InvalidNumbers_ReturnsFalse() {
        assertFalse(InputValidator.isNumeric("123abc"));
        assertFalse(InputValidator.isNumeric("12.34"));
        assertFalse(InputValidator.isNumeric(""));
        assertFalse(InputValidator.isNumeric(null));
    }

    @Test
    public void testIsInRange_ValidRanges_ReturnsTrue() {
        assertTrue(InputValidator.isInRange(5, 1, 10));
        assertTrue(InputValidator.isInRange(1, 1, 10));
        assertTrue(InputValidator.isInRange(10, 1, 10));
        assertTrue(InputValidator.isInRange(5.5f, 1.0f, 10.0f));
    }

    @Test
    public void testIsInRange_InvalidRanges_ReturnsFalse() {
        assertFalse(InputValidator.isInRange(0, 1, 10));
        assertFalse(InputValidator.isInRange(11, 1, 10));
        assertFalse(InputValidator.isInRange(5.5f, 1.0f, 5.0f));
    }
} 