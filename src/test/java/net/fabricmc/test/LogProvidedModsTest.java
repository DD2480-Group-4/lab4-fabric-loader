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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogHandler;

public class LogProvidedModsTest {
	private LogHandler logHandler;

	/*
	 * Calls dumpModsHavingProvider using reflection, so we don't need to make the class
	 * and method public.
	 */
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
		logHandler = mock();
		Mockito.when(logHandler.shouldLog(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.doNothing().when(logHandler).log(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
		Log.init(logHandler);
	}

	/*
	 * Test that the log handler is called with the correct message when there are
	 * non-fabric mods found.
	 */
	@Test
	public void testLogNonFabricMods() {
		ModCandidate testMode1 = ModCandidate.createTestData(null, null,
				-1, null, false, Collections.emptyList());
		ModCandidate testMode2 = ModCandidate.createTestData(null, null,
				-1, null, false, Collections.emptyList());
		ModCandidate testMode3 = ModCandidate.createTestData(null, null,
				-1, null, false, Collections.emptyList());


		List<ModCandidate> LoadedMods = new ArrayList<>();
		LoadedMods.add(testMode1);
		LoadedMods.add(testMode2);
		LoadedMods.add(testMode3);

		dumpModsHavingProvider(LoadedMods);

		String expectedLog = "Found 2 loaded mods that have providers:"
				+ System.lineSeparator() + "\t- mod1 0.1.0 (in mod x 2.2.7)"
				+ System.lineSeparator() + "\t- mod3 9.1.1 (in mod y 1.1.1)";

		Mockito.verify(logHandler, Mockito.times(1)).log(Mockito.anyLong(), Mockito.any(), Mockito.any(),
				eq(expectedLog), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
	}
}
