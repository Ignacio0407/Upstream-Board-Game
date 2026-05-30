import { describe, it, expect, vi } from 'vitest';
import getDeleteAlertsOrModal from './getDeleteAlertsOrModal';

describe('getDeleteAlertsOrModal', () => {
    it('calls setMessage and setVisible if json has statusCode', () => {
        const setMessage = vi.fn();
        const setVisible = vi.fn();
        const json = { statusCode: 400, message: 'Conflict' };
        
        getDeleteAlertsOrModal(json, 1, [], vi.fn(), setMessage, setVisible);
        
        expect(setMessage).toHaveBeenCalledWith('Conflict');
        expect(setVisible).toHaveBeenCalledWith(true);
    });

    it('adds an alert if json has no statusCode', () => {
        const setAlerts = vi.fn();
        const json = { statusCode: null, message: 'Deleted successfully' };
        
        getDeleteAlertsOrModal(json as any, 1, [], setAlerts, vi.fn(), vi.fn());
        
        expect(setAlerts).toHaveBeenCalled();
        const call = setAlerts.mock.calls[0][0];
        // Test that it adds one alert
        expect(call).toHaveLength(1);
        expect(call[0].id).toBe('alert-1');
    });
});
