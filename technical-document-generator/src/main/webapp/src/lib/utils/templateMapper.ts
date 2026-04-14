import type { Evidence } from '$lib/types/evidence';
import { getResourceType, getResourceData } from '$lib/types/evidence';

export interface ManufacturerInfo {
  name: string;
  tradeName: string;
  postalAddress: string;
  generalEmail: string;
  securityEmail: string;
  website: string;
  securityPortalUrl: string;
  model: string;
  serialNumber: string;
  automaticUpdateMethod: string;
  dataRemovalMethod: string;
  disableUpdatesPath: string;
  integrationDocUrl: string;
}

export function emptyManufacturerInfo(): ManufacturerInfo {
  return {
    name: '',
    tradeName: '',
    postalAddress: '',
    generalEmail: '',
    securityEmail: '',
    website: '',
    securityPortalUrl: '',
    model: '',
    serialNumber: '',
    automaticUpdateMethod: '',
    dataRemovalMethod: '',
    disableUpdatesPath: '',
    integrationDocUrl: ''
  };
}

function findByType(evidences: Evidence[], type: string): Record<string, any> | null {
  const ev = evidences.find((e) => getResourceType(e) === type);
  return ev ? getResourceData(ev) : null;
}

function val(value: any, placeholder: string): string {
  if (value === undefined || value === null || value === '') return placeholder;
  return String(value);
}

function formatDate(isoDate: string | undefined): string {
  if (!isoDate) return '[END DATE]';
  return new Date(isoDate).toISOString().split('T')[0];
}

