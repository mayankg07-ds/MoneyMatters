import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { SignUp, useAuth } from '@clerk/react';

export default function Register() {
    const navigate = useNavigate();
    const { isSignedIn, isLoaded } = useAuth();

    useEffect(() => {
        if (isLoaded && isSignedIn) {
            navigate('/dashboard', { replace: true });
        }
    }, [isSignedIn, isLoaded, navigate]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
            <SignUp routing="hash" afterSignUpUrl="/dashboard" />
        </div>
    );
}
