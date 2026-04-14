import type { Evidence } from '$lib/types/evidence';
import { getResourceType, getResourceData } from '$lib/types/evidence';
import type { ManufacturerInfo } from './templateMapper';

export type Segment =
  | { type: 'text'; content: string }
  | { type: 'heading'; content: string; level: 1 | 2 | 3 }
  | { type: 'evidence'; content: string; evidence: Evidence; resourceType: string; field: string }
  | { type: 'placeholder'; content: string }
  | { type: 'bullet' }
    | { type: 'bold'; content: string };

export type DocumentLine = Segment[];

function findEvidence(evidences: Evidence[], type: string): Evidence | null {
  return evidences.find((e) => getResourceType(e) === type) ?? null;
}

function getData(evidence: Evidence | null): Record<string, any> | null {
  if (!evidence) return null;
  return getResourceData(evidence);
}

function getEvidence(
  evidence: Evidence | null,
  field: string,
  placeholder: string,
  formatter?: (value: any) => string
): Segment {
  const data = getData(evidence);
  const value = data?.[field];
  if (value !== undefined && value !== null && value !== '') {
    const display = formatter ? formatter(value) : String(value);
    return {
      type: 'evidence',
      content: display,
      evidence: evidence!,
      resourceType: getResourceType(evidence!),
      field
    };
  }
  return { type: 'placeholder', content: placeholder };
}

function text(content: string): Segment {
  return { type: 'text', content };
}

function heading(content: string, level: 1 | 2 | 3): Segment {
  return { type: 'heading', content, level };
}

function bullet(): Segment {
  return { type: 'bullet' };
}

function bold(content: string): Segment {
  return { type: 'bold', content };
}

function formatDate(isoDate: string | undefined): string {
  if (!isoDate) return '[END DATE]';
  return new Date(isoDate).toISOString().split('T')[0];
}

function manualOrEvidence(
  manualValue: string,
  evidence: Evidence | null,
  field: string,
  placeholder: string
): Segment {
  if (manualValue) {
    return text(manualValue);
  }
  return getEvidence(evidence, field, placeholder);
}

