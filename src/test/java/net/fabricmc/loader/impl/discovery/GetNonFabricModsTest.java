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

package net.fabricmc.loader.impl.discovery;

import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

public class GetNonFabricModsTest {
	private FabricLoaderImpl loader;
	private FabricLauncher launcher;
	private ModDiscoverer discoverer;
	private MockedConstruction<FabricLoaderImpl> loaderConstruction;

	/*
	 * Set up the mock loader and discoverer
	 */
	@BeforeEach
	public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		GameProvider provider = mock();
		Mockito.when(provider.getBuiltinMods()).thenReturn(Collections.emptyList());

		launcher = mock();
		Mockito.when(launcher.getEnvironmentType()).thenReturn(EnvType.CLIENT);
		Mockito.when(launcher.isDevelopment()).thenReturn(false);

		Method setLauncher = FabricLauncherBase.class.getDeclaredMethod("setLauncher", FabricLauncher.class);
		setLauncher.setAccessible(true);
		setLauncher.invoke(null, launcher);

		loader = mock();
		Mockito.when(loader.getGameProvider()).thenReturn(provider);
		Mockito.when(loader.isDevelopmentEnvironment()).thenReturn(false);

		loaderConstruction = Mockito.mockConstructionWithAnswer(FabricLoaderImpl.class, invocation -> loader);

		discoverer = new ModDiscoverer(mock(), mock());
		discoverer.addCandidateFinder(new MockCandidateFinder());
	}

	@AfterEach
	public void tearDown() {
		loaderConstruction.close();
	}

	/*
	 * Test that the discoverer can find non-fabric mods
	 */
	@Test
	public void testGetNonFabricMods() throws ModResolutionException {
		discoverer.discoverMods(loader, new HashMap<String, Set<ModCandidate>>());
		List<Path> nonFabricMods = discoverer.getNonFabricMods();
		Assertions.assertEquals(1, nonFabricMods.size());
		Assertions.assertEquals(Paths.get("./src/test/resources/testing.discovery/dummyNonFabricMod.jar"),
				nonFabricMods.get(0));
	}

	/*
	 * Mock candidate finder that returns two dummy mods (one fabric and one
	 * non-fabric)
	 */
	public static class MockCandidateFinder implements ModCandidateFinder {
		@Override
		public void findCandidates(ModCandidateConsumer out) {
			List<Path> modPaths = new ArrayList<>();
			modPaths.add(Paths.get("./src/test/resources/testing.discovery/dummyFabricMod.jar"));
			modPaths.add(Paths.get("./src/test/resources/testing.discovery/dummyNonFabricMod.jar"));
			out.accept(modPaths, false);
		}
	}
}
