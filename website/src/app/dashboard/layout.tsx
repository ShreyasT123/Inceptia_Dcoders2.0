'use client';

import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import Header from '@/components/Header'; // Assuming you have a Header component
import { ShieldAlert } from 'lucide-react';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // If loading is finished and there's no user, redirect to login
    if (!loading && !user) {
      router.push('/login');
    }
  }, [user, loading, router]);

  // While loading, show a loading screen to prevent flicker
  if (loading) {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-[#0d1424]">
            <ShieldAlert className="h-12 w-12 text-orange-400 animate-pulse" />
            <p className="mt-4 text-zinc-300">Authenticating...</p>
        </div>
    );
  }

  // If there is a user, render the protected dashboard layout
  if (user) {
    return (
      <div className="relative flex min-h-screen flex-col bg-background">
        <Header />
        <main className="flex-1 p-4 md:p-8">{children}</main>
      </div>
    );
  }

  // If no user after loading, this will be briefly rendered before redirect
  return null;
}