export function generateDocument(evidences: Evidence[], manufacturer: ManufacturerInfo): string {
  const product = findByType(evidences, 'product');
  const contact = findByType(evidences, 'contactPerson');
  const app = findByType(evidences, 'application');
  const memory = findByType(evidences, 'memory');
  const vm = findByType(evidences, 'virtualMachine');
  const sbom = findByType(evidences, 'sbomDocument');
  const euDecl = findByType(evidences, 'euDeclarationOfConformity');
  const cvdp = findByType(evidences, 'coordinatedVulnerabilityDisclosurePolicy');
  const distUpdates = findByType(evidences, 'distributionOfUpdatesDocument');
  const prodMonitor = findByType(evidences, 'productionAndMonitoringProcessDocument');
  const riskAssessment = findByType(evidences, 'cyberSecurityRiskAssessmentDocument');
  const monitoring = findByType(evidences, 'monitoringProcedure');

  const contactEmail = contact?.emailAddress || '';
  const secEmail = manufacturer.securityEmail || contactEmail;

  const lines: string[] = [];

  // === INFORMATION AND INSTRUCTIONS TO THE USER ===

  lines.push('Information and Instructions to the User');
  lines.push('');
  lines.push('1. Manufacturer');
  lines.push('');
  lines.push(`Manufacturer name: ${val(manufacturer.name, '[MANUFACTURER NAME]')}`);
  lines.push(`Registered trade name or trademark: ${val(manufacturer.tradeName, '[TRADE NAME / TRADEMARK]')}`);
  lines.push(`Postal address: ${val(manufacturer.postalAddress, '[POSTAL ADDRESS]')}`);
  lines.push(`General contact email: ${val(manufacturer.generalEmail || contactEmail, '[GENERAL CONTACT EMAIL]')}`);
  lines.push(`Security contact email: ${val(secEmail, '[SECURITY EMAIL]')}`);
  lines.push(`Website: ${val(manufacturer.website, '[WEBSITE]')}`);
  lines.push('');

  lines.push('2. Vulnerability reporting');
  lines.push('');
  lines.push('Information about vulnerabilities concerning this product may be reported to the manufacturer through the following single point of contact:');
  lines.push('');
  lines.push(`Security contact: ${val(secEmail, '[SECURITY EMAIL]')}`);
  lines.push(`Alternative reporting channel: ${val(manufacturer.securityPortalUrl, '[SECURITY PORTAL URL]')}`);
  lines.push(`CVD policy: ${val(cvdp?.raw, '[CVD POLICY URL]')}`);
  lines.push('');
  lines.push('When reporting a vulnerability, the reporter should, where possible, provide:');
  lines.push('');
  lines.push('product name and version;');
  lines.push('affected component or interface, if known;');
  lines.push('description of the suspected vulnerability;');
  lines.push('conditions under which it can be reproduced;');
  lines.push('potential impact, if known;');
  lines.push('contact details for follow-up.');
  lines.push('The manufacturer will handle vulnerability reports in accordance with its coordinated vulnerability disclosure policy.');
  lines.push('');

  lines.push('3. Product identification');
  lines.push('');
  lines.push(`Product name: ${val(product?.name, '[PRODUCT NAME]')}`);
  lines.push(`Product type: ${val(product?.type, '[PRODUCT TYPE]')}`);
  lines.push(`Model: ${val(manufacturer.model, '[MODEL]')}`);
  lines.push(`Version / firmware / software version: ${val(product?.programmingVersion, '[VERSION]')}`);
  lines.push(`Unique product identifier, if applicable: ${val(manufacturer.serialNumber, '[SERIAL NUMBER / BATCH / DEVICE ID]')}`);
  lines.push('');

  lines.push('4. Intended purpose and essential information');
  lines.push('');
  lines.push('Intended purpose:');
  lines.push(`This product is intended for ${val(product?.purpose, '[INTENDED PURPOSE]')}.`);
  lines.push('');
  lines.push('Essential functionalities:');
  lines.push(`The product provides the following essential functionalities: ${val(product?.description, '[ESSENTIAL FUNCTIONALITIES]')}.`);
  lines.push('');
  lines.push('Security properties:');
  lines.push('The product includes the following security-relevant properties, where applicable: authentication, access control, update capability, logging, and protection of data in storage and transmission.');
  lines.push('');

  lines.push('5. Foreseeable circumstances of use or misuse creating cybersecurity risks');
  lines.push('');
  lines.push('Cybersecurity risks may arise in particular where:');
  lines.push('');
  lines.push('the product is used outside its intended purpose;');
  lines.push('the product is operated with outdated software or firmware;');
  lines.push('default or weak credentials are used;');
  lines.push('security settings are changed without authorization or sufficient knowledge;');
  lines.push('the product is connected to untrusted networks or systems;');
  lines.push('unsupported third-party software, hardware, or services are used.');
  lines.push('');

  lines.push('6. EU declaration of conformity');
  lines.push('');
  lines.push('Where applicable, the EU declaration of conformity is available at:');
  lines.push(val(euDecl?.raw, '[URL]'));
  lines.push('');

  lines.push('7. Technical security support');
  lines.push('');
  lines.push('Type of technical security support:');
  lines.push('The manufacturer provides security-relevant updates, vulnerability handling, and security-related user support for this product.');
  lines.push('');
  lines.push('End date of support period:');
  lines.push(formatDate(product?.supportEnds));
  lines.push('');
  lines.push('After this date, security updates or vulnerability handling for the product may no longer be provided.');
  lines.push('');

  lines.push('8. Detailed instructions and information');
  lines.push('');
  lines.push('(a) Measures for secure use');
  lines.push('');
  lines.push('For secure initial commissioning and secure use throughout the lifetime of the product, the user should:');
  lines.push('');
  lines.push('install the product in accordance with the manufacturer\'s instructions;');
  lines.push('ensure the product is updated to the latest available version before or during initial use;');
  lines.push('replace default credentials or set strong credentials where applicable;');
  lines.push('restrict access to authorized users only;');
  lines.push('use the product only in the intended operating environment;');
  lines.push('install security updates without undue delay;');
  lines.push('keep connected systems and interfaces appropriately secured.');
  lines.push('');
  lines.push('(b) Effect of changes on the security of data');
  lines.push('');
  lines.push('Changes to the product, including configuration changes, software changes, connection of external systems, or changes to user access settings, may affect the security of data. Such changes may reduce the confidentiality, integrity, or availability of data if they are not properly implemented and controlled.');
  lines.push('');
  lines.push('(c) Installation of security-relevant updates');
  lines.push('');
  lines.push(`Security-relevant updates can be installed by ${val(manufacturer.automaticUpdateMethod, '[AUTOMATIC UPDATE FUNCTION / ADMIN INTERFACE / MANUAL INSTALLATION METHOD]')}.`);
  lines.push('');
  lines.push('(d) Secure decommissioning');
  lines.push('');
  lines.push('Before disposal, resale, transfer, return, or permanent withdrawal from service, the user should:');
  lines.push('');
  lines.push('back up data, where necessary;');
  lines.push('remove the product from user accounts, management systems, or connected services;');
  lines.push('revoke or delete credentials, tokens, or access rights associated with the product;');
  lines.push('reset the product to factory settings or use the provided secure erase function, where available;');
  lines.push('verify that user data has been removed from the product.');
  lines.push(`Data removal method: ${val(manufacturer.dataRemovalMethod, '[FACTORY RESET / SECURE WIPE / ADMIN FUNCTION / INSTRUCTIONS URL]')}`);
  lines.push('');
  lines.push('(e) Turning off automatic security updates');
  lines.push('');
  lines.push(`Where automatic installation of security updates is enabled by default, this setting can be turned off through: ${val(manufacturer.disableUpdatesPath, '[SETTINGS PATH / ADMIN INTERFACE / INSTRUCTIONS URL]')}`);
  lines.push('');
  lines.push('Disabling automatic security updates may increase cybersecurity risks. The manufacturer recommends keeping automatic security updates enabled.');
  lines.push('');
  lines.push('(f) Information for integration into other products');
  lines.push('');
  lines.push('Where this product is intended for integration into other products with digital elements, the integrator should use the product only in accordance with the integration, interface, configuration, and security instructions provided by the manufacturer.');
  lines.push('');
  lines.push(`Integration information is available at: ${val(manufacturer.integrationDocUrl, '[INTEGRATION DOCUMENT / URL]')}`);
  lines.push('');
  lines.push('');

  // === TECHNICAL DOCUMENTATION ===

  lines.push('TECHNICAL DOCUMENTATION');
  lines.push('');
  lines.push('1. General description of the product with digital elements');
  lines.push('');
  lines.push('1.1 Intended purpose');
  lines.push('');
  lines.push(`The product with digital elements ${val(product?.name, '[PRODUCT NAME]')} is intended for ${val(product?.purpose, '[INTENDED PURPOSE]')}.`);
  lines.push(`It is intended to be used in ${val(product?.contextOfUse, '[INTENDED ENVIRONMENT / CONTEXT OF USE]')}.`);
  lines.push('');
  lines.push('1.2 Software versions affecting compliance');
  lines.push('');
  lines.push('The following software / firmware versions affect compliance with the essential cybersecurity requirements:');
  lines.push('');
  lines.push(`Product version: ${val(product?.programmingVersion, '[VERSION]')}`);
  if (app) {
    lines.push(`Application: ${val(app.name, '[NAME]')} (${val(app.programmingLanguage, '')} ${val(app.programmingVersion, '[VERSION]')})`);
  }
  lines.push('');
  lines.push('1.3 External features, marking and internal layout');
  lines.push('');
  lines.push('Not applicable.');
  lines.push('');
  lines.push('1.4 User information and instructions');
  lines.push('');
  lines.push('The user information and instructions for the product are provided in the accompanying documentation.');
  lines.push('');

  lines.push('2. Description of design, development, production and vulnerability handling processes');
  lines.push('');
  lines.push('2.1 Design and development');
  lines.push('');
  lines.push(`The product has been designed and developed by ${val(manufacturer.name, '[MANUFACTURER NAME]')} in accordance with documented design and development processes.`);
  lines.push('');
  lines.push('The product consists of the following main elements:');
  lines.push('');
  lines.push(`Hardware components: ${memory ? `${memory.name} (${memory.mode || ''})` : '[DESCRIPTION]'}`);
  lines.push(`Software / firmware components: ${app ? `${app.name} (${app.programmingLanguage || ''} ${app.programmingVersion || ''})` : '[DESCRIPTION]'}`);
  lines.push(`Interfaces / connections: ${vm ? `${vm.name}${vm.internetAccessibleEndpoint ? ' (internet-accessible)' : ' (not internet-accessible)'}` : '[DESCRIPTION]'}`);
  lines.push('');

  lines.push('2.2 Vulnerability handling');
  lines.push('');
  lines.push('The manufacturer has established vulnerability handling processes for the product, including:');
  lines.push('');
  lines.push('receipt and assessment of vulnerability reports;');
  lines.push('documentation of relevant components;');
  lines.push('remediation and release of security updates;');
  lines.push('communication through the vulnerability contact point.');
  lines.push('');
  lines.push('Relevant documentation:');
  lines.push('');
  lines.push(`Software bill of materials (SBOM): ${val(sbom?.name, '[REFERENCE]')}`);
  lines.push(`Coordinated vulnerability disclosure policy: ${val(cvdp?.name, '[REFERENCE / URL]')}`);
  lines.push(`Vulnerability reporting contact: ${val(secEmail, '[SECURITY CONTACT EMAIL / PORTAL]')}`);
  lines.push(`Secure distribution of updates: ${val(distUpdates?.name, '[SHORT DESCRIPTION / REFERENCE]')}`);
  lines.push('');

  lines.push('2.3 Production, monitoring and validation');
  lines.push('');
  lines.push('The manufacturer has established production and monitoring processes relevant to the cybersecurity of the product, including release control, monitoring, and validation activities.');
  lines.push('');
  lines.push(`Relevant records or procedures are provided in: ${val(prodMonitor?.name, '[REFERENCE / DOCUMENT TITLE]')}`);
  if (monitoring) {
    lines.push(`Monitoring interval: every ${monitoring.intervalMonths} months (${monitoring.name})`);
  }
  lines.push('');

  lines.push('3. Cybersecurity risk assessment');
  lines.push('');
  lines.push('A cybersecurity risk assessment has been carried out for the product.');
  lines.push('');
  lines.push(`Risk assessment reference: ${val(riskAssessment?.name, '[DOCUMENT TITLE / REFERENCE / VERSION]')}`);
  lines.push('');

  lines.push('4. Information relevant to determination of the support period');
  lines.push('');
  lines.push(`Support period / end date: ${formatDate(product?.supportEnds)}`);
  lines.push('');

  lines.push('5. Harmonised standards, common specifications, or European cybersecurity certification schemes applied');
  lines.push('');
  lines.push('No harmonised standards, common specifications, or European cybersecurity certification schemes have been applied.');

  return lines.join('\n');
}