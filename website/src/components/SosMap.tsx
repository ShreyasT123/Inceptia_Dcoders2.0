// 'use client';

// import { APIProvider, Map, AdvancedMarker, Pin } from '@vis.gl/react-google-maps';
// import { useEffect, useState } from 'react';
// import { db } from '@/lib/firebase';
// import { collection, onSnapshot, query, where, QueryDocumentSnapshot, DocumentData } from 'firebase/firestore';

// // Define the shape of our session data for the map
// export interface SosSession {
//   userId: ReactNode;
//   id: string;
//   status: 'ACTIVE' | 'RESOLVED' | 'ESCALATED_SIGNAL_LOST';
//   currentLocation?: {
//     latitude: number;
//     longitude: number;
//   };
// }

// const SosMap = () => {
//   const [activeSessions, setActiveSessions] = useState<SosSession[]>([]);
//   const initialPosition = { lat: 28.6139, lng: 77.2090 }; // Default center (e.g., New Delhi)

//   useEffect(() => {
//     // Create a query to get ONLY active and escalated sessions
//     const q = query(
//       collection(db, "sos_sessions"), 
//       where("status", "in", ["ACTIVE", "ESCALATED_SIGNAL_LOST"])
//     );

//     // Real-time listener
//     const unsubscribe = onSnapshot(q, (snapshot) => {
//       const sessionsData: SosSession[] = [];
//       snapshot.forEach((doc: QueryDocumentSnapshot<DocumentData>) => {
//         // Only include sessions that have a location
//         if (doc.data().currentLocation) {
//           sessionsData.push({ id: doc.id, ...doc.data() } as SosSession);
//         }
//       });
//       setActiveSessions(sessionsData);
//       console.log("Map updated with active sessions:", sessionsData);
//     });

//     return () => unsubscribe();
//   }, []);

//   if (!process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY) {
//     return <div>Google Maps API Key is missing.</div>;
//   }

//   return (
//     <APIProvider apiKey={process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY}>
//       <div style={{ height: "60vh", width: "100%" }}>
//         <Map
//           defaultCenter={initialPosition}
//           defaultZoom={10}
//           gestureHandling={'greedy'}
//           disableDefaultUI={true}
//           mapId="disaster-map" // Optional: for custom styling
//         >
//           {activeSessions.map(session => (
//             session.currentLocation && (
//               <AdvancedMarker
//                 key={session.id}
//                 position={{ 
//                   lat: session.currentLocation.latitude, 
//                   lng: session.currentLocation.longitude 
//                 }}
//                 title={`User: ${session.id}`}
//               >
//                 {/* 
//                   The Pin component changes color based on the status.
//                   This is a powerful and immediate visual cue for the operator.
//                 */}
//                 <Pin 
//                   background={session.status === 'ESCALATED_SIGNAL_LOST' ? '#C70039' : '#F9C74F'} // Red for escalated, Yellow for active
//                   glyphColor={'#000'}
//                   borderColor={'#000'}
//                 />
//               </AdvancedMarker>
//             )
//           ))}
//         </Map>
//       </div>
//     </APIProvider>
//   );
// };

// export default SosMap;
'use client';

import { APIProvider, Map, AdvancedMarker, Pin } from '@vis.gl/react-google-maps';
import { useEffect, useState } from 'react';
import { db } from '@/lib/firebase';
import { collection, onSnapshot, query, where, QueryDocumentSnapshot, DocumentData } from 'firebase/firestore';

// Define the comprehensive shape of our session data
export interface SosSession {
  id: string;
  userId: string;
  status: 'ACTIVE' | 'RESOLVED' | 'ESCALATED_SIGNAL_LOST';
  currentLocation?: {
    latitude: number;
    longitude: number;
  };
  startTime?: string | Date;
  lastHeartbeat?: string | Date;
  batteryLevel?: number;
  networkStatus?: 'connected' | 'disconnected' | 'poor';
  emergencyType?: string;
  description?: string;
  contactsNotified?: boolean;
  createdAt?: string | Date;
  updatedAt?: string | Date;
}

const SosMap = () => {
  const [activeSessions, setActiveSessions] = useState<SosSession[]>([]);
  const initialPosition = { lat: 28.6139, lng: 77.2090 }; // Default center (e.g., New Delhi)

  useEffect(() => {
    // Create a query to get ONLY active and escalated sessions
    const q = query(
      collection(db, "sos_sessions"), 
      where("status", "in", ["ACTIVE", "ESCALATED_SIGNAL_LOST"])
    );

    // Real-time listener
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const sessionsData: SosSession[] = [];
      snapshot.forEach((doc: QueryDocumentSnapshot<DocumentData>) => {
        // Only include sessions that have a location
        if (doc.data().currentLocation) {
          sessionsData.push({ id: doc.id, ...doc.data() } as SosSession);
        }
      });
      setActiveSessions(sessionsData);
      console.log("Map updated with active sessions:", sessionsData);
    });

    return () => unsubscribe();
  }, []);

  if (!process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY) {
    return <div>Google Maps API Key is missing.</div>;
  }

  return (
    <APIProvider apiKey={process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY}>
      <div style={{ height: "60vh", width: "100%" }}>
        <Map
          defaultCenter={initialPosition}
          defaultZoom={10}
          gestureHandling={'greedy'}
          disableDefaultUI={true}
          mapId="disaster-map" // Optional: for custom styling
        >
          {activeSessions.map(session => (
            session.currentLocation && (
              <AdvancedMarker
                key={session.id}
                position={{ 
                  lat: session.currentLocation.latitude, 
                  lng: session.currentLocation.longitude 
                }}
                title={`User: ${session.userId} | Status: ${session.status}`}
              >
                {/* 
                  The Pin component changes color based on the status.
                  This is a powerful and immediate visual cue for the operator.
                */}
                <Pin 
                  background={session.status === 'ESCALATED_SIGNAL_LOST' ? '#C70039' : '#F9C74F'} // Red for escalated, Yellow for active
                  glyphColor={'#000'}
                  borderColor={'#000'}
                />
              </AdvancedMarker>
            )
          ))}
        </Map>
      </div>
    </APIProvider>
  );
};

export default SosMap;