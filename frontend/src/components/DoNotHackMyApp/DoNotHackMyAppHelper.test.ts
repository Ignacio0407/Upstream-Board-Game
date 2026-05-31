/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { navigateToUnauthorized } from './DoNotHackMyAppHelper';

describe('DoNotHackMyAppHelper', () => {
    const navigate = vi.fn();

    beforeEach(() => {
        navigate.mockClear();
    });

    it('uses checkAuth function if provided', () => {
        const checkAuth = vi.fn().mockReturnValue(true);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, 'ADMIN', navigate);
        expect(checkAuth).toHaveBeenCalledWith('ADMIN');
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects if checkAuth function returns false', () => {
        const checkAuth = vi.fn().mockReturnValue(false);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, 'ADMIN', navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('uses checkAuthDefault if checkAuth is undefined', () => {
        // Alice has roles ['PLAYER']
        const alice = { id: 1, username: 'Alice', roles: ['PLAYER'] };
        
        // Attempting to access ADMIN area
        navigateToUnauthorized(undefined, alice as any, 'ADMIN', navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('allows access if checkAuthDefault returns true', () => {
        const admin = { id: 1, username: 'Admin', roles: ['ADMIN'] };
        navigateToUnauthorized(undefined, admin as any, 'ADMIN', navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('allows access if checkAuth is true', () => {
        navigateToUnauthorized(true, { id: 1 } as any, 'ADMIN', navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects to custom URL', () => {
        const checkAuth = () => false;
        navigateToUnauthorized(checkAuth, { id: 1 } as any, 'ADMIN', navigate, '/custom-redirect');
        expect(navigate).toHaveBeenCalledWith('/custom-redirect');
    });
});
