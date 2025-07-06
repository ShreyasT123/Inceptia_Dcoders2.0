// 'use client';

// import { useEffect, useState } from "react";

// interface DisasterEvent {
//   event_id: string;
//   event_name: string;
//   date: string;
//   proximity_severity_level: string;
// }

// const DisasterFeed = () => {
//   const [disasters, setDisasters] = useState<DisasterEvent[]>([]);
//   const [isLoading, setIsLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);

//   useEffect(() => {
//     const fetchDisasterData = async () => {
//       setIsLoading(true);
//       setError(null);
//       try {
//         const lat = 19.0760;
//         const lng = 72.8777;

//         // Call OUR internal API route
//         const response = await fetch(`/api/disasters?lat=${lat}&lng=${lng}`);

//         if (!response.ok) {
//           throw new Error(`Error: ${response.statusText}`);
//         }
//         const data = await response.json();
//         if (data.result && Array.isArray(data.result)) {
//           setDisasters(data.result);
//         } else {
//           setDisasters([]);
//         }
//       } catch (err: any) {
//         setError(err.message);
//       } finally {
//         setIsLoading(false);
//       }
//     };

//     fetchDisasterData();
//   }, []);

//   return (
//     <div className="bg-gray-800 p-4 rounded-lg shadow-2xl h-full">
//       <h2 className="text-2xl font-bold mb-4">Disaster Feed (Mumbai)</h2>
//       <div className="max-h-[60vh] overflow-y-auto pr-2">
//         {isLoading && <p>Loading disaster alerts...</p>}
//         {error && <p className="text-red-400">Could not load feed: {error}</p>}
//         {!isLoading && !error && disasters.length === 0 && <p>No recent disasters reported nearby.</p>}
//         <ul className="space-y-3">
//           {disasters.map((event) => (
//             <li key={event.event_id} className="bg-gray-700/50 p-3 rounded-md border-l-4 border-orange-400">
//               <p className="font-semibold">{event.event_name}</p>
//               <div className="text-sm text-gray-300 flex justify-between mt-1">
//                 <span>{new Date(event.date).toLocaleDateString()}</span>
//                 <span className="font-bold">{event.proximity_severity_level}</span>
//               </div>
//             </li>
//           ))}
//         </ul>
//       </div>
//     </div>
//   );
// };

// export default DisasterFeed;
'use client';

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Rss } from "lucide-react";

interface DisasterEvent {
  event_id: string;
  event_name: string;
  date: string;
  proximity_severity_level: string;
}

// NO PROPS NEEDED ANYMORE
const DisasterFeed = () => {
  const [disasters, setDisasters] = useState<DisasterEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDisasterData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        // Just call our new "smart" endpoint. No parameters needed.
        const response = await fetch(`/api/disaster-feed`);

        if (!response.ok) {
          throw new Error(`Error: ${response.statusText}`);
        }
        const data = await response.json();
        if (data.result && Array.isArray(data.result)) {
          setDisasters(data.result);
        } else {
          setDisasters([]);
        }
      } catch (err: any) {
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDisasterData();

    // Optional: Refresh the disaster feed every 5 minutes
    const intervalId = setInterval(fetchDisasterData, 5 * 60 * 1000);

    return () => clearInterval(intervalId); // Cleanup interval on component unmount
  }, []); // Empty dependency array, runs once on mount (and then on interval)

  return (
    <Card className="bg-[#18213a]">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Rss className="h-5 w-5" />
          {/* Title now reflects the dynamic nature */}
          Area Disaster Feed
        </CardTitle>
      </CardHeader>
      <CardContent className="max-h-[60vh] overflow-y-auto pr-4">
        {isLoading && <p className="text-zinc-400">Loading relevant disaster alerts...</p>}
        {error && <p className="text-red-400">Could not load feed: {error}</p>}
        {!isLoading && !error && disasters.length === 0 && <p className="text-zinc-400">No recent disasters reported near active users.</p>}
        
        <ul className="space-y-3">
          {disasters.map((event) => (
            <li key={event.event_id} className="bg-secondary/30 p-3 rounded-md border-l-4 border-orange-400">
              <p className="font-semibold">{event.event_name}</p>
              <div className="text-sm text-muted-foreground flex justify-between mt-1">
                <span>{new Date(event.date).toLocaleDateString()}</span>
                <span className="font-bold">{event.proximity_severity_level}</span>
              </div>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  );
};

export default DisasterFeed;