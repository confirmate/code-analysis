/*
 * This file is part of the Confirmate project.
 */
@file:OptIn(ExperimentalUuidApi::class)

import kotlin.uuid.ExperimentalUuidApi

include {
    // No includes so far
}

project {
    name = "Example CRA project"

    toe {
        name = "Example project"
        version = "1.0.0"

        architecture {
            modules {
                module("test-module") {
                    directory = "src/main/kotlin/resources/toe"
                    include("toe")
                }
            }
        }
    }

    requirements {
        category("CRA") {
            requirement {
                name = "X.1.1.1"

                fulfilledBy { true eq true }
            }
        }
    }
}
