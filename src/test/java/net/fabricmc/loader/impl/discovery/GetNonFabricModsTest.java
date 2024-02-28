package net.fabricmc.loader.impl.discovery;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class GetNonFabricModsTest {
	private FabricLoaderImpl loader;
	private ModDiscoverer discoverer;

	/*
	 * Set up the mock loader and discoverer
	 */
	@BeforeEach
	public void setUp() {
		GameProvider provider = mock();
		Mockito.when(provider.getBuiltinMods()).thenReturn(Collections.emptyList());
		loader = mock();
		Mockito.when(loader.getGameProvider()).thenReturn(provider);
		Mockito.when(loader.isDevelopmentEnvironment()).thenReturn(false);
		discoverer = new ModDiscoverer(mock(), mock());
		discoverer.addCandidateFinder(new MockCandidateFinder());
	}

	/*
	 * Test that the discoverer can find non-fabric mods
	 */
	@Test
	public void testGetNonFabricMods() throws ModResolutionException {
		discoverer.discoverMods(loader, new HashMap<String, Set<ModCandidate>>());
		List<Path> nonFabricMods = discoverer.getNonFabricMods();
		Assertions.assertEquals(1, nonFabricMods.size());
		Assertions.assertEquals(Paths.get("./src/test/resources/testing.discovery/dummyNonFabricMod.jar"), nonFabricMods.get(0));
	}

	/*
	 * Mock candidate finder that returns two dummy mods (one fabric and one non-fabric)
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
