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

import { useEffect, useMemo, useRef, useState } from "react";
import "./doNotHackMyApp.css";

type Flag = {id: number;
  x: number;        // 0..100 vw percent
  sizeRem: number;  // font-size en rem
  durationMs: number, delayMs: number, rotateDeg: number, color: string | null;
};

interface DoNotHackMyAppProps {readonly density?: number, readonly spawnIntervalMs?: number, readonly minSpeedMs?: number, readonly maxSpeedMs?: number, 
  readonly emoji?: string, readonly colors?: readonly string[], readonly maxPool?: number}

export default function DoNotHackMyApp({density = 40, spawnIntervalMs = 120, minSpeedMs = 4000, maxSpeedMs = 9000,
  emoji = "🏴‍☠️", colors = ["#000000", "#333333", "#555555"], maxPool = 120}: Readonly<DoNotHackMyAppProps>) {

  const [flags, setFlags] = useState<Flag[]>([]);
  const idCounter = useRef(0);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const prefersReducedMotion = useMemo(() => {
    return typeof window !== "undefined" &&
      window.matchMedia &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }, []);

  const createFlag = (): Flag => {
    const id = idCounter.current++;
    const x = Math.random() * 100; // Horizontal percentage
    const durationMs = randomBetween(minSpeedMs, maxSpeedMs);
    const delayMs = Math.random() * 1000; // Little gap
    const sizeRem = randomBetween(3.2, 5.4);
    const rotateDeg = randomBetween(-20, 20);
    const color = colors.length ? colors[Math.floor(Math.random() * colors.length)] : null;
    return { id, x, sizeRem, durationMs, delayMs, rotateDeg, color };
  };

  useEffect(() => {
    if (prefersReducedMotion) {
      // Accesible mode: less elements, less speed
      const initial = Array.from({ length: Math.min(10, density) }, createFlag);
      setFlags(initial);
      return;
    }

    // Initial: fill until density
    setFlags((prev) => {
      const needed = Math.max(0, density - prev.length);
      const batch = Array.from({ length: needed }, createFlag);
      return [...prev, ...batch].slice(-maxPool);
    });

    // Periodic spawner that recicles without rising infinitely
    const interval = setInterval(() => {
      setFlags((prev) => {
        const next = [...prev, createFlag()];
        // If we overcome maxPool, the oldest flag is removed
        return next.length > maxPool ? next.slice(next.length - maxPool) : next;
      });
    }, spawnIntervalMs);

    return () => clearInterval(interval);
  }, [density, spawnIntervalMs, prefersReducedMotion, maxPool, minSpeedMs, maxSpeedMs, colors]);

  return (
    <div ref={containerRef} className="dh-container">
      <div className="dh-overlay">
        <h1 className="dh-title">Don' t Hack My App 😎</h1>
        <h2 className="dh-hacker-detected" style={{ color: '#ff0000', textTransform: 'uppercase', letterSpacing: '0.2rem', marginBottom: '1rem' }}>
            HACKER DETECTED
        </h2>
        <p className="dh-subtitle">Security mode: fun and relentless 🛡️</p>
      </div>

      <div className="dh-stage">
        {flags.map((flag) => (
          <span key={flag.id} className="dh-flag"
            style={{left: `${flag.x}vw`, fontSize: `${flag.sizeRem}rem`, animationDuration: `${flag.durationMs}ms`,
              animationDelay: `${flag.delayMs}ms`, transform: `rotate(${flag.rotateDeg}deg)`, color: flag.color ?? undefined}}
            aria-hidden="true"
          >
            {emoji}
          </span>
        ))}
      </div>
    </div>
  );
}

function randomBetween(min: number, max: number) {
  return Math.random() * (max - min) + min;
}