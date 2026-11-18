/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.settings;

import java.util.List;
import java.util.Map;

/**
 * Settings for the workspace Bloom index ({@code xml.workspaceIndex}).
 *
 * <p><b>Disabled by default</b> — users must opt in by setting
 * {@code "xml.workspaceIndex.enabled": true} in their VS Code settings.
 *
 * <p>When enabled, the server maintains a per-file Bloom filter that powers
 * cross-file grammar reference features: Find References on grammar files,
 * the "N references" code lens, and automatic reference updates on rename.
 */
public class XMLWorkspaceIndexSettings {

	/**
	 * Default directory names that are excluded from the workspace scan.
	 * These directories are skipped entirely during the initial index build.
	 */
	public static final List<String> DEFAULT_EXCLUDE_DIRECTORIES = List.of(
			".git", ".hg", ".svn",
			"node_modules",
			"target", "build",
			".gradle",
			"out", "dist", "bin"
	);

	/**
	 * Default per-extension head-read limits, designed to prevent OOM on workspaces
	 * with large files while still capturing all grammar references:
	 * <ul>
	 *   <li>XML instance files ({@code .xml}): 64 KB — schema references always
	 *       appear in the prolog / root element.</li>
	 *   <li>Grammar files ({@code .xsd}, {@code .dtd}, {@code .rng}, {@code .rnc},
	 *       {@code .wsdl}, {@code .xsl}, {@code .xslt}): 512 KB — large enough for
	 *       typical grammars; keeps G1GC humongous-object allocations under control.</li>
	 * </ul>
	 * Users can override individual extensions or use {@code "null"} / {@code "0"}
	 * to remove limits for specific patterns.
	 */
	public static final Map<String, String> DEFAULT_HEAD_LIMITS = Map.of(
			"**/*.xml",  "64KB",
			"**/*.xsd",  "512KB",
			"**/*.dtd",  "512KB",
			"**/*.rng",  "512KB",
			"**/*.rnc",  "512KB",
			"**/*.wsdl", "512KB",
			"**/*.xsl",  "512KB",
			"**/*.xslt", "512KB"
	);

	private boolean enabled = false;

	private List<String> excludeDirectories = DEFAULT_EXCLUDE_DIRECTORIES;

	/**
	 * Per-file read limits for indexed files. Keys are glob patterns
	 * (e.g. {@code "**&#47;*.xsd"}, {@code "**&#47;vendor&#47;**"}); values are human-readable
	 * size strings (e.g. {@code "512KB"}, {@code "1Mo"}) or a raw integer (bytes).
	 * {@code null} or {@code "0"} means no limit (read the whole file).
	 *
	 * <p><b>Matching:</b> the longest matching pattern wins. Extension-only patterns
	 * ({@code "**&#47;*.ext"} or {@code "*.ext"}) are matched in O(1); path-based
	 * patterns use {@link java.nio.file.PathMatcher} on the absolute path.
	 *
	 * <p><b>Defaults:</b> {@link #DEFAULT_HEAD_LIMITS} — 64 KB for {@code .xml},
	 * 512 KB for grammar files. Set {@code "*": null} to disable all limits.
	 *
	 * <p>Example — disable limits for vendor XSDs, keep defaults for everything else:
	 * <pre>
	 * "xml.workspaceIndex.headLimits": {
	 *   "**&#47;vendor&#47;**": null,
	 *   "**&#47;*.xsd":    "512KB",
	 *   "**&#47;*.xml":    "64KB"
	 * }
	 * </pre>
	 */
	private Map<String, String> headLimits = DEFAULT_HEAD_LIMITS;

	/**
	 * Returns {@code true} when the workspace Bloom index is active.
	 *
	 * <p><b>Use case:</b> {@link WorkspaceIndexPlugin} checks this flag on every
	 * settings change to decide whether to trigger a scan or clear the index.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enables or disables the workspace Bloom index.
	 *
	 * <p><b>Use case:</b> Bound to {@code "xml.workspaceIndex.enabled"} in the client
	 * settings JSON. When switched to {@code false}, the plugin immediately clears
	 * the index so no stale candidates are served. When switched back to {@code true},
	 * a fresh workspace scan is triggered.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the directory names to skip during the workspace scan.
	 * Any directory whose name exactly matches an entry in this list is skipped
	 * entirely (along with all its descendants).
	 */
	public List<String> getExcludeDirectories() {
		return excludeDirectories;
	}

