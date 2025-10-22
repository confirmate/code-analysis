package de.fraunhofer.aisec.confirmate.queries.catalogs.configs

import de.fraunhofer.aisec.confirmate.queries.catalogs.RequirementsCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.SecureConfigurationsCatalog
import de.fraunhofer.aisec.cpg.graph.types.IntegerType
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.QueryTreeOperators

class DefaultConfig(): RequirementsCatalog(), SecureConfigurationsCatalog {

    override val secureConfigurations: MutableMap<String, List<String>> = hashMapOf(
        // Transport Encryption
        "ENABLE_TLS" to listOf("true"),
        "HASH_ALGORITHM" to listOf("SHA256"),

        // Authentication and Roles
        "ALLOW_ANONYMOUS" to listOf("false"),
        "ADMIN_ROLE" to listOf("root"),
        "DEFAULT_USER_PERMISSIONS" to listOf("read_only"),
        "MAX_LOGIN_ATTEMPTS" to listOf("5"),
        "PASSWORD_EXPIRY_DAYS" to listOf("90"),

        // Logging
        "DEBUG_MODE" to listOf("false"),
        "LOG_LEVEL" to listOf("INFO"),
        "INCLUDE_STACK_TRACE" to listOf("false"),
        "LOG_SENSITIVE_DATA" to listOf("false"),

        // Session and Token configuration
        "SESSION_TIMEOUT" to listOf("1800"),
        "SECURE_COOKIES" to listOf("true"),
        "SESSION_COOKIE_SECURE" to listOf("true"),
        "CSRF_PROTECTION" to listOf("true"),
        "TOKEN_EXPIRY" to listOf("3600"),
        "REMEMBER_ME_ENABLED" to listOf("false"),

        // Updates and Isolation
        "AUTO_UPDATE" to listOf("true"),
        "ALLOW_REMOTE_CONFIG" to listOf("false"),
        "SANDBOX_MODE" to listOf("true"),
    )


}