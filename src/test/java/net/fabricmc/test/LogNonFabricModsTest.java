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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogHandler;

public class LogNonFabricModsTest {
	private LogHandler logHandler;

	/*
	 * Calls dumpNonFabricMods using reflection, so we don't need to make the class
	 * and method public.
	 */
	private static void dumpNonFabricMods(List<Path> nonFabricMods) {
		try {
			Method method = FabricLoaderImpl.class.getDeclaredMethod("dumpNonFabricMods", List.class);
			method.setAccessible(true);
			method.invoke(FabricLoaderImpl.INSTANCE, nonFabricMods);
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
		List<Path> nonFabricMods = new ArrayList<Path>();
		nonFabricMods.add(Paths.get("mods/non_fabric_mod1.jar"));
		nonFabricMods.add(Paths.get("mods/non_fabric_mod2.jar"));
		nonFabricMods.add(Paths.get("mods/non_fabric_mod3.jar"));

		dumpNonFabricMods(nonFabricMods);

		String expectedLog = "Found 3 non-fabric mod(s):"
				+ System.lineSeparator() + "\t- non_fabric_mod1.jar"
				+ System.lineSeparator() + "\t- non_fabric_mod2.jar"
				+ System.lineSeparator() + "\t- non_fabric_mod3.jar";

		Mockito.verify(logHandler, Mockito.times(1)).log(Mockito.anyLong(), Mockito.any(), Mockito.any(),
				eq(expectedLog), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
	}
}
