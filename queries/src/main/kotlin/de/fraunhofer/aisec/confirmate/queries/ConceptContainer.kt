/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.Concept
import de.fraunhofer.aisec.cpg.graph.concepts.Operation
import de.fraunhofer.aisec.cpg.graph.concepts.auth.Authentication
import de.fraunhofer.aisec.cpg.graph.concepts.auth.Authorization
import de.fraunhofer.aisec.cpg.graph.concepts.auth.RequestContext
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpEndpoint
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpMethod
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration

/*
This file contains a collection of Concepts and Operations which provide additional information compared to the basic CPG model.
They can be used to build more complex queries and encode more information which is required for evaluating the CRA and the
SOTA of certain regulations/standards/requirements/guidelines. The classes should be migrated to the CPG project if they are generally useful or to the ontology.
 */

/**
 * This class represents a symmetric cipher used for encryption and decryption of data. It extends
 * the [Cipher] class and includes additional properties specific to symmetric ciphers, such as the
 * mode of operation, tag size, IV size, and the initialization vector itself.
 */
class SymmetricCipher(underlyingNode: Node?) : Cipher(underlyingNode) {
    /** The modus of operation, e.g., "GCM", "CBC", "CCM", ... */
    var modus: String? = null
    /** The size of an authentication tag in bits, if present for the given [modus]. */
    var tagSize: Int? = null
    /** The size of the initialization vector (IV) in bits, if present for the given [modus]. */
    var ivSize: Int? = null
    /**
     * The initialization vector (IV) itself, if present for the given [modus]. TODO: Maybe move
     * this to the encrypt operation??
     */
    var iv: Node? = null
}

/**
 * This class represents a hybrid encryption and decryption scheme. These consist of a symmetric
 * encryption scheme, a key exchange.
 */
class HybridCipher(underlyingNode: Node?) : Cipher(underlyingNode) {
    var symmetricCipher: SymmetricCipher? = null

    var keyExchange: Cipher? = null // TODO: Maybe we do not need this here.

    var hashFunction: HashFunction? = null
}

class HashFunction(underlyingNode: Node?) : Concept(underlyingNode) {
    var hashFunctionName: String? = null
    var outputSize: Int? = null
}

class HttpEndpointWithProtocol(
    underlyingNode: FunctionDeclaration? = null,
    httpMethod: HttpMethod,
    path: String,
    arguments: List<Node>,
    authentication: Authentication?,
    authorization: Authorization?,
    requestContext: RequestContext?,
) :
    HttpEndpoint(
        underlyingNode = underlyingNode,
        httpMethod = httpMethod,
        path = path,
        arguments = arguments,
        authentication = authentication,
        authorization = authorization,
        requestContext = requestContext,
    ) {
    var protocol: CommunicationProtocol? = null
}

class CommunicationProtocol(
    val protocolName: String,
    val versionNumber: String,
    val cipherSuites: List<CipherSuite>?,
    underlyingNode: Node?,
) : Concept(underlyingNode)

/**
 * This class represents a Random Number Generator (RNG) concept in the code property graph (CPG).
 * It extends the [Concept] class and is used to identify nodes in the CPG that are associated with
 * random number generation functionality.
 */
class RNG(underlyingNode: Node?) : Concept(underlyingNode)

/**
 * This class represents an operation that retrieves random numbers from a random number generator
 * (RNG). It extends the [Operation] class and is associated with an [RNG] concept to indicate that
 * it performs a get operation on the RNG.
 */
class RngGet(underlyingNode: Node?, val rng: RNG) : Operation(underlyingNode, rng)
