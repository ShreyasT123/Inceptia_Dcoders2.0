// import Link from 'next/link';
// import { Shield } from 'lucide-react';

// const Header = () => {
//   return (
//     <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
//       <div className="container flex h-14 max-w-screen-2xl items-center">
//         {/* Logo and App Name */}
//         <Link href="/" className="mr-6 flex items-center space-x-2">
//           <Shield className="h-6 w-6 text-primary" />
//           <span className="hidden font-bold sm:inline-block">
//             ResQ-Dash
//           </span>
//         </Link>
        
//         {/* Navigation Links */}
//         <nav className="flex items-center gap-4 text-sm lg:gap-6">
//           <Link
//             href="/"
//             className="transition-colors hover:text-foreground/80 text-foreground/60"
//           >
//             Dashboard
//           </Link>
//           {/* Add more links here if needed in the future */}
//         </nav>

//         {/* Right side content, e.g., User Profile */}
//         <div className="flex flex-1 items-center justify-end">
//             {/* You can add a user profile button here later */}
//         </div>
//       </div>
//     </header>
//   );
// };

// export default Header;
'use client'; // This component now uses hooks, so it must be a client component

import Link from 'next/link';
import { Shield, LogOut } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/context/AuthContext'; // Import our custom hook
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"; // Assuming you've added avatar with shadcn

// If you haven't added the Avatar component, run this in your terminal:
// npx shadcn-ui@latest add avatar

const Header = () => {
  // Use the hook to get the user object and the signOut function
  const { user, signOut } = useAuth();

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 max-w-screen-2xl items-center">
        {/* Logo and App Name */}
        <Link href="/dashboard" className="mr-6 flex items-center space-x-2">
          <Shield className="h-6 w-6 text-primary" />
          <span className="hidden font-bold sm:inline-block">
            ResQ-Dash
          </span>
        </Link>
        
        {/* Navigation Links */}
        <nav className="flex items-center gap-4 text-sm lg:gap-6">
          <Link
            href="/dashboard"
            className="transition-colors hover:text-foreground/80 text-foreground/60"
          >
            Dashboard
          </Link>
        </nav>

        {/* --- NEW USER PROFILE & SIGN OUT BUTTON --- */}
        <div className="flex flex-1 items-center justify-end space-x-4">
          {user && (
            <>
              <div className="flex items-center gap-2">
                <Avatar className="h-8 w-8">
                  <AvatarImage src={user.photoURL ?? ''} alt={user.displayName ?? 'User'} />
                  <AvatarFallback>{user.displayName?.charAt(0)}</AvatarFallback>
                </Avatar>
                <span className="hidden text-sm font-medium sm:inline-block">
                  {user.displayName}
                </span>
              </div>
              <Button variant="outline" size="sm" onClick={signOut}>
                <LogOut className="mr-2 h-4 w-4" />
                Sign Out
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;