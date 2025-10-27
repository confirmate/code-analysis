/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs

/**
 * If an instance of a [de.fraunhofer.aisec.codyze.catalogs.SotaRequirementsCatalog] provides an
 * interpretation of the state-of-the-art regarding secure configurations, it should implement this
 * interface to signal that it provides such an interpretation and fill values into the secure
 * configurations map.
 */
interface SecureConfigurationsCatalog {

    /**
     * This Mapping of secure configuration keys maps to a list of allowed values. A specification
     * of this catalog only needs to set the values that are recommended or considered.
     */
    val secureConfigurations: MutableMap<String, List<String>>

    /**
     * This function checks if a configuration is secure. This default implementation checks if the
     * configName has an entry in the Map. If there is no such entry, no restriction was placed on
     * the value of a configuration. If such an entry exists, the value must be amongst the allowed
     * values in the list. For mor complex or catalog adequate checks an implementing catalog may
     * override this function.
     */
    fun isSecureConfigValue(configName: String, configValue: String): Boolean {
        return !secureConfigurations.containsKey(configName) ||
            secureConfigurations.get(configName)?.contains(configValue) ?: false
    }
}