	/**
	 * Sets the directory names to skip during the workspace scan.
	 * Falls back to {@link #DEFAULT_EXCLUDE_DIRECTORIES} when {@code null} is supplied.
	 *
	 * <p><b>Use case:</b> Bound to {@code "xml.workspaceIndex.excludeDirectories"}.
	 * A project with a heavy {@code generated-sources} directory can add it here so
	 * the initial scan does not spend time indexing machine-generated XML.
	 */
	public void setExcludeDirectories(List<String> excludeDirectories) {
		this.excludeDirectories = excludeDirectories != null ? excludeDirectories : DEFAULT_EXCLUDE_DIRECTORIES;
	}

	/**
	 * Returns the raw (unparsed) head-limits map.
	 * Keys are file extensions without dot or {@code "*"}; values are size strings.
	 */
	public Map<String, String> getHeadLimits() {
		return headLimits;
	}

	/**
	 * Sets the raw head-limits map.
	 *
	 * <p><b>Use case:</b> Bound to {@code "xml.workspaceIndex.headLimits"}.
	 * The raw map is passed to
	 * {@link org.eclipse.lemminx.extensions.index.HeadLimitRules#compile(Map)}
	 * which compiles the glob patterns once for efficient per-file lookup.
	 */
	public void setHeadLimits(Map<String, String> headLimits) {
		this.headLimits = headLimits != null ? headLimits : DEFAULT_HEAD_LIMITS;
	}

	/**
	 * Parses a human-readable size string into a character count (not byte count —
	 * the index reads Java {@code char} units).
	 *
	 * <p>Supported formats (case-insensitive):
	 * <ul>
	 *   <li>{@code null}, {@code ""}, {@code "0"} → {@link Integer#MAX_VALUE} (no limit)</li>
	 *   <li>{@code "512"} → 512 chars (raw integer)</li>
	 *   <li>{@code "512KB"} or {@code "512Ko"} → 524 288</li>
	 *   <li>{@code "1MB"} or {@code "1Mo"} → 1 048 576</li>
	 *   <li>{@code "1GB"} or {@code "1Go"} → 1 073 741 824</li>
	 * </ul>
	 * Any unrecognised format returns {@link Integer#MAX_VALUE} (no limit).
	 *
	 * @param value raw size string from the settings JSON.
	 * @return parsed character limit, or {@link Integer#MAX_VALUE} for no limit.
	 */
	public static int parseSizeLimit(String value) {
		if (value == null || value.isBlank() || "0".equals(value.trim())) {
			return Integer.MAX_VALUE;
		}
		String v = value.trim().toUpperCase();
		try {
			if (v.endsWith("GB") || v.endsWith("GO")) {
				return Integer.parseInt(v.substring(0, v.length() - 2)) * 1024 * 1024 * 1024;
			}
			if (v.endsWith("MB") || v.endsWith("MO")) {
				return Integer.parseInt(v.substring(0, v.length() - 2)) * 1024 * 1024;
			}
			if (v.endsWith("KB") || v.endsWith("KO")) {
				return Integer.parseInt(v.substring(0, v.length() - 2)) * 1024;
			}
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * Copies all settings from {@code newSettings} into this instance.
	 *
	 * <p><b>Use case:</b> Called when the client sends {@code didChangeConfiguration}
	 * and the existing settings object needs to be updated in place (e.g. inside a
	 * cached {@link org.eclipse.lemminx.settings.XMLGeneralClientSettings} that other
	 * components hold a reference to).
	 */
	public void merge(XMLWorkspaceIndexSettings newSettings) {
		this.enabled = newSettings.isEnabled();
		this.excludeDirectories = newSettings.getExcludeDirectories();
		this.headLimits = newSettings.getHeadLimits();
	}
}
