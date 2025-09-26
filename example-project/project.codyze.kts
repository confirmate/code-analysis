/*
 * This file is part of the Confirmate project.
 */
@file:OptIn(ExperimentalUuidApi::class)

import de.fraunhofer.aisec.confirmate.queries.catalogs.german.BSI_TR02102
import de.fraunhofer.aisec.confirmate.queries.cra.*
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
                name = "X.1.1.6"
                description =
                    "Products with digital elements shall protect the confidentiality of stored, transmitted or otherwise processed data, personal or other, such as by encrypting relevant data at rest or in transit by state of the art mechanisms, and by using other technical means;"

                fulfilledBy {
                    with(BSI_TR02102()) {
                        dataEncryptedBeforePersisting() and dataInTransitEncrypted()
                    }
                }
            }
        }
    }
}
