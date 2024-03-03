/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.fabricmc.loader.impl.discovery.ModCandidate;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.ModMetadataParser;
import net.fabricmc.loader.impl.metadata.ParseMetadataException;
import net.fabricmc.loader.impl.metadata.VersionOverrides;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogHandler;

public class LogProvidedModsTest {
	private LogHandler logHandler;
    private static Path metadataPath;
	@BeforeAll
	public static void setupPaths() {
        Path testLocation = new File(System.getProperty("user.dir")).toPath()
                .resolve("src")
                .resolve("test")
                .resolve("resources")
                .resolve("testing");
		metadataPath = testLocation.resolve("metadata");
	}

	private static LoaderModMetadata parseMetadata(Path path) throws ParseMetadataException, IOException {
		try (InputStream is = Files.newInputStream(path)) {
			return ModMetadataParser.parseMetadata(is, "null", Collections.emptyList(),
					new VersionOverrides(),
					new DependencyOverrides(Paths.get("null")), false);
		}
	}
	private static void dumpModsHavingProvider(List<ModCandidate> LoadedMods) {
		try {
			Method method = FabricLoaderImpl.class.getDeclaredMethod("dumpModsHavingProvider", List.class);
			method.setAccessible(true);
			method.invoke(FabricLoaderImpl.INSTANCE, LoadedMods);
		} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException
				 | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Setup log handler before each test.
	 */
	@BeforeEach
	public void setUp() {
		// Create a mock of the class that contains function log
		logHandler = mock(LogHandler.class);
		Mockito.when(logHandler.shouldLog(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.doNothing().when(logHandler).log(Mockito.anyLong(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
		Log.init(logHandler);
	}

	/*
	 * Test that the log handler is called with the correct message when there are
	 * nested mods with providers found.
	 */
	@Test
	@DisplayName("Log nested mods that have providers")
	public void testModsHavingProvider() throws ParseMetadataException, IOException {

        LoaderModMetadata metadata1 = parseMetadata(metadataPath.resolve("fabric.test.mod1.json"));
        LoaderModMetadata metadata2 = parseMetadata(metadataPath.resolve("fabric.test.mod2.json"));
        LoaderModMetadata metadata3 = parseMetadata(metadataPath.resolve("fabric.test.mod3.json"));
        LoaderModMetadata metadata4 = parseMetadata(metadataPath.resolve("fabric.test.mod4.json"));
        LoaderModMetadata metadata5 = parseMetadata(metadataPath.resolve("fabric.test.mod5.json"));

        // Create nested testing mods
        List<ModCandidate> LoadedMods;

        {
            ModCandidate testMode1 = ModCandidate.createTestData(null, null,
                    -1, metadata1, false, Collections.emptyList());
            Collection<ModCandidate> nestedMod2 = new ArrayList<>();
            nestedMod2.add(testMode1);
            ModCandidate testMode2 = ModCandidate.createTestData(null, null,
                    -1, metadata2, false, nestedMod2);
            for (ModCandidate child : testMode2.getNestedMods()) {
                child.testAddParent(testMode2);
            }
            ModCandidate testMode3 = ModCandidate.createTestData(null, null,
                    -1, metadata3, false, Collections.emptyList());
            Collection<ModCandidate> nestedMod4 = new ArrayList<>();
            nestedMod4.add(testMode2);
            nestedMod4.add(testMode3);
            ModCandidate testMode4 = ModCandidate.createTestData(null, null,
                    -1, metadata4, false, nestedMod4);
            for (ModCandidate child : testMode4.getNestedMods()) {
                child.testAddParent(testMode4);
            }
            ModCandidate testMode5 = ModCandidate.createTestData(null, null,
                    -1, metadata5, false, Collections.emptyList());

            LoadedMods = new ArrayList<>();
            LoadedMods.add(testMode1);
            LoadedMods.add(testMode2);
            LoadedMods.add(testMode3);
            LoadedMods.add(testMode4);
            LoadedMods.add(testMode5);
        }

		dumpModsHavingProvider(LoadedMods);

        String expectedLog = "Found 3 loaded mods that have providers:"
                + System.lineSeparator() + "\t- mod1 0.1.0 (in  mod4 1.1.1)"
				+ System.lineSeparator() + "\t- mod2 2.2.7 (in  mod4 1.1.1)"
                + System.lineSeparator() + "\t- mod3 9.1.1 (in  mod4 1.1.1)";

        Mockito.verify(logHandler, Mockito.times(1)).log(
				Mockito.anyLong(), Mockito.any(), Mockito.any(),
				eq(expectedLog),
				Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }
}
