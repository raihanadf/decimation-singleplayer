import { describe, it, expect } from 'vitest';
import { roomHint } from '../src/index';

describe('room placement near the host (Durable Object location hints)', () => {
  it('Southeast Asia, Northeast Asia, the rest of Asia', () => {
    expect(roomHint('ID', 'AS')).toBe('apac-se');
    expect(roomHint('SG', 'AS')).toBe('apac-se');
    expect(roomHint('PH', 'AS')).toBe('apac-se');
    expect(roomHint('JP', 'AS')).toBe('apac-ne');
    expect(roomHint('KR', 'AS')).toBe('apac-ne');
    expect(roomHint('IN', 'AS')).toBe('apac');
  });
  it('other continents, and nothing when unknown', () => {
    expect(roomHint('AU', 'OC')).toBe('oc');
    expect(roomHint('DE', 'EU')).toBe('weur');
    expect(roomHint('PL', 'EU')).toBe('eeur');
    expect(roomHint('US', 'NA')).toBe('enam');
    expect(roomHint('BR', 'SA')).toBe('sam');
    expect(roomHint(undefined, undefined)).toBeUndefined();
  });
});
