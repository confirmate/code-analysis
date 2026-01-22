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
    assumptions {
        decisions {
            /** We assume that the start of the inference system is ok. */
            accept {
                it.assumptionType == AssumptionType.InferenceAssumption &&
                    it.message.startsWith(
                        "We assume that the start of inference is a record, namespace or translation unit."
                    )
            }

            accept {
                it.assumptionType == AssumptionType.DataFlowAssumption &&
                    it.message.startsWith(
                        "We assume that the initiator is always logged via the same argument, i.e., the logging routine does not hold the initiator in different arguments on different paths reaching it."
                    )
            }
        }
    }
}
