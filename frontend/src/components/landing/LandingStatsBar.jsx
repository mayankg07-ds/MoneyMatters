import { motion, useInView } from 'framer-motion';
import { useRef, useEffect, useState } from 'react';

// Simple count up hook
function useCountUp(end, duration = 2000, start = 0) {
    const [count, setCount] = useState(start);
    const ref = useRef(null);
    const isInView = useInView(ref, { once: true });

    useEffect(() => {
        let startTime;
        let animationFrame;

        if (isInView) {
            const updateCount = (timestamp) => {
                if (!startTime) startTime = timestamp;
                const progress = timestamp - startTime;

                if (progress < duration) {
                    // easeOutExpoish
                    const currentCount = Math.floor(end * (1 - Math.pow(2, -10 * progress / duration)));
                    setCount(currentCount);
                    animationFrame = requestAnimationFrame(updateCount);
                } else {
                    setCount(end);
                }
            };
            animationFrame = requestAnimationFrame(updateCount);
        }
        return () => cancelAnimationFrame(animationFrame);
    }, [isInView, end, duration]);

    return { count, ref };
}

const StatItem = ({ end, suffix, label }) => {
    const { count, ref } = useCountUp(end);

    return (
        <div ref={ref} style={{ textAlign: 'center' }}>
            <div className="font-display" style={{ fontSize: '48px', fontWeight: 700, color: 'var(--land-accent-green)', marginBottom: '8px' }}>
                {end >= 1000 ? (count / 1000).toFixed(1).replace('.0', '') + 'k' : count}{suffix}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--land-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.1em' }}>
                {label}
            </div>
        </div>
    );
};

export default function LandingStatsBar() {
    return (
        <section style={{
            background: 'rgba(16,185,129,0.04)',
            borderTop: '1px solid rgba(16,185,129,0.1)',
            borderBottom: '1px solid rgba(16,185,129,0.1)',
            padding: 'var(--section-pad-sm)'
        }}>
            <div className="landing-container">
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(4, 1fr)',
                    gap: '24px'
                }}>
                    {/* Static values mapped to useCountUp */}
                    <StatItem end={12000} suffix="+" label="Active Users" />
                    <StatItem end={847} suffix=" Cr+" label="Wealth Tracked" />
                    <StatItem end={6} suffix="" label="Free Tools Forever" />
                    <div style={{ textAlign: 'center' }}>
                        <div className="font-display" style={{ fontSize: '48px', fontWeight: 700, color: 'var(--land-accent-green)', marginBottom: '8px' }}>
                            99.8%
                        </div>
                        <div style={{ fontSize: '13px', color: 'var(--land-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.1em' }}>
                            Accuracy
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
