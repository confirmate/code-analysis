/*
 * This file is part of the Privacy-Policy-Generator project
 */
package de.fraunhofer.aisec.example.tagging

import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpEndpoint
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpMethod
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

/**
 * Tags Flask routes based on route decorators on [FunctionDeclaration]s.
 *
 * Supports both the `@app.route` decorator and the HTTP method shortcuts (`@app.get`, `@app.post`, etc.).
 *
 * ```python
 * @app.route('/api/users', methods=['GET', 'POST'])
 * def users():
 *     pass
 *
 * @app.route('/api/user/<id>')  # Default: GET
 * def get_user(id):
 *     pass
 *
 * @app.get('/health')
 * def health():
 *     pass
 *
 * @app.post('/login')
 * def login():
 *     pass
 * ```
 *
 * @see https://flask.palletsprojects.com/en/latest/api/#flask.Flask.route
 * @see https://flask.palletsprojects.com/en/latest/api/#flask.Flask.get
 */
fun TaggingContext.flaskEndpoints() {
    each<FunctionDeclaration>(
        predicate = { func ->
            // Check if the function has a decorator that looks like a Flask route
            // Flask uses @app.route() or @app.get(), @app.post(), etc
            func.annotations.any { annotation ->
                val name = annotation.name.toString()
                name == "app.route" || parseHttpMethod(name) != null
            }
        }
    )
        .with {
            val func = node

            // Find the route decorator (@app.route or @app.get/@app.post/etc.)
            val routeAnnotation = func.annotations
                .firstOrNull {
                    val name = it.name.toString()
                    name == "app.route" || parseHttpMethod(name) != null
                }
                ?: return@with null

            val annotationName = routeAnnotation.name.toString()

            // Extract the path from the first argument
            val path = routeAnnotation.members.firstOrNull()?.value?.evaluate() as? String ?: ""

            // Extract HTTP methods:
            // - For @app.get/@app.post/etc., the method is implicit in the decorator name
            // - For @app.route, extract from 'methods' keyword argument
            val httpMethods = parseHttpMethod(annotationName)?.let { listOf(it) }
                ?: extractHttpMethods(routeAnnotation)

            // Find request.get_json() or request.json call within the function
            val payloadCall = findRequestPayloadCall(func)

            // Create HttpEndpoint for each HTTP method
            // Flask allows multiple methods per route (only relevant for @app.route)
            val endpoints = httpMethods.map { httpMethod ->
                HttpEndpoint(
                    underlyingNode = func,
                    path = path,
                    authorization = null,
                    rateLimiting = null,
                    maxInputSize = null,
                    userInput = mutableListOf(),
                    handler = null,
                    method = httpMethod,
                    url = path,
                    authenticity = null,
                    httpRequestContext = null,
                    proxyTarget = null,
                    transportEncryption = null,
                ).apply {
                    this.codeAndLocationFrom(func)
                    this.name = Name("${func.name.localName}_${httpMethod.name}")
                    this.nextDFG += func.parameters
                    payloadCall?.let { this.nextDFG += it }
                }
            }

            endpoints.firstOrNull()
        }
}

/**
 * Finds a call to `request.get_json()` or access to `request.json` within a [FunctionDeclaration].
 *
 * To access the JSON payload from incoming HTTP requests, Flask uses:
 * - `request.get_json()`
 * - `request.json`
 *
 *
 * @see https://flask.palletsprojects.com/en/latest/api/#flask.Request.get_json
 * @see https://flask.palletsprojects.com/en/latest/api/#flask.Request.json
 */
private fun findRequestPayloadCall(func: FunctionDeclaration): Node? {
    // Find request.get_json() call
    func.allChildren<MemberCallExpression>()
        .firstOrNull { call ->
            call.name.localName == "get_json" &&
                (call.base as? Reference)?.name?.localName == "request"
        }?.let { return it }

    // Find request.json property access
    return func.allChildren<MemberExpression>()
        .firstOrNull { member ->
            member.name.localName == "json" &&
                (member.base as? Reference)?.name?.localName == "request"
        }
}

/**
 * Extracts HTTP methods from Flask route decorator.
 *
 * Looks for the 'methods' keyword argument: `methods=['GET', 'POST']`
 * If not found, defaults to GET (Flask's default behavior).
 */
private fun extractHttpMethods(annotation: Annotation): List<HttpMethod> {
    val methodsMember = annotation.members
        .firstOrNull { it.name.toString() == "methods" }
        ?: return listOf(HttpMethod.GET) // Default to GET if no methods specified

    return when (val value = methodsMember.value) {
        is InitializerListExpression -> {
            value.initializers
                .mapNotNull { initializer ->
                    val methodString = (initializer as? Literal<*>)?.value as? String
                    methodString?.let { parseHttpMethod(it) }
                }
                .ifEmpty { listOf(HttpMethod.GET) }
        }
        else -> listOf(HttpMethod.GET)
    }
}

/**
 * Parses a string to an HttpMethod.
 * Supports method names ("GET", "POST") and Flask decorator shortcuts ("app.get", "app.post").
 */
private fun parseHttpMethod(method: String): HttpMethod? {
    return when (method.uppercase()) {
        "GET", "APP.GET" -> HttpMethod.GET
        "POST", "APP.POST" -> HttpMethod.POST
        "PUT", "APP.PUT" -> HttpMethod.PUT
        "DELETE", "APP.DELETE" -> HttpMethod.DELETE
        "PATCH", "APP.PATCH" -> HttpMethod.PATCH
        "HEAD", "APP.HEAD" -> HttpMethod.HEAD
        "OPTIONS", "APP.OPTIONS" -> HttpMethod.OPTIONS
        else -> null
    }
}
