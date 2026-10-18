const SE_ASIA = new Set(['ID', 'SG', 'MY', 'TH', 'VN', 'PH', 'KH', 'LA', 'MM', 'BN', 'TL']);
const NE_ASIA = new Set(['JP', 'KR', 'KP', 'CN', 'TW', 'HK', 'MO', 'MN']);
const E_EUROPE = new Set(['PL', 'CZ', 'SK', 'HU', 'RO', 'BG', 'UA', 'BY', 'MD', 'LT', 'LV', 'EE', 'RU', 'RS', 'HR', 'SI', 'BA', 'GR', 'TR']);

/**
 * Where a new room should live: near the host whose control socket creates it (Durable Object location hint, best effort,
 * only on creation). Free Worker traffic from Indonesia was seen in Paris and Frankfurt (docs/player_hosting.md 14).
 */
export function roomHint(country?: string, continent?: string): DurableObjectLocationHint | undefined {
  if (continent === 'AS') return country && SE_ASIA.has(country) ? 'apac-se' : country && NE_ASIA.has(country) ? 'apac-ne' : 'apac';
  if (continent === 'OC') return 'oc';
  if (continent === 'EU') return country && E_EUROPE.has(country) ? 'eeur' : 'weur';
  if (continent === 'NA') return 'enam';
  if (continent === 'SA') return 'sam';
  if (continent === 'AF') return 'afr';
  return undefined;
}
