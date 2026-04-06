import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Show, SignInButton, SignUpButton, UserButton, useAuth } from '@clerk/react';

import LandingNavbar from '../components/landing/LandingNavbar';
import LandingHero from '../components/landing/LandingHero';
import LandingTicker from '../components/landing/LandingTicker';
import LandingCalculators from '../components/landing/LandingCalculators';
import LandingPortfolio from '../components/landing/LandingPortfolio';
import LandingHowItWorks from '../components/landing/LandingHowItWorks';
import LandingStatsBar from '../components/landing/LandingStatsBar';
import LandingFinalCTA from '../components/landing/LandingFinalCTA';
import LandingFooter from '../components/landing/LandingFooter';

import './Landing.css';

export default function Login() {
    const navigate = useNavigate();
    const { isSignedIn, isLoaded } = useAuth();

    // Redirect to dashboard automatically once Clerk confirms sign-in
    useEffect(() => {
        if (isLoaded && isSignedIn) {
            navigate('/dashboard');
        }
    }, [isSignedIn, isLoaded, navigate]);

    // Clerk sign-in slot rendered inside LandingNavbar
    const clerkSignInSlot = (
        <>
            <Show when="signed-out">
                <div style={{ display: 'flex', gap: '12px' }}>
                    <SignInButton mode="modal">
                        <button
                            className="land-btn land-btn-ghost"
                            style={{ padding: '10px 22px', fontSize: '14px' }}
                        >
                            Sign In
                        </button>
                    </SignInButton>
                    <SignUpButton mode="modal">
                        <button
                            className="land-btn land-btn-primary"
                            style={{ padding: '10px 22px', fontSize: '14px' }}
                        >
                            Sign Up
                        </button>
                    </SignUpButton>
                </div>
            </Show>
            <Show when="signed-in">
                <UserButton afterSignOutUrl="/" />
            </Show>
        </>
    );

    return (
        <div className="landing-page-wrapper">
            <LandingNavbar onSignInClick={null} clerkSignInSlot={clerkSignInSlot} />

            <main>
                <LandingHero />
                <LandingTicker />
                <LandingCalculators />
                <LandingPortfolio />
                <LandingHowItWorks />
                <LandingStatsBar />
                <LandingFinalCTA />
            </main>

            <LandingFooter />
        </div>
    );
}
