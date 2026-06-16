import { describe, it, expect, vi } from 'vitest';
import getDeleteAlertsOrModal from './getDeleteAlertsOrModal';
import React from 'react';

describe('getDeleteAlertsOrModal', () => {
    
    it('calls setMessage and setVisible if json has statusCode', () => {
        const setMessage = vi.fn();
        const setVisible = vi.fn();
        const json = { statusCode: 400, message: 'Conflict' };
        
        getDeleteAlertsOrModal(json, 1, [], vi.fn(), setMessage, setVisible);
        
        expect(setMessage).toHaveBeenCalledWith('Conflict');
        expect(setVisible).toHaveBeenCalledWith(true);
    });

    it('does not throw if setMessage and setVisible are undefined and json has statusCode', () => {
        const json = { statusCode: 400, message: 'Conflict' };
        
        expect(() => {
            getDeleteAlertsOrModal(json, 1, [], vi.fn(), undefined, undefined);
        }).not.toThrow();
    });

    it('adds an alert if json has no statusCode and allows dismissing it', () => {
        const setAlerts = vi.fn();
        const json = { statusCode: null, message: 'Deleted successfully' };
        
        const initialAlerts = [{ id: 'alert-old', alert: React.createElement('div') }];
        
        getDeleteAlertsOrModal(json as any, 1, initialAlerts, setAlerts, undefined, undefined);
        
        expect(setAlerts).toHaveBeenCalledTimes(1);

        const newAlertsArray = setAlerts.mock.calls[0][0];
        
        expect(newAlertsArray).toHaveLength(2);
        expect(newAlertsArray[1].id).toBe('alert-1');

        const alertElement = newAlertsArray[1].alert;
        expect(alertElement.props.toggle).toBeDefined();

        alertElement.props.toggle();

        expect(setAlerts).toHaveBeenCalledTimes(2);
        
        const dismissedAlertsArray = setAlerts.mock.calls[1][0];
        expect(dismissedAlertsArray).toHaveLength(1);
        expect(dismissedAlertsArray[0].id).toBe('alert-old');
    });
});