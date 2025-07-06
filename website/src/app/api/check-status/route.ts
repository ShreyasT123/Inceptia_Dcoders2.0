import { NextResponse } from 'next/server';
import { initializeApp, getApps, cert } from 'firebase-admin/app';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';

// --- ROBUST INITIALIZATION ---
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY as string);

if (!getApps().length) {
  initializeApp({
    credential: cert(serviceAccount)
  });
}
// ----------------------------

export async function POST() {
  console.log("--- /api/check-status POST request received ---");
  
  const db = getFirestore();
  const now = Timestamp.now();
  // Threshold: 2 minutes (120 seconds) for the demo.
  // In a real app, this would be longer, e.g., 5-10 minutes.
  const thresholdSeconds = 30; 

  let escalatedCount = 0;

  try {
    const sessionsRef = db.collection('sos_sessions');
    // 1. Get all sessions that are currently 'ACTIVE'
    const snapshot = await sessionsRef.where('status', '==', 'ACTIVE').get();

    if (snapshot.empty) {
      console.log("No active sessions found to check.");
      return NextResponse.json({ message: "No active sessions to check.", escalatedCount: 0 });
    }

    // 2. Iterate through each active session
    const batch = db.batch(); // Use a batch for efficient writes
    snapshot.forEach(doc => {
      const session = doc.data();
      const lastHeartbeat = session.lastHeartbeat as Timestamp;
      const secondsSinceHeartbeat = now.seconds - lastHeartbeat.seconds;

      console.log(`Checking session ${doc.id}: Last heartbeat was ${secondsSinceHeartbeat}s ago.`);

      // 3. Check if the last heartbeat is older than our threshold
      if (secondsSinceHeartbeat > thresholdSeconds) {
        console.log(`ESCALATING session ${doc.id}! Threshold of ${thresholdSeconds}s exceeded.`);
        const sessionToUpdateRef = sessionsRef.doc(doc.id);
        batch.update(sessionToUpdateRef, { status: 'ESCALATED_SIGNAL_LOST' });
        escalatedCount++;
      }
    });

    // 4. Commit all the updates to Firestore at once
    await batch.commit();

    if (escalatedCount > 0) {
      return NextResponse.json({ message: `Scan complete. Escalated ${escalatedCount} session(s).` });
    } else {
      return NextResponse.json({ message: "Scan complete. No sessions exceeded the time threshold." });
    }

  } catch (error: any) {
    console.error("Error in /api/check-status:", error);
    return NextResponse.json({ error: 'Internal Server Error', details: error.message }, { status: 500 });
  }
}