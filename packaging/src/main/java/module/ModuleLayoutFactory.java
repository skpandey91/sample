/*
 * Copyright 2016-2019 Amdocs Software Systems Limited.
 *
 */

package module;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Load module factory.
 */

public class ModuleLayoutFactory implements LayoutFactory {

	@Override
	public Layout getLayout(File file) {

		System.out.println("file path is " + file.getAbsolutePath() + "file Size" + file.length());
		checkJarHasModuleConfigEntryWithLabelName(file);

		return new Module();
	}

	/**
	 * verify if module has a configuration file with the label name which defines the module.
	 * @param file the jar file of the module
	 */
	private void checkJarHasModuleConfigEntryWithLabelName(File file) {

		try(InputStream in = Files.newInputStream(file.toPath());
			JarInputStream jarInputStream = new JarInputStream(in)) {
			boolean hasConfig = false;
			JarEntry jarEntry;
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				if (jarEntry.getName()
					.equals("MODULE-INF/module-properties.yaml")) {
					try(InputStreamReader reader = new InputStreamReader(jarInputStream, StandardCharsets.UTF_8);
						BufferedReader buffer = new BufferedReader(reader);) {
						if (buffer.lines()
							.noneMatch(line -> line.startsWith("  name: "))) {
							throw new IllegalArgumentException("Module does not contain a name tag in MODULE-INF/module-properties.yaml");
						}
					}
					hasConfig = true;
					break;
				}
			}
			if (!hasConfig) {
				throw new IllegalArgumentException("Configuration file module-properties.yaml could not be found under MODULE-INF/");
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(String.format(
				"Cannot read jar '%s': %s", file, e), e);
		}
	}

}
