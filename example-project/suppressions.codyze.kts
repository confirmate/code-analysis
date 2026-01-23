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
package example

project {
    suppressions {
        /** Suppress the warning about missing opt-out checks on logging calls. */
        queryTree(
            { qt: QueryTree<Boolean> ->
                qt.stringRepresentation ==
                    "Some logging statements are not controlled by an opt-out mechanism based on user input."
            } to true
        )

        /** Registration does not require prior authentication. */
        queryTree(
            { qt: QueryTree<Boolean> ->
                qt.stringRepresentation == "There endpoint register_POST is not authenticated."
            } to true
        )
    }
}
