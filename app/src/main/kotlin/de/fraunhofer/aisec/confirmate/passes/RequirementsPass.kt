/*
 * This file is part of the Confirmate project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package de.fraunhofer.aisec.confirmate.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.passes.SymbolResolver
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.configuration.DependsOn
import io.clouditor.model.Library
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.uuid.ExperimentalUuidApi

/** Pass that analyzes requirements.txt files and creates Library resources from them. */
@DependsOn(SymbolResolver::class)
class RequirementsPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun accept(t: TranslationResult) {
        // Find requirements.txt in the project
        val (requirementsPath, componentId) = findRequirementsFile(t)

        if (requirementsPath != null && requirementsPath.exists()) {
            val libraries = parseRequirements(requirementsPath, componentId)
            t.addOntologyObjects(*libraries.toTypedArray())
        }
    }

    /**
     * Finds the requirements.txt file by searching from component source locations. Returns the
     * path and component ID.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun findRequirementsFile(t: TranslationResult): Pair<Path?, String?> {
        // Try to get a file path from the translation units in the components
        for (component in t.components) {
            // Get translation units (source files) from the component
            val translationUnits = component.translationUnits

            for (tu in translationUnits) {
                // Get the absolute file location from the translation unit
                val artifactLocation = tu.location?.artifactLocation?.uri
                if (artifactLocation != null) {
                    val sourceFile = File(artifactLocation)
                    if (sourceFile.exists()) {
                        var parent = sourceFile.parentFile

                        // Walk up the directory tree looking for requirements.txt
                        var depth = 0
                        while (parent != null && depth < 5) {
                            val requirementsFile = File(parent, "requirements.txt").toPath()
                            if (requirementsFile.exists()) {
                                return Pair(requirementsFile, component.id.toString())
                            }
                            parent = parent.parentFile
                            depth++
                        }
                    }
                }
            }
        }

        return Pair(null, null)
    }

    /**
     * Parses a requirements.txt file and returns a list of Library objects.
     *
     * Supports the following formats:
     * - package==version
     * - package>=version
     * - package<=version
     * - package~=version
     * - package
     */
    private fun parseRequirements(requirementsPath: Path, parentId: String?): List<Library> {
        val libraries = mutableListOf<Library>()

        requirementsPath.readLines().forEach { line ->
            val trimmedLine = line.trim()

            // Skip empty lines and comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                return@forEach
            }

            // Parse the requirement
            val library = parseRequirement(trimmedLine, parentId)
            if (library != null) {
                libraries.add(library)
            }
        }

        return libraries
    }

    /** Parses a single requirement line and returns a Library object with PURL format. */
    private fun parseRequirement(requirement: String, parentId: String?): Library? {
        // Split by common version operators
        val operators = listOf("==", ">=", "<=", "~=", ">", "<")

        for (operator in operators) {
            if (requirement.contains(operator)) {
                val parts = requirement.split(operator, limit = 2)
                val packageName = parts[0].trim().lowercase()
                val version = parts.getOrNull(1)?.trim()

                // Use PURL format: pkg:pypi/package-name@version
                val purl =
                    if (version != null) {
                        "pkg:pypi/$packageName@$version"
                    } else {
                        "pkg:pypi/$packageName"
                    }

                return Library(id = packageName, name = purl, parentId = parentId)
            }
        }

        // No version specified
        val packageName = requirement.trim().lowercase()
        return Library(id = packageName, name = "pkg:pypi/$packageName", parentId = parentId)
    }

    override fun cleanup() {
        // Nothing to do
    }
}

private fun TranslationResult.addOntologyObjects(vararg obj: Any) {
    // We re-use the scratch field behind the scenes
    this.ontologyObjects.addAll(obj)
}

typealias OntologyObjects = MutableList<Any>

val TranslationResult.ontologyObjects: OntologyObjects
    get() {
        return this.scratch.computeIfAbsent("ontology") { mutableListOf<Any>() } as MutableList<Any>
    }
