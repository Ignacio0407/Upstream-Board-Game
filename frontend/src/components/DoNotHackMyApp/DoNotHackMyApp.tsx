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

import { useEffect, useMemo, useRef, useState } from "react";
import "./doNotHackMyApp.css";

type Flag = {id: number;
  x: number;        // 0..100 vw percent
  sizeRem: number;  // font-size en rem
  durationMs: number, delayMs: number, rotateDeg: number, color: string | null;
};

interface DoNotHackMyAppProps {density?: number, spawnIntervalMs?: number, minSpeedMs?: number, maxSpeedMs?: number, 
  emoji?: string, colors?: string[], maxPool?: number}

export default function DoNotHackMyApp({density = 40, spawnIntervalMs = 120, minSpeedMs = 4000, maxSpeedMs = 9000,
  emoji = "🏴‍☠️", colors = ["#000000", "#333333", "#555555"], maxPool = 120}: DoNotHackMyAppProps) {

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