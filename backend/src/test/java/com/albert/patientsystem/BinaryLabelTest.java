package com.albert.patientsystem;

import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.SubtypeLabel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity tests for the wire-format enum conversions used by the API.
 * Catches the kind of off-by-one / case-sensitivity bug that caused
 * the original "did not match parameter type BinaryLabel" 500.
 */
class BinaryLabelTest {

    @Test
    void fromWireAcceptsLowercase() {
        assertEquals(BinaryLabel.MALIGNANT, BinaryLabel.fromWire("malignant"));
        assertEquals(BinaryLabel.BENIGN, BinaryLabel.fromWire("benign"));
    }

    @Test
    void fromWireAcceptsUppercase() {
        assertEquals(BinaryLabel.MALIGNANT, BinaryLabel.fromWire("MALIGNANT"));
        assertEquals(BinaryLabel.BENIGN, BinaryLabel.fromWire("Benign"));
    }

    @Test
    void fromWireRejectsUnknownValues() {
        assertThrows(IllegalArgumentException.class, () -> BinaryLabel.fromWire("unknown"));
        assertThrows(IllegalArgumentException.class, () -> BinaryLabel.fromWire(""));
        assertThrows(IllegalArgumentException.class, () -> BinaryLabel.fromWire("yes"));
    }

    @Test
    void wireRoundTrips() {
        assertEquals("malignant", BinaryLabel.MALIGNANT.wire());
        assertEquals("benign", BinaryLabel.BENIGN.wire());
    }

    @Test
    void allSubtypeLabelsAreRecognised() {
        for (SubtypeLabel s : SubtypeLabel.values()) {
            String wire = s.wire();
            assertEquals(s, SubtypeLabel.fromWire(wire),
                "round-trip failed for " + s);
        }
    }
}
