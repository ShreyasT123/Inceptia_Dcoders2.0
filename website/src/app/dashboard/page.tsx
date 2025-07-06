'use client';

import { useState, useEffect, useMemo } from 'react';
import { db } from '@/lib/firebase';
import { collection, onSnapshot, query, QueryDocumentSnapshot, DocumentData } from 'firebase/firestore';
import SosMap from '@/components/SosMap';
import DisasterFeed from '@/components/DisasterFeed';
import { SosSession } from '@/components/SosMap'; // Use the central types file
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Users, ShieldAlert, HeartPulse, Rss, Map as MapIcon, Siren, MapPin, BatteryCharging, User, Clock, Timer } from 'lucide-react';
import { Button } from '@/components/ui/button';

// --- HELPER FUNCTION FOR TIME AGO ---
function timeAgo(date: Date): string {
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);
    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + " years ago";
    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + " months ago";
    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + " days ago";
    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + " hours ago";
    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + " minutes ago";
    return Math.floor(seconds) + " seconds ago";
}


export default function DashboardPage() {
    const [allSessions, setAllSessions] = useState<SosSession[]>([]);
    const [page, setPage] = useState(2); // Default to the new page
    const [isScanning, setIsScanning] = useState(false);

    // This useEffect fetches all sessions
    useEffect(() => {
        const q = query(collection(db, 'sos_sessions'));
        const unsubscribe = onSnapshot(q, (querySnapshot) => {
            const sessionsData: SosSession[] = [];
            querySnapshot.forEach((doc: QueryDocumentSnapshot<DocumentData>) => {
                sessionsData.push({ id: doc.id, ...doc.data() } as SosSession);
            });
            setAllSessions(sessionsData);
        });
        return () => unsubscribe();
    }, []);

    // This useEffect fetches location names for new sessions
 // Rerun when sessions update

    // --- DATA GROUPING LOGIC using useMemo for performance ---
const groupedByStatus = useMemo(() => {
    return allSessions.reduce((acc, session) => {
        const status = session.status;
        if (status === 'RESOLVED') return acc; // Ignore resolved sessions

        if (!acc[status]) {
            acc[status] = [];
        }
        acc[status].push(session);
        return acc;
    }, {} as Record<string, SosSession[]>);
}, [allSessions]);



    const activeCount = allSessions.filter(s => s.status === 'ACTIVE').length;
    const resolvedCount = allSessions.filter(s => s.status === 'RESOLVED').length;
    const escalatedCount = allSessions.filter(s => s.status === 'ESCALATED_SIGNAL_LOST').length;

  const handleSendNotification = async (session: SosSession) => {
    // Add a guard clause to prevent sending if there's no user ID
    if (!session.userId) {
        alert('Error: This session has no associated User ID. Cannot send notification.');
        return;
    }

    console.log(`Sending notification to user: ${session.userId}`);
    
    // You could add a loading state here for the specific button if you want
    // E.g., setLoadingNotification(session.id, true);

    try {
        const title = "Urgent: From ResQ-Dash Control";
        const body = "Please check your app for an important message from the emergency response team.";
        
        const response = await fetch('/api/notify', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ userId: session.userId, title, body }),
        });

        // The response from our API route might not be JSON if an error occurred.
        // We must check the 'ok' status first.
        if (!response.ok) {
            // Try to get a specific error message from the API, otherwise use statusText
            let errorMessage = `HTTP error! Status: ${response.status} ${response.statusText}`;
            try {
                // Our API route returns a JSON object with a 'message' or 'error' key on failure
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch (jsonError) {
                // The body was not JSON, which can happen. Use the default HTTP error message.
                console.log("Response was not JSON, using status text for error.");
            }
            throw new Error(errorMessage);
        }
        
        // If the response was successful, we expect a JSON body with a success message
        const result = await response.json();
        console.log("API Response:", result);
        alert(result.message || 'Notification sent successfully!'); // Show the success message from the API

    } catch (error: any) {
        console.error("Notification Error:", error);
        // Display the detailed error message to the operator
        alert(`Failed to send notification: ${error.message}`);
    } finally {
        // Here you would set the specific button's loading state back to false
        // E.g., setLoadingNotification(session.id, false);
    }
  };

  // --- NEW FUNCTION for the auto-escalate feature ---
  const handleCheckStatus = async () => {
    setIsScanning(true);
    try {
      const response = await fetch('/api/check-status', {
        method: 'POST',
      });
      const result = await response.json();
      alert(result.message); // Give feedback to the operator
    } catch (error) {
      console.error("Failed to check statuses:", error);
      alert("An error occurred while scanning for lost signals.");
    } finally {
      setIsScanning(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#10172a] text-zinc-100 flex flex-col">
      {/* Header */}
      <header className="w-full px-8 py-6 bg-[#0d1424] flex flex-col gap-1 shadow-md">
        <h1 className="text-2xl md:text-3xl font-bold flex items-center gap-2">
          <ShieldAlert className="inline-block h-7 w-7 text-orange-400" />
          Emergency Command Center
        </h1>
        <span className="text-zinc-400 text-sm">Resqmesh Management System</span>
      </header>

      {/* Tab Navigation */}
      <div className="flex gap-2 px-8 pt-4 pb-2 bg-[#10172a] border-b border-b-zinc-800">
        <button className={`px-4 py-2 rounded-t-md font-semibold flex items-center gap-2 ${page === 1 ? 'bg-[#18213a] text-orange-400' : 'bg-[#10172a] text-zinc-300 hover:bg-[#18213a]'}`} onClick={() => setPage(1)}>
            <MapIcon size={16} /> SOS & Map
        </button>
        <button className={`px-4 py-2 rounded-t-md font-semibold flex items-center gap-2 ${page === 2 ? 'bg-[#18213a] text-blue-400' : 'bg-[#10172a] text-zinc-300 hover:bg-[#18213a]'}`} onClick={() => setPage(2)}>
            <Users size={16} /> Live Sessions
        </button>
        <button className={`px-4 py-2 rounded-t-md font-semibold flex items-center gap-2 ${page === 3 ? 'bg-[#18213a] text-yellow-400' : 'bg-[#10172a] text-zinc-300 hover:bg-[#18213a]'}`} onClick={() => setPage(3)}>
            <Rss size={16} /> Disaster Alerts
        </button>
      </div>

      {/* Page Content */}
      <main className="flex-1 px-8 py-8 bg-[#10172a]">
        {/* --- PAGE 1: MAP & STATS --- */}
        {page === 1 && (
            // ... (Your page 1 layout code is great, no changes needed)
            <div className="flex flex-col gap-8">
                {/* Status Bar */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card className="bg-[#18213a] text-zinc-100"><CardHeader className="flex flex-row items-center justify-between pb-2"><CardTitle className="text-sm font-medium">ACTIVE SOS</CardTitle><Users className="h-5 w-5 text-yellow-400" /></CardHeader><CardContent><div className="text-3xl font-bold text-yellow-400">{activeCount}</div></CardContent></Card>
                  <Card className="bg-[#18213a] text-zinc-100"><CardHeader className="flex flex-row items-center justify-between pb-2"><CardTitle className="text-sm font-medium">CRITICAL</CardTitle><ShieldAlert className="h-5 w-5 text-red-500" /></CardHeader><CardContent><div className="text-3xl font-bold text-red-500">{escalatedCount}</div></CardContent></Card>
                  <Card className="bg-[#18213a] text-zinc-100"><CardHeader className="flex flex-row items-center justify-between pb-2"><CardTitle className="text-sm font-medium">RESOLVED</CardTitle><HeartPulse className="h-5 w-5 text-green-400" /></CardHeader><CardContent><div className="text-3xl font-bold text-green-400">{resolvedCount}</div></CardContent></Card>
                </div>
                {/* Map Section */}
                <Card className="bg-[#18213a] text-zinc-100 flex-1"><CardHeader><CardTitle>Live Emergency Map</CardTitle></CardHeader><CardContent><div className="rounded-lg overflow-hidden shadow-2xl bg-zinc-800 min-h-[50vh]"><SosMap /></div></CardContent></Card>
            </div>
        )}
        
              {/* --- NEW, DETAILED PAGE 2 UI --- */}
                {page === 2 && (
                    <div className="flex flex-col gap-8">
                        {/* System Operations Card remains the same */}
                        <Card className="bg-[#18213a] text-zinc-100">
                            <CardHeader><CardTitle>System Operations</CardTitle></CardHeader>
                            <CardContent>
                                <Button onClick={handleCheckStatus} disabled={isScanning}><Siren className="mr-2 h-4 w-4" />{isScanning ? 'Scanning...' : 'Scan for Lost Signals'}</Button>
                            </CardContent>
                        </Card>

                        {/* Iterate over status groups: CRITICAL, then ACTIVE */}
                        {['ESCALATED_SIGNAL_LOST', 'ACTIVE'].map(status => (
                            groupedByStatus[status] && (
                                <div key={status}>
                                    {/* Section Header */}
                                    <div className={`p-2 rounded-t-lg flex items-center gap-3 ${status === 'ESCALATED_SIGNAL_LOST' ? 'bg-red-900/50' : 'bg-yellow-900/50'}`}>
                                        {status === 'ESCALATED_SIGNAL_LOST' ? <ShieldAlert className="text-red-400"/> : <Users className="text-yellow-400"/>}
                                        <h2 className={`text-xl font-bold ${status === 'ESCALATED_SIGNAL_LOST' ? 'text-red-400' : 'text-yellow-400'}`}>
                                            {status.replace('_', ' ')} Sessions ({groupedByStatus[status].length})
                                        </h2>
                                    </div>
                                    
                                    {/* Grid for User "Data Sheet" Cards */}
                                    <div className="grid grid-cols-1 xl:grid-cols-2 2xl:grid-cols-3 gap-4 p-4 bg-[#18213a]/50 rounded-b-lg">
                                        {groupedByStatus[status].map(session => {
                                            const jsStartTime = session.startTime ? new Date(session.startTime.seconds * 1000) : null;
                                            const jsLastHeartbeat = session.lastHeartbeat ? new Date(session.lastHeartbeat.seconds * 1000) : null;
                                            
                                            return (
                                                <Card key={session.id} className="bg-[#0d1424] border-zinc-700 flex flex-col">
                                                    <CardHeader className="flex-row items-center justify-between p-4">
                                                        <div className="flex items-center gap-3">
                                                           <div className="p-2 bg-zinc-700 rounded-full"><User className="h-5 w-5 text-zinc-300"/></div>
                                                           <CardTitle className="text-base font-mono">{session.userId}</CardTitle>
                                                        </div>
                                                        <Button variant="destructive" size="sm" onClick={() => handleSendNotification(session)}>Alert</Button>
                                                    </CardHeader>
                                                    <CardContent className="p-4 pt-0 space-y-3 text-sm flex-grow">
                                                        {/* Timing Info */}
                                                        <div className="flex items-center gap-3 text-zinc-400">
                                                            <Clock size={16}/>
                                                            <span>Started: {jsStartTime ? jsStartTime.toLocaleTimeString() : 'N/A'}</span>
                                                        </div>
                                                        <div className="flex items-center gap-3 text-zinc-400">
                                                            <Timer size={16}/>
                                                            <span>Last Signal: {jsLastHeartbeat ? timeAgo(jsLastHeartbeat) : 'N/A'}</span>
                                                        </div>
                                                        {/* Location Info */}
                                                        <div className="flex items-center gap-3 text-zinc-400">
                                                            <MapPin size={16}/>
                                                            {session.currentLocation ? (
                                                                <a href={`https://maps.google.com/?q=${session.currentLocation.latitude},${session.currentLocation.longitude}`} target="_blank" rel="noopener noreferrer" className="text-blue-400 hover:underline">
                                                                    {session.currentLocation.latitude.toFixed(4)}, {session.currentLocation.longitude.toFixed(4)}
                                                                </a>
                                                            ) : (
                                                                <span>Location N/A</span>
                                                            )}
                                                        </div>
                                                        {/* Battery Info (optional) */}
                                                        {session.batteryLevel && (
                                                            <div className="flex items-center gap-3 text-zinc-400">
                                                                <BatteryCharging size={16} className={session.batteryLevel <= 20 ? 'text-red-500' : 'text-green-500'}/>
                                                                <span>Battery: {session.batteryLevel}%</span>
                                                            </div>
                                                        )}
                                                    </CardContent>
                                                </Card>
                                            );
                                        })}
                                    </div>
                                </div>
                            )
                        ))}
                         {Object.keys(groupedByStatus).length === 0 && (
                            <div className="text-center py-10">
                                <HeartPulse className="h-12 w-12 text-green-500 mx-auto mb-4" />
                                <p className="text-zinc-400 text-lg">All Clear!</p>
                                <p className="text-sm text-zinc-500 mt-2">No active or critical sessions at this time.</p>
                            </div>
                         )}
                    </div>
                )}
        {/* --- PAGE 3: DISASTER FEED --- */}
        {page === 3 && (
            <div className="max-w-4xl mx-auto"><DisasterFeed /></div>
        )}
      </main>
    </div>
  );
}