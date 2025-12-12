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

import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

/**
 * Tags PBKDF2 (Password-Based Key Derivation Function 2) usage as Identity with password policy
 * enabled.
 */
fun TaggingContext.tagPbkdf2AsIdentity() {
    // TODO: Check again if this makes sense; The current approach would make more sense when using PasswordBasedAuthentication.
    //  Since the Identity concept seems more high-level, another idea would be to tag against a config value
    each<CallExpression>("hashlib.pbkdf2_hmac").with {
        // TODO: blocked because of StackOverFlowError -> problem with hashCode
        IdentityWithPasswordPolicy(disablePasswordPolicy = false, underlyingNode = null).apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
    }
}

fun TaggingContext.tagIdentity() {
    tagPbkdf2AsIdentity()
}
