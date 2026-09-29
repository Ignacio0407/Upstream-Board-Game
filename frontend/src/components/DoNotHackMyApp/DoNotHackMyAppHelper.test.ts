/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
 * -----------------------------------------------------------------------------
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { navigateToUnauthorized } from './DoNotHackMyAppHelper';
import { Authority } from '../../interfaces/User';

describe('DoNotHackMyAppHelper', () => {
    const navigate = vi.fn();

    beforeEach(() => {
        navigate.mockClear();
    });

    it('uses checkAuth function if provided', () => {
        const checkAuth = vi.fn().mockReturnValue(true);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, Authority.ADMIN, navigate);
        expect(checkAuth).toHaveBeenCalledWith(Authority.ADMIN);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects if checkAuth function returns false', () => {
        const checkAuth = vi.fn().mockReturnValue(false);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, Authority.ADMIN, navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('uses checkAuthDefault if checkAuth is undefined', () => {
        // Alice has roles [Authority.PLAYER]
        const alice = { id: 1, username: 'Alice', roles: [Authority.PLAYER] };
        
        // Attempting to access ADMIN area
        navigateToUnauthorized(undefined, alice as any, Authority.ADMIN, navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('allows access if checkAuthDefault returns true', () => {
        const admin = { id: 1, username: 'Admin', roles: [Authority.ADMIN] };
        navigateToUnauthorized(undefined, admin as any, Authority.ADMIN, navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('allows access if checkAuth is true', () => {
        navigateToUnauthorized(true, { id: 1 } as any, Authority.ADMIN, navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects to custom URL', () => {
        const checkAuth = () => false;
        navigateToUnauthorized(checkAuth, { id: 1 } as any, Authority.ADMIN, navigate, '/custom-redirect');
        expect(navigate).toHaveBeenCalledWith('/custom-redirect');
    });
});