export function generateSegments(
  evidences: Evidence[],
  manufacturer: ManufacturerInfo
): DocumentLine[] {
  const productEv = findEvidence(evidences, 'product');
  const contactEv = findEvidence(evidences, 'contactPerson');
  const appEv = findEvidence(evidences, 'application');
  const memoryEv = findEvidence(evidences, 'memory');
  const vmEv = findEvidence(evidences, 'virtualMachine');
  const sbomEv = findEvidence(evidences, 'sbomDocument');
  const euDeclEv = findEvidence(evidences, 'euDeclarationOfConformity');
  const cvdpEv = findEvidence(evidences, 'coordinatedVulnerabilityDisclosurePolicy');
  const distUpdatesEv = findEvidence(evidences, 'distributionOfUpdatesDocument');
  const prodMonitorEv = findEvidence(evidences, 'productionAndMonitoringProcessDocument');
  const riskAssessmentEv = findEvidence(evidences, 'cyberSecurityRiskAssessmentDocument');
  const monitoringEv = findEvidence(evidences, 'monitoringProcedure');

  const product = getData(productEv);
  const contact = getData(contactEv);
  const app = getData(appEv);
  const memory = getData(memoryEv);
  const vm = getData(vmEv);
  const monitoring = getData(monitoringEv);

  const contactEmail = contact?.emailAddress || '';

  const lines: DocumentLine[] = [];

  // === INFORMATION AND INSTRUCTIONS TO THE USER ===

  lines.push([heading('Information and Instructions to the User', 1)]);
  lines.push([heading('1. Manufacturer', 2)]);
  lines.push([text('Manufacturer name: '), manualOrEvidence(manufacturer.name, null, '', '[MANUFACTURER NAME]')]);
  lines.push([text('Registered trade name or trademark: '), manualOrEvidence(manufacturer.tradeName, null, '', '[TRADE NAME / TRADEMARK]')]);
  lines.push([text('Postal address: '), manualOrEvidence(manufacturer.postalAddress, null, '', '[POSTAL ADDRESS]')]);
  lines.push([
    text('General contact email: '),
    manufacturer.generalEmail
      ? text(manufacturer.generalEmail)
      : contactEmail
        ? getEvidence(contactEv, 'emailAddress', '[GENERAL CONTACT EMAIL]')
        : { type: 'placeholder' as const, content: '[GENERAL CONTACT EMAIL]' }
  ]);
  lines.push([
    text('Security contact email: '),
    manufacturer.securityEmail
      ? text(manufacturer.securityEmail)
      : contactEmail
        ? getEvidence(contactEv, 'emailAddress', '[SECURITY EMAIL]')
        : { type: 'placeholder' as const, content: '[SECURITY EMAIL]' }
  ]);
  lines.push([text('Website: '), manualOrEvidence(manufacturer.website, null, '', '[WEBSITE]')]);
  // 2. Vulnerability reporting
  lines.push([heading('2. Vulnerability reporting', 2)]);
  lines.push([text('Information about vulnerabilities concerning this product may be reported to the manufacturer through the following single point of contact:')]);
  lines.push([
    bold('Security contact: '),
    manufacturer.securityEmail
      ? text(manufacturer.securityEmail)
      : contactEmail
        ? getEvidence(contactEv, 'emailAddress', '[SECURITY EMAIL]')
        : { type: 'placeholder' as const, content: '[SECURITY EMAIL]' }
  ]);
  lines.push([bold('Alternative reporting channel: '), manualOrEvidence(manufacturer.securityPortalUrl, null, '', '[SECURITY PORTAL URL]')]);
  lines.push([bold('CVD policy: '), getEvidence(cvdpEv, 'raw', '[CVD POLICY URL]')]);
  lines.push([text('When reporting a vulnerability, the reporter should, where possible, provide:')]);
  lines.push([bullet(), text('product name and version;')]);
  lines.push([bullet(), text('affected component or interface, if known;')]);
  lines.push([bullet(), text('description of the suspected vulnerability;')]);
  lines.push([bullet(), text('conditions under which it can be reproduced;')]);
  lines.push([bullet(), text('potential impact, if known;')]);
  lines.push([bullet(), text('contact details for follow-up.')]);
  lines.push([text('The manufacturer will handle vulnerability reports in accordance with its coordinated vulnerability disclosure policy.')]);

  // 3. Product identification
  lines.push([heading('3. Product identification', 2)]);
  lines.push([bold('Product name: '), getEvidence(productEv, 'name', '[PRODUCT NAME]')]);
  lines.push([bold('Product type: '), getEvidence(productEv, 'type', '[PRODUCT TYPE]')]);
  lines.push([bold('Model: '), manualOrEvidence(manufacturer.model, null, '', '[MODEL]')]);
  lines.push([bold('Version / firmware / software version: '), getEvidence(productEv, 'programmingVersion', '[VERSION]')]);
  lines.push([bold('Unique product identifier, if applicable: '), manualOrEvidence(manufacturer.serialNumber, null, '', '[SERIAL NUMBER / BATCH / DEVICE ID]')]);

  // 4. Intended purpose
  lines.push([heading('4. Intended purpose and essential information', 2)]);
  lines.push([bold('Intended purpose:')]);
  lines.push([text('This product is intended for '), getEvidence(productEv, 'purpose', '[INTENDED PURPOSE]'), text('.')]);
  lines.push([bold('Essential functionalities:')]);
  lines.push([text('The product provides the following essential functionalities: '), getEvidence(productEv, 'description', '[ESSENTIAL FUNCTIONALITIES]'), text('.')]);
  lines.push([bold('Security properties:')]);
  lines.push([text('The product includes the following security-relevant properties, where applicable: authentication, access control, update capability, logging, and protection of data in storage and transmission.')]);

  // 5. Foreseeable circumstances
  lines.push([heading('5. Foreseeable circumstances of use or misuse creating cybersecurity risks', 2)]);
  lines.push([text('Cybersecurity risks may arise in particular where:')]);
  lines.push([bullet(), text('the product is used outside its intended purpose;')]);
  lines.push([bullet(), text('the product is operated with outdated software or firmware;')]);
  lines.push([bullet(), text('default or weak credentials are used;')]);
  lines.push([bullet(), text('security settings are changed without authorization or sufficient knowledge;')]);
  lines.push([bullet(), text('the product is connected to untrusted networks or systems;')]);
  lines.push([bullet(), text('unsupported third-party software, hardware, or services are used.')]);

  // 6. EU declaration
  lines.push([heading('6. EU declaration of conformity', 2)]);
  lines.push([text('Where applicable, the EU declaration of conformity is available at:')]);
  lines.push([getEvidence(euDeclEv, 'raw', '[URL]')]);

  // 7. Technical security support
  lines.push([heading('7. Technical security support', 2)]);
  lines.push([bold('Type of technical security support:')]);
  lines.push([text('The manufacturer provides security-relevant updates, vulnerability handling, and security-related user support for this product.')]);
  lines.push([bold('End date of support period:')]);
  lines.push([getEvidence(productEv, 'supportEnds', '[END DATE]', formatDate)]);
  lines.push([text('After this date, security updates or vulnerability handling for the product may no longer be provided.')]);

  // 8. Detailed instructions
  lines.push([heading('8. Detailed instructions and information', 2)]);

  lines.push([heading('(a) Measures for secure use', 3)]);
  lines.push([text('For secure initial commissioning and secure use throughout the lifetime of the product, the user should:')]);
  lines.push([bullet(), text('install the product in accordance with the manufacturer\'s instructions;')]);
  lines.push([bullet(), text('ensure the product is updated to the latest available version before or during initial use;')]);
  lines.push([bullet(), text('replace default credentials or set strong credentials where applicable;')]);
  lines.push([bullet(), text('restrict access to authorized users only;')]);
  lines.push([bullet(), text('use the product only in the intended operating environment;')]);
  lines.push([bullet(), text('install security updates without undue delay;')]);
  lines.push([bullet(), text('keep connected systems and interfaces appropriately secured.')]);

  lines.push([heading('(b) Effect of changes on the security of data', 3)]);
  lines.push([text('Changes to the product, including configuration changes, software changes, connection of external systems, or changes to user access settings, may affect the security of data. Such changes may reduce the confidentiality, integrity, or availability of data if they are not properly implemented and controlled.')]);

  lines.push([heading('(c) Installation of security-relevant updates', 3)]);
  lines.push([text('Security-relevant updates can be installed by '), manualOrEvidence(manufacturer.automaticUpdateMethod, null, '', '[AUTOMATIC UPDATE FUNCTION / ADMIN INTERFACE / MANUAL INSTALLATION METHOD]'), text('.')]);

  lines.push([heading('(d) Secure decommissioning', 3)]);
  lines.push([text('Before disposal, resale, transfer, return, or permanent withdrawal from service, the user should:')]);
  lines.push([bullet(), text('back up data, where necessary;')]);
  lines.push([bullet(), text('remove the product from user accounts, management systems, or connected services;')]);
  lines.push([bullet(), text('revoke or delete credentials, tokens, or access rights associated with the product;')]);
  lines.push([bullet(), text('reset the product to factory settings or use the provided secure erase function, where available;')]);
  lines.push([bullet(), text('verify that user data has been removed from the product.')]);
  lines.push([bold('Data removal method: '), manualOrEvidence(manufacturer.dataRemovalMethod, null, '', '[FACTORY RESET / SECURE WIPE / ADMIN FUNCTION / INSTRUCTIONS URL]')]);

  lines.push([heading('(e) Turning off automatic security updates', 3)]);
  lines.push([text('Where automatic installation of security updates is enabled by default, this setting can be turned off through: '), manualOrEvidence(manufacturer.disableUpdatesPath, null, '', '[SETTINGS PATH / ADMIN INTERFACE / INSTRUCTIONS URL]')]);
  lines.push([text('Disabling automatic security updates may increase cybersecurity risks. The manufacturer recommends keeping automatic security updates enabled.')]);

  lines.push([heading('(f) Information for integration into other products', 3)]);
  lines.push([text('Where this product is intended for integration into other products with digital elements, the integrator should use the product only in accordance with the integration, interface, configuration, and security instructions provided by the manufacturer.')]);
  lines.push([text('Integration information is available at: '), manualOrEvidence(manufacturer.integrationDocUrl, null, '', '[INTEGRATION DOCUMENT / URL]')]);

  // === TECHNICAL DOCUMENTATION ===

  lines.push([heading('TECHNICAL DOCUMENTATION', 1)]);

  lines.push([heading('1. General description of the product with digital elements', 2)]);

  lines.push([heading('1.1 Intended purpose', 3)]);
  lines.push([text('The product with digital elements '), getEvidence(productEv, 'name', '[PRODUCT NAME]'), text(' is intended for '), getEvidence(productEv, 'purpose', '[INTENDED PURPOSE]'), text('.')]);
  lines.push([text('It is intended to be used in '), getEvidence(productEv, 'contextOfUse', '[INTENDED ENVIRONMENT / CONTEXT OF USE]'), text('.')]);

  lines.push([heading('1.2 Software versions affecting compliance', 3)]);
  lines.push([text('The following software / firmware versions affect compliance with the essential cybersecurity requirements:')]);
  lines.push([text('Product version: '), getEvidence(productEv, 'programmingVersion', '[VERSION]')]);
  if (app) {
    lines.push([
      text('Application: '),
      getEvidence(appEv, 'name', '[NAME]'),
      text(' ('),
      getEvidence(appEv, 'programmingLanguage', ''),
      text(' '),
      getEvidence(appEv, 'programmingVersion', '[VERSION]'),
      text(')')
    ]);
  }

  lines.push([heading('1.3 External features, marking and internal layout', 3)]);
  lines.push([text('Not applicable.')]);

  lines.push([heading('1.4 User information and instructions', 3)]);
  lines.push([text('The user information and instructions for the product are provided in the accompanying documentation.')]);

  // 2. Design, development, production
  lines.push([heading('2. Description of design, development, production and vulnerability handling processes', 2)]);

  lines.push([heading('2.1 Design and development', 3)]);
  lines.push([text('The product has been designed and developed by '), manualOrEvidence(manufacturer.name, null, '', '[MANUFACTURER NAME]'), text(' in accordance with documented design and development processes.')]);
  lines.push([text('The product consists of the following main elements:')]);
  if (memory) {
    lines.push([bullet(), text('Hardware components: '), getEvidence(memoryEv, 'name', '[DESCRIPTION]'), text(' ('), getEvidence(memoryEv, 'mode', ''), text(')')]);
  } else {
    lines.push([bullet(), text('Hardware components: '), { type: 'placeholder' as const, content: '[DESCRIPTION]' }]);
  }
  if (app) {
    lines.push([bullet(), text('Software / firmware components: '), getEvidence(appEv, 'name', '[DESCRIPTION]'), text(' ('), getEvidence(appEv, 'programmingLanguage', ''), text(' '), getEvidence(appEv, 'programmingVersion', ''), text(')')]);
  } else {
    lines.push([bullet(), text('Software / firmware components: '), { type: 'placeholder' as const, content: '[DESCRIPTION]' }]);
  }
  if (vm) {
    const accessible = vm.internetAccessibleEndpoint;
    lines.push([bullet(), text('Interfaces / connections: '), getEvidence(vmEv, 'name', '[DESCRIPTION]'), text(accessible ? ' (internet-accessible)' : ' (not internet-accessible)')]);
  } else {
    lines.push([bullet(), text('Interfaces / connections: '), { type: 'placeholder' as const, content: '[DESCRIPTION]' }]);
  }

  lines.push([heading('2.2 Vulnerability handling', 3)]);
  lines.push([text('The manufacturer has established vulnerability handling processes for the product, including:')]);
  lines.push([bullet(), text('receipt and assessment of vulnerability reports;')]);
  lines.push([bullet(), text('documentation of relevant components;')]);
  lines.push([bullet(), text('remediation and release of security updates;')]);
  lines.push([bullet(), text('communication through the vulnerability contact point.')]);
  lines.push([text('Relevant documentation:')]);
  lines.push([bullet(), text('Software bill of materials (SBOM): '), getEvidence(sbomEv, 'name', '[REFERENCE]')]);
  lines.push([bullet(), text('Coordinated vulnerability disclosure policy: '), getEvidence(cvdpEv, 'name', '[REFERENCE / URL]')]);
  lines.push([
    bullet(),
    text('Vulnerability reporting contact: '),
    manufacturer.securityEmail
      ? text(manufacturer.securityEmail)
      : contactEmail
        ? getEvidence(contactEv, 'emailAddress', '[SECURITY CONTACT EMAIL / PORTAL]')
        : { type: 'placeholder' as const, content: '[SECURITY CONTACT EMAIL / PORTAL]' }
  ]);
  lines.push([bullet(), text('Secure distribution of updates: '), getEvidence(distUpdatesEv, 'name', '[SHORT DESCRIPTION / REFERENCE]')]);

  lines.push([heading('2.3 Production, monitoring and validation', 3)]);
  lines.push([text('The manufacturer has established production and monitoring processes relevant to the cybersecurity of the product, including release control, monitoring, and validation activities.')]);
  lines.push([text('Relevant records or procedures are provided in: '), getEvidence(prodMonitorEv, 'name', '[REFERENCE / DOCUMENT TITLE]')]);
  if (monitoring) {
    lines.push([text('Monitoring interval: every '), getEvidence(monitoringEv, 'intervalMonths', '[INTERVAL]'), text(' months ('), getEvidence(monitoringEv, 'name', ''), text(')')]);
  }

  // 3. Risk assessment
  lines.push([heading('3. Cybersecurity risk assessment', 2)]);
  lines.push([text('A cybersecurity risk assessment has been carried out for the product.')]);
  lines.push([text('Risk assessment reference: '), getEvidence(riskAssessmentEv, 'name', '[DOCUMENT TITLE / REFERENCE / VERSION]')]);

  // 4. Support period
  lines.push([heading('4. Information relevant to determination of the support period', 2)]);
  lines.push([text('Support period / end date: '), getEvidence(productEv, 'supportEnds', '[END DATE]', formatDate)]);

  // 5. Standards
  lines.push([heading('5. Harmonised standards, common specifications, or European cybersecurity certification schemes applied', 2)]);
  lines.push([text('No harmonised standards, common specifications, or European cybersecurity certification schemes have been applied.')]);

  return lines;
}

export function countFields(lines: DocumentLine[]): { total: number; filled: number } {
  let total = 0;
  let filled = 0;
  for (const line of lines) {
    for (const segment of line) {
      if (segment.type === 'evidence') {
        total++;
        filled++;
      } else if (segment.type === 'placeholder') {
        total++;
      }
    }
  }
  return { total, filled };
}