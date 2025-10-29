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
package de.fraunhofer.aisec.confirmate.queries.catalogs.configs

import de.fraunhofer.aisec.codyze.catalogs.SotaRequirementsCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.SecureConfigurationsCatalog

class DefaultConfig() : SotaRequirementsCatalog(), SecureConfigurationsCatalog {

    override val secureConfigurations: MutableMap<String, List<String>> =
        hashMapOf(
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
