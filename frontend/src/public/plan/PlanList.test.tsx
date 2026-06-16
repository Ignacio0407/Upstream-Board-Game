import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import PlanList from './PlanList.tsx';

describe('PlanList Component', () => {
  it('renders all pricing plans correctly', () => {
    render(<PlanList />);
    
    // Comprueba que los títulos principales están en la pantalla
    expect(screen.getByText('Pricing Plans')).toBeDefined();
    expect(screen.getByText('BASIC')).toBeDefined();
    expect(screen.getByText('GOLD')).toBeDefined();
    expect(screen.getByText('PLATINUM')).toBeDefined();
    
    // Comprueba que los precios están
    expect(screen.getByText('FREE')).toBeDefined();
    expect(screen.getByText('5')).toBeDefined();
    expect(screen.getByText('12')).toBeDefined();
  });
});