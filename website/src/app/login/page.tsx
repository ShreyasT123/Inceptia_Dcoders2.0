'use client';

import { useRouter } from 'next/navigation';
import { getAuth, signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { app } from '@/lib/firebase'; // Assuming your firebase init is here
import { Button } from '@/components/ui/button';
import { ShieldAlert } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const auth = getAuth(app);
  const provider = new GoogleAuthProvider();

  const handleSignIn = async () => {
    try {
      await signInWithPopup(auth, provider);
      // On successful sign-in, Firebase automatically handles the user session.
      // We can now redirect to the main dashboard.
      router.push('/dashboard'); 
    } catch (error) {
      console.error("Error signing in with Google: ", error);
      // You can add user-facing error handling here
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-[#0d1424]">
      <div className="w-full max-w-md p-8 space-y-8 bg-[#18213a] rounded-xl shadow-lg text-center">
        <div className="flex flex-col items-center">
            <ShieldAlert className="h-16 w-16 text-orange-400 mb-4" />
            <h1 className="text-3xl font-bold text-white">
                Command Center Access
            </h1>
            <p className="text-zinc-400 mt-2">Please sign in to continue.</p>
        </div>
        <Button 
          onClick={handleSignIn} 
          className="w-full bg-blue-600 hover:bg-blue-700 text-lg py-6"
        >
          Sign In with Google
        </Button>
      </div>
    </div>
  );
}