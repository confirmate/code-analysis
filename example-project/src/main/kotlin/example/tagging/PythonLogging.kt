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
package example.tagging

import de.fraunhofer.aisec.cpg.graph.concepts.logging.newLogGet
import de.fraunhofer.aisec.cpg.graph.concepts.logging.newLogWrite
import de.fraunhofer.aisec.cpg.graph.concepts.logging.newLogging
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.followPrevFullDFGEdgesUntilHit
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

/**
 * Storage connecting logger names with [Logging] concepts. Used to connect logging calls to the
 * matching [Logging].
 */
private val loggers = mutableMapOf<String, Logging>()

/** Tagging for Python logging */
fun TaggingContext.tagLogging() {
    each<CallExpression>("logging.getLogger").with {
        val loggerName = node.arguments.firstOrNull()?.evaluate() as? String ?: ""
        val logging =
            loggers.computeIfAbsent(loggerName) {
                node.newLogging(
                    underlyingNode = node,
                    name = loggerName,
                    logLevelThreshold = LogLevel.WARN,
                    enabled = true,
                    connect = true,
                    logFields = mutableListOf(CurrentTimeField, LoggerNameField, LogMessage),
                )
            }
        node.newLogGet(underlyingNode = node, concept = logging, connect = true)
    }

    // Tag logger.info() calls
    each<CallExpression>(predicate = { it.name.localName == "info" }).with {
        node.findLogger()?.let { logging ->
            node.newLogWrite(node, logging, LogLevel.INFO, node.arguments, connect = true)
        }
    }

    // Tag logger.debug() calls
    each<CallExpression>(predicate = { it.name.localName == "debug" }).with {
        node.findLogger()?.let { logging ->
            node.newLogWrite(node, logging, LogLevel.DEBUG, node.arguments, connect = true)
        }
    }

    // Tag logger.warn() calls
    each<CallExpression>(predicate = { it.name.localName == "warn" }).with {
        node.findLogger()?.let { logging ->
            node.newLogWrite(node, logging, LogLevel.WARN, node.arguments, connect = true)
        }
    }

    // Tag logger.error() calls
    each<CallExpression>(predicate = { it.name.localName == "error" }).with {
        node.findLogger()?.let { logging ->
            node.newLogWrite(node, logging, LogLevel.ERROR, node.arguments, connect = true)
        }
    }

    // Tag logger.setLevel(X) calls
    each<CallExpression>(predicate = { it.name.localName == "setLevel" }).with {
        node.findLogger()?.let { logging ->
            logging.logLevelThreshold =
                when (node.arguments.firstOrNull()?.code) {
                    "logging.NOTSET" ->
                        LogLevel
                            .UNKNOWN // TODO: We should look for another logger. If none is found,
                    // all events are logged.
                    "logging.DEBUG" -> LogLevel.DEBUG
                    "logging.INFO" -> LogLevel.INFO
                    "logging.WARNING" -> LogLevel.WARN
                    "logging.ERROR" -> LogLevel.ERROR
                    "logging.CRITICAL" -> LogLevel.CRITICAL
                    else -> LogLevel.DEBUG
                }
            logging
        }
    }
}

/** Find the corresponding logger for the given call expression by walking the DFG backwards. */
private fun CallExpression.findLogger(): Logging? {
    val callee = this.callee
    if (callee is MemberExpression) {
        val base = callee.base
        val fulfilledPaths =
            base
                .followPrevFullDFGEdgesUntilHit(
                    collectFailedPaths = false,
                    findAllPossiblePaths = false,
                ) {
                    it.overlays.any { overlay -> overlay is LogGet }
                }
                .fulfilled

        val foundLoggers =
            fulfilledPaths
                .map { path -> path.nodes.last() }
                .flatMap { it.overlays }
                .filterIsInstance<LogGet>()
                .map { it.linkedConcept }

        return foundLoggers.firstOrNull()
    }
    return null
}
