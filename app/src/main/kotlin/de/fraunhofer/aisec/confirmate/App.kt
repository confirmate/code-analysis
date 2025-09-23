/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate

import de.fraunhofer.aisec.codyze.AnalysisProject
import de.fraunhofer.aisec.codyze.AnalysisResult
import de.fraunhofer.aisec.confirmate.integration.ClouditorClient
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.concepts.TagOverlaysPass
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.tag
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlinx.coroutines.runBlocking

/**
 * Evaluates a script file with Codyze using the provided profile.
 *
 * @param scriptFile The path to the script file to evaluate.
 * @param profile A function that configures the translation configuration for the analysis.
 * @return The analysis result, or null if the project could not be created.
 */
fun evaluateWithCodyze(
    scriptFile: String,
    profile: (TranslationConfiguration.Builder) -> TranslationConfiguration.Builder = { it },
): AnalysisResult? {
    val absoluteFile = Path(scriptFile).absolute()
    val project = AnalysisProject.fromScript(absoluteFile) { profile(it) }
    val result = project?.analyze() ?: return null

    // Print some performance metrics
    result.translationResult.benchmarkResults.print()

    runBlocking { ClouditorClient().sendClouditorResults(result) }

    return result
}

/** Registers the tagging profiles in the [TranslationConfiguration] builder. */
fun TranslationConfiguration.Builder.taggingProfiles(profiles: TaggingContext.() -> Unit) {
    registerPass<TagOverlaysPass>()
    configurePass<TagOverlaysPass>(TagOverlaysPass.Configuration(tag { apply(profiles) }))
}
