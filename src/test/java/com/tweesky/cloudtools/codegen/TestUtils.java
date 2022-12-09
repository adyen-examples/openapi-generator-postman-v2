package com.tweesky.cloudtools.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class TestUtils {

    public static void assertFileContains(Path path, String... lines) {
        try {
            String generatedFile = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            String file = linearize(generatedFile);
            assertNotNull(file);
            for (String line : lines)
                assertTrue(file.contains(linearize(line)), "File does not contain line [" + line + "]");
        } catch (IOException e) {
            fail("Unable to evaluate file " + path);
        }
    }

    public static String linearize(String target) {
        return target.replaceAll("\r?\n", "").replaceAll("\\s+", "\\s");
    }

    public static void assertFileNotContains(Path path, String... lines) {
        String generatedFile = null;
        try {
            generatedFile = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Unable to evaluate file " + path);
        }
        String file = linearize(generatedFile);
        assertNotNull(file);
        for (String line : lines)
            assertFalse(file.contains(linearize(line)));
    }

    public static void assertFileNotExists(Path path) {
        try {
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            fail("File exists when it should not: " + path);
        } catch (IOException e) {
            // File exists, pass.
            assertTrue(true);
        }
    }

    public static void assertFileExists(Path path) {
        try {
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            // File exists, pass.
            assertTrue(true);
        } catch (IOException e) {
            fail("File does not exist when it should: " + path);
        }
    }

}
