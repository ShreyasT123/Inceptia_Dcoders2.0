// 'use client';

// import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
// import { getAuth, onAuthStateChanged, User } from 'firebase/auth';
// import { app } from '@/lib/firebase'; // Your Firebase initialization file

// // Define the shape of the context data
// interface AuthContextType {
//   user: User | null;
//   loading: boolean;
// }

// // Create the context with a default value
// const AuthContext = createContext<AuthContextType>({ user: null, loading: true });

// // Create the provider component
// export const AuthProvider = ({ children }: { children: ReactNode }) => {
//   const [user, setUser] = useState<User | null>(null);
//   const [loading, setLoading] = useState(true);
//   const auth = getAuth(app);

//   useEffect(() => {
//     // onAuthStateChanged is the Firebase listener for login/logout events
//     const unsubscribe = onAuthStateChanged(auth, (user) => {
//       setUser(user);
//       setLoading(false);
//     });

//     // Clean up the listener when the component unmounts
//     return () => unsubscribe();
//   }, [auth]);

//   return (
//     <AuthContext.Provider value={{ user, loading }}>
//       {children}
//     </AuthContext.Provider>
//   );
// };

// // Create a custom hook to easily access the context
// export const useAuth = () => useContext(AuthContext);
'use client';

import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { getAuth, onAuthStateChanged, User, signOut as firebaseSignOut } from 'firebase/auth'; // Import signOut
import { app } from '@/lib/firebase';
import { useRouter } from 'next/navigation'; // Import for redirection

// Define the shape of the context data, now including a signOut function
interface AuthContextType {
  user: User | null;
  loading: boolean;
  signOut: () => void; // Add the signOut function signature
}

const AuthContext = createContext<AuthContextType>({ 
  user: null, 
  loading: true, 
  signOut: () => {} // Provide a dummy function as default
});

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const auth = getAuth(app);
  const router = useRouter(); // Get the router instance

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user);
      setLoading(false);
    });
    return () => unsubscribe();
  }, [auth]);

  // --- NEW SIGN OUT FUNCTION ---
  const signOut = async () => {
    try {
      await firebaseSignOut(auth);
      // After signing out, Firebase's onAuthStateChanged will trigger, setting user to null.
      // We can also manually redirect to the login page for a better user experience.
      router.push('/login');
    } catch (error) {
      console.error("Error signing out: ", error);
    }
  };

  return (
    // Pass the new signOut function in the context value
    <AuthContext.Provider value={{ user, loading, signOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);