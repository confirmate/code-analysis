<script lang="ts">
  import type { Evidence } from '$lib/types/evidence';
  import { getResourceData } from '$lib/types/evidence';
  import { findEvidenceByType, type ManufacturerInfo } from '$lib/utils/templateMapper';
  import Field from './Field.svelte';
  import Manual from './Manual.svelte';

  interface Props {
    evidences: Evidence[];
    manufacturer: ManufacturerInfo;
  }

  let { evidences, manufacturer }: Props = $props();

  const product = $derived(findEvidenceByType(evidences, 'product'));
  const contactPerson = $derived(findEvidenceByType(evidences, 'contactPerson'));
  const application = $derived(findEvidenceByType(evidences, 'application'));
  const memory = $derived(findEvidenceByType(evidences, 'memory'));
  const virtualMachine = $derived(findEvidenceByType(evidences, 'virtualMachine'));
  const sbomDocument = $derived(findEvidenceByType(evidences, 'sbomDocument'));
  const euDeclarationOfConformity = $derived(findEvidenceByType(evidences, 'euDeclarationOfConformity'));
  const coordinatedVulnerabilityDisclosurePolicy = $derived(findEvidenceByType(evidences, 'coordinatedVulnerabilityDisclosurePolicy'));
  const distributionOfUpdatesDocument = $derived(findEvidenceByType(evidences, 'distributionOfUpdatesDocument'));
  const productionAndMonitoringProcessDocument = $derived(findEvidenceByType(evidences, 'productionAndMonitoringProcessDocument'));
  const cyberSecurityRiskAssessmentDocument = $derived(findEvidenceByType(evidences, 'cyberSecurityRiskAssessmentDocument'));
  const monitoringProcedure = $derived(findEvidenceByType(evidences, 'monitoringProcedure'));

  const vmInternetAccessible = $derived(
    virtualMachine ? Boolean((getResourceData(virtualMachine) as { internetAccessibleEndpoint?: boolean } | null)?.internetAccessibleEndpoint) : false
  );
</script>

<div
  class="text-[13.5px] leading-[1.8] text-gray-600 tracking-[0.01em]
    [&_h2]:text-base [&_h2]:font-semibold [&_h2]:text-gray-900 [&_h2]:mt-10 [&_h2]:mb-2 [&_h2]:tracking-[-0.015em]
    [&_h3]:text-[13.5px] [&_h3]:font-semibold [&_h3]:text-gray-800 [&_h3]:mt-7 [&_h3]:mb-1
    [&_h4]:text-[13.5px] [&_h4]:font-medium [&_h4]:text-gray-700 [&_h4]:mt-5 [&_h4]:mb-1
    [&_p]:my-1
    [&_ul]:my-1 [&_ul]:pl-5 [&_ul]:list-disc
    [&_strong]:font-semibold [&_strong]:text-gray-600"
>
  <h2>Information and Instructions to the User</h2>

  <h3>1. Manufacturer</h3>
  <p><strong>Manufacturer name:</strong> <Manual value={manufacturer.name} placeholder="[MANUFACTURER NAME]" /></p>
  <p><strong>Registered trade name or trademark:</strong> <Manual value={manufacturer.tradeName} placeholder="[TRADE NAME / TRADEMARK]" /></p>
  <p><strong>Postal address:</strong> <Manual value={manufacturer.postalAddress} placeholder="[POSTAL ADDRESS]" /></p>
  <p>
    <strong>General contact email:</strong>
    {#if manufacturer.generalEmail}
      {manufacturer.generalEmail}
    {:else}
      <Field evidence={contactPerson} field="emailAddress" placeholder="[GENERAL CONTACT EMAIL]" />
    {/if}
  </p>
  <p>
    <strong>Security contact email:</strong>
    {#if manufacturer.securityEmail}
      {manufacturer.securityEmail}
    {:else}
      <Field evidence={contactPerson} field="emailAddress" placeholder="[SECURITY EMAIL]" />
    {/if}
  </p>
  <p><strong>Website:</strong> <Manual value={manufacturer.website} placeholder="[WEBSITE]" /></p>

  <h3>2. Vulnerability reporting</h3>
  <p>Information about vulnerabilities concerning this product may be reported to the manufacturer through the following single point of contact:</p>
  <p>
    <strong>Security contact:</strong>
    {#if manufacturer.securityEmail}
      {manufacturer.securityEmail}
    {:else}
      <Field evidence={contactPerson} field="emailAddress" placeholder="[SECURITY EMAIL]" />
    {/if}
  </p>
  <p><strong>Alternative reporting channel:</strong> <Manual value={manufacturer.securityPortalUrl} placeholder="[SECURITY PORTAL URL]" /></p>
  <p><strong>CVD policy:</strong> <Field evidence={coordinatedVulnerabilityDisclosurePolicy} field="raw" placeholder="[CVD POLICY URL]" /></p>
  <p>When reporting a vulnerability, the reporter should, where possible, provide:</p>
  <ul>
    <li>product name and version;</li>
    <li>affected component or interface, if known;</li>
    <li>description of the suspected vulnerability;</li>
    <li>conditions under which it can be reproduced;</li>
    <li>potential impact, if known;</li>
    <li>contact details for follow-up.</li>
  </ul>
  <p>The manufacturer will handle vulnerability reports in accordance with its coordinated vulnerability disclosure policy.</p>

  <h3>3. Product identification</h3>
  <p><strong>Product name:</strong> <Field evidence={product} field="name" placeholder="[PRODUCT NAME]" /></p>
  <p><strong>Product type:</strong> <Field evidence={product} field="type" placeholder="[PRODUCT TYPE]" /></p>
  <p><strong>TODO: Model:</strong> <Manual value={manufacturer.model} placeholder="[MODEL]" /></p>
  <p><strong>Version / firmware / software version:</strong> <Field evidence={product} field="programmingVersion" placeholder="[VERSION]" /></p>
  <p><strong>Unique product identifier, if applicable:</strong> <Field evidence={product} field="id" placeholder="[SERIAL NUMBER / BATCH / DEVICE ID]" /></p>

  <h3>4. Intended purpose and essential information</h3>
  <p><strong>Intended purpose:</strong></p>
  <p>This product is intended for <Field evidence={product} field="purpose" placeholder="[INTENDED PURPOSE]" />.</p>
  <p><strong>Essential functionalities:</strong></p>
  <p>The product provides the following essential functionalities: <Field evidence={product} field="description" placeholder="[ESSENTIAL FUNCTIONALITIES]" />.</p>
  <p><strong>Security properties:</strong></p>
  <p>TODO: The product includes the following security-relevant properties, where applicable: authentication, access control, update capability, logging, and protection of data in storage and transmission.</p>

  <h3>5. Foreseeable circumstances of use or misuse creating cybersecurity risks</h3>
  <p>Cybersecurity risks may arise in particular where:</p>
  <ul>
    <li>the product is used outside its intended purpose;</li>
    <li>the product is operated with outdated software or firmware;</li>
    <li>default or weak credentials are used;</li>
    <li>security settings are changed without authorization or sufficient knowledge;</li>
    <li>the product is connected to untrusted networks or systems;</li>
    <li>unsupported third-party software, hardware, or services are used.</li>
  </ul>

  <h3>6. EU declaration of conformity</h3>
  <p>Where applicable, the EU declaration of conformity is available at: <Field evidence={euDeclarationOfConformity} field="raw" placeholder="[URL]" /></p>

  <h3>7. Technical security support</h3>
  <p><strong>Type of technical security support:</strong></p>
  <p>The manufacturer provides security-relevant updates, vulnerability handling, and security-related user support for this product.</p>
  <p><strong>End date of support period: </strong><Field evidence={product} field="supportEnds" placeholder="[END DATE]" /></p>
  <p>After this date, security updates or vulnerability handling for the product may no longer be provided.</p>

  <h3>8. Detailed instructions and information</h3>

  <h4>(a) Measures for secure use</h4>
  <p>For secure initial commissioning and secure use throughout the lifetime of the product, the user should:</p>
  <ul>
    <li>install the product in accordance with the manufacturer's instructions;</li>
    <li>ensure the product is updated to the latest available version before or during initial use;</li>
    <li>replace default credentials or set strong credentials where applicable;</li>
    <li>restrict access to authorized users only;</li>
    <li>use the product only in the intended operating environment;</li>
    <li>install security updates without undue delay;</li>
    <li>keep connected systems and interfaces appropriately secured.</li>
  </ul>

  <h4>(b) Effect of changes on the security of data</h4>
  <p>Changes to the product, including configuration changes, software changes, connection of external systems, or changes to user access settings, may affect the security of data. Such changes may reduce the confidentiality, integrity, or availability of data if they are not properly implemented and controlled.</p>

  <h4>(c) Installation of security-relevant updates</h4>
  <p>Security-relevant updates can be installed by <Manual value={manufacturer.automaticUpdateMethod} placeholder="[AUTOMATIC UPDATE FUNCTION / ADMIN INTERFACE / MANUAL INSTALLATION METHOD]" />.</p>
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >TODO:Where applicable, the user should:

  check whether automatic security updates are enabled;
  verify that the product is connected to the required update source;
  follow the update instructions provided in the user interface or at [UPDATE INSTRUCTIONS URL];
  restart the product if required after installation</span>
  <h4>(d) Secure decommissioning</h4>
  <p>Before disposal, resale, transfer, return, or permanent withdrawal from service, the user should:</p>
  <ul>
    <li>back up data, where necessary;</li>
    <li>remove the product from user accounts, management systems, or connected services;</li>
    <li>revoke or delete credentials, tokens, or access rights associated with the product;</li>
    <li>reset the product to factory settings or use the provided secure erase function, where available;</li>
    <li>verify that user data has been removed from the product.</li>
  </ul>
  <p><strong>Data removal method:</strong> <Manual value={manufacturer.dataRemovalMethod} placeholder="[FACTORY RESET / SECURE WIPE / ADMIN FUNCTION / INSTRUCTIONS URL]" /></p>

  <h4>(e) Turning off automatic security updates</h4>
  <p>Where automatic installation of security updates is enabled by default, this setting can be turned off through: <Manual value={manufacturer.disableUpdatesPath} placeholder="[SETTINGS PATH / ADMIN INTERFACE / INSTRUCTIONS URL]" /></p>
  <p>Disabling automatic security updates may increase cybersecurity risks. The manufacturer recommends keeping automatic security updates enabled.</p>

  <h4>(f) Information for integration into other products</h4>
  <p>Where this product is intended for integration into other products with digital elements, the integrator should use the product only in accordance with the integration, interface, configuration, and security instructions provided by the manufacturer.</p>
  <p>Integration information is available at: <Manual value={manufacturer.integrationDocUrl} placeholder="[INTEGRATION DOCUMENT / URL]" /></p>
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >TODO:The integrator should in particular consider:

  supported interfaces and configurations;
  required security settings;
  update and patch management;
  access control and authentication measures;
  any limitations or assumptions described in the integration documentation.</span>

  <h2>TECHNICAL DOCUMENTATION</h2>

  <h3>1. General description of the product with digital elements</h3>

  <h4>1.1 Intended purpose</h4>
  <p>
    The product with digital elements
    <Field evidence={product} field="name" placeholder="[PRODUCT NAME]" />
    is intended for
    <Field evidence={product} field="purpose" placeholder="[INTENDED PURPOSE]" />.
  </p>
  <p>It is intended to be used in <Field evidence={product} field="contextOfUse" placeholder="[INTENDED ENVIRONMENT / CONTEXT OF USE]" />.</p>

  <h4>1.2 Software versions affecting compliance</h4>
  <p>The following software / firmware versions affect compliance with the essential cybersecurity requirements:</p>
  <p>Product version: <Field evidence={product} field="programmingVersion" placeholder="[VERSION]" /></p>
  {#if application}
    <p>
      Application:
      <Field evidence={application} field="name" placeholder="[NAME]" />
      (<Field evidence={application} field="programmingLanguage" placeholder="" />
      <Field evidence={application} field="programmingVersion" placeholder="[VERSION]" />)
    </p>
  {/if}
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >TODO:Product version: [VERSION]
Firmware version: [VERSION]
Software components relevant to compliance: [NAME / VERSION] </span>

  <h4>1.3 External features, marking and internal layout</h4>
  <p>Not applicable.</p>
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >TODO:Where the product is a hardware product, photographs or illustrations showing its external features, marking and internal layout are provided in:
  [REFERENCE / ANNEX / APPENDIX]

  If not applicable, state: Not applicable.</span>

  <h4>1.4 User information and instructions</h4>
  <p>The user information and instructions for the product are provided in the accompanying documentation.</p>
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >TODO: [DOCUMENT TITLE / REFERENCE / VERSION] </span>

  <h3>2. Description of design, development, production and vulnerability handling processes</h3>

  <h4>2.1 Design and development</h4>
  <p>The product has been designed and developed by <Manual value={manufacturer.name} placeholder="[MANUFACTURER NAME]" /> in accordance with documented design and development processes.</p>
  <p>The product consists of the following main elements:</p>
  <ul>
    <li>
      Hardware components:
      {#if memory}
        <Field evidence={memory} field="name" placeholder="[DESCRIPTION]" />
        (<Field evidence={memory} field="mode" placeholder="" />)
      {:else}
        <Manual value="" placeholder="[DESCRIPTION]" />
      {/if}
    </li>
    <li>
      Software / firmware components:
      {#if application}
        <Field evidence={application} field="name" placeholder="[DESCRIPTION]" />
        (<Field evidence={application} field="programmingLanguage" placeholder="" />
        <Field evidence={application} field="programmingVersion" placeholder="" />)
      {:else}
        <Manual value="" placeholder="[DESCRIPTION]" />
      {/if}
    </li>
    <li>
      Interfaces / connections:
      {#if virtualMachine}
        <Field evidence={virtualMachine} field="name" placeholder="[DESCRIPTION]" />
        {vmInternetAccessible ? '(internet-accessible)' : '(not internet-accessible)'}
      {:else}
        <Manual value="" placeholder="[DESCRIPTION]" />
      {/if}
    </li>
  </ul>
  <span
          class="inline px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >
    TODO: Where applicable, drawings, schemes and system architecture documentation are provided in:
[REFERENCE / DOCUMENT TITLE]
  </span>
  <span
          class="px-1.5 py-0.5 text-red-500 ring-1 ring-red-500 text-[12px] font-medium"
  >
    TODO: The components interact as follows:
    [SHORT DESCRIPTION OF HOW THE COMPONENTS BUILD ON OR FEED INTO EACH OTHER AND INTEGRATE INTO THE OVERALL PROCESSING]
  </span>


  <h4>2.2 Vulnerability handling</h4>
  <p>The manufacturer has established vulnerability handling processes for the product, including:</p>
  <ul>
    <li>receipt and assessment of vulnerability reports;</li>
    <li>documentation of relevant components;</li>
    <li>remediation and release of security updates;</li>
    <li>communication through the vulnerability contact point.</li>
  </ul>
  <p>Relevant documentation:</p>
  <ul>
    <li><strong>Software bill of materials (SBOM): <Field evidence={sbomDocument} field="name" placeholder="[REFERENCE]" /></strong></li>
    <li><strong>Coordinated vulnerability disclosure policy: <Field evidence={coordinatedVulnerabilityDisclosurePolicy} field="name" placeholder="[REFERENCE / URL]" /></strong></li>
    <li>
      <strong>
      Vulnerability reporting contact:
      {#if manufacturer.securityEmail}
        {manufacturer.securityEmail}
      {:else}
        <Field evidence={contactPerson} field="emailAddress" placeholder="[SECURITY CONTACT EMAIL / PORTAL]" />
      {/if}
      </strong></li>
    <li><strong>Secure distribution of updates: <Field evidence={distributionOfUpdatesDocument} field="name" placeholder="[SHORT DESCRIPTION / REFERENCE]" /></strong></li>
  </ul>

  <h4>2.3 Production, monitoring and validation</h4>
  <p>The manufacturer has established production and monitoring processes relevant to the cybersecurity of the product, including release control, monitoring, and validation activities.</p>
  <p>Relevant records or procedures are provided in: <Field evidence={productionAndMonitoringProcessDocument} field="name" placeholder="[REFERENCE / DOCUMENT TITLE]" /></p>
  {#if monitoringProcedure}
    <p>
      Monitoring interval: every
      <Field evidence={monitoringProcedure} field="intervalMonths" placeholder="[INTERVAL]" />
      months (<Field evidence={monitoringProcedure} field="name" placeholder="" />)
    </p>
  {/if}

  <h3>3. Cybersecurity risk assessment</h3>
  <p>A cybersecurity risk assessment has been carried out for the product.</p>
  <p>The assessment considers the intended purpose, intended environment, interfaces, dependencies, and reasonably foreseeable misuse of the product. It identifies relevant cybersecurity risks and the measures applied to address the applicable essential cybersecurity requirements. </p>
  <p>Risk assessment reference: <Field evidence={cyberSecurityRiskAssessmentDocument} field="name" placeholder="[DOCUMENT TITLE / REFERENCE / VERSION]" /></p>

  <h3>4. Information relevant to determination of the support period</h3>
  <p>Support period / end date: <Field evidence={product} field="supportEnds" placeholder="[END DATE]" /></p>

  <h3>5. Harmonised standards, common specifications, or European cybersecurity certification schemes applied</h3>
  <p>No harmonised standards, common specifications, or European cybersecurity certification schemes have been applied.</p>
</div>

