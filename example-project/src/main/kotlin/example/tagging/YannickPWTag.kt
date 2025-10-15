/*
 * This file is part of the Confirmate project.
 */
package example.tagging

/*
fun TaggingContext.tagFernetencrypt() {
    each<MemberCallExpression>(predicate = { it.name.localName.contains("encrypt") }).with {
        Fernet(underlyingNode = node).apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
    }
}

fun TaggingContext.tagPersonaldata() {
    each<AssignExpression>(predicate = { it.comment.toString().contains("@PersonalData") }).with {
        val ref = node.lhs.first()
        Personaldata(underlyingNode = ref).apply {
            this.codeAndLocationFrom(ref)
            this.name = Name(ref.name.localName)
        }
    }
}

fun TaggingContext.tagPassword() {
    each<Reference>(
            predicate = {
                it.name.localName.contains("password") && it.location.toString().contains("client")
            }
        )
        .with {
            Password(underlyingNode = node).apply {
                this.codeAndLocationFrom(node)
                this.name = Name(node.name.localName)
            }
        }
}

fun TaggingContext.tagUser() {
    each<Reference>(predicate = { it.name.localName == "user" }).with {
        User(underlyingNode = node).apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
    }
}

fun TaggingContext.tagHttp() {
    val httpMethods = setOf("get", "post", "delete")
    each<FunctionDeclaration>(
            predicate = {
                it.annotations.any { annotation -> annotation.name.localName in httpMethods }
            }
        )
        .with {
            val path = node.annotations.first().members[0].value?.evaluate() as? String ?: ""
            val httpmethods = node.annotations.first().name.localName.asHttpMethod()
            val httpEndpoint =
                HttpEndpoint(
                        underlyingNode = node,
                        httpMethod = httpmethods,
                        path = path,
                        arguments = node.parameters,
                        authentication = null,
                        authorization = null,
                        requestContext = null,
                    )
                    .apply {
                        this.codeAndLocationFrom(node)
                        this.name = Name(node.name.localName)
                    }
            httpEndpoint
        }
}

val createdHttpRequest = mutableListOf<HttpRequest>()

fun TaggingContext.tagHTTPRequests() {
    val httpMethods = setOf("get", "post", "delete")

    each<CallExpression>(
            predicate = {
                httpMethods.any { m -> it.name.localName.contains(m) } &&
                    it.name.contains("requests.") || it.name.parent.toString().contains("requests.")
            }
        )
        .with {
            val urlpath =
                when (val url = this.node.arguments.firstOrNull()) {
                    is Literal<*> -> url.value
                    is Reference -> url.evaluate()
                    is BinaryOperator -> url.lhs.value.value
                    else -> {
                        null
                    }
                }
            val httpmethods = node.name.localName.asHttpMethod()
            val httpRequest =
                HttpRequest(
                        underlyingNode = node,
                        url = urlpath.toString(),
                        arguments = node.arguments,
                        httpMethod = httpmethods,
                        concept = HttpClient(underlyingNode = node, authentication = null),
                    )
                    .apply {
                        this.codeAndLocationFrom(node)
                        this.name = Name(node.name.localName)
                    }
            createdHttpRequest += httpRequest
            httpRequest
        }
}

fun String.asHttpMethod(): HttpMethod =
    when (this.lowercase()) {
        "get" -> HttpMethod.GET
        "post" -> HttpMethod.POST
        else -> HttpMethod.DELETE
    }

fun TaggingContext.tagTaint() {
    each<Reference>(predicate = { it.name.localName == "input" }).with {
        Userinput(underlyingNode = node).apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
    }
}

fun TaggingContext.tagDBExecute() {
    each<MemberCallExpression>(predicate = { it.name.localName == "execute" }).with {
        DBExe(underlyingNode = node).apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
    }
}
*/
