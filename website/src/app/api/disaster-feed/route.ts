import { NextResponse } from 'next/server';
import { initializeApp, getApps, cert } from 'firebase-admin/app';
import { getFirestore, GeoPoint } from 'firebase-admin/firestore';

// --- ROBUST INITIALIZATION ---
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY as string);

if (!getApps().length) {
  initializeApp({
    credential: cert(serviceAccount)
  });
}
// ----------------------------

export async function GET() {
  console.log("--- /api/disaster-feed GET request received ---");
  const db = getFirestore();

  try {
    // 1. Fetch all ACTIVE and ESCALATED sessions from Firestore
    const sessionsRef = db.collection('sos_sessions');
    const snapshot = await sessionsRef.where('status', 'in', ['ACTIVE', 'ESCALATED_SIGNAL_LOST']).get();

    const sessionsWithLocation = snapshot.docs
      .map(doc => doc.data())
      .filter(session => session.currentLocation && session.currentLocation.latitude && session.currentLocation.longitude);

    let centroid = { lat: 28.6139, lng: 77.2090 }; // Default to New Delhi

    // 2. Calculate the centroid (average location)
    if (sessionsWithLocation.length > 0) {
      const totalLat = sessionsWithLocation.reduce((sum, session) => sum + session.currentLocation.latitude, 0);
      const totalLng = sessionsWithLocation.reduce((sum, session) => sum + session.currentLocation.longitude, 0);
      centroid = {
        lat: totalLat / sessionsWithLocation.length,
        lng: totalLng / sessionsWithLocation.length,
      };
      console.log(`Calculated centroid of ${sessionsWithLocation.length} users:`, centroid);
    } else {
      console.log("No active users with location, using default location for disaster feed.");
    }
    
    // 3. Call the AmbeeData API using the calculated centroid
    const apiKey = process.env.AMBEEDATA_API_KEY;
    if (!apiKey) throw new Error("AmbeeData API key not configured.");

    const ambeeUrl = `https://api.ambeedata.com/disasters/latest/by-lat-lng?lat=${centroid.lat}&lng=${centroid.lng}`;
    
    const ambeeResponse = await fetch(ambeeUrl, {
      headers: { 'x-api-key': apiKey },
    });

    if (!ambeeResponse.ok) {
        throw new Error(`AmbeeData API failed with status ${ambeeResponse.status}`);
    }

    const disasterData = await ambeeResponse.json();

    // 4. Return the final disaster data to the frontend
    return NextResponse.json(disasterData);

  } catch (error: any) {
    console.error("Error in /api/disaster-feed:", error);
    return NextResponse.json({ error: 'Internal Server Error', details: error.message }, { status: 500 });
  }
}