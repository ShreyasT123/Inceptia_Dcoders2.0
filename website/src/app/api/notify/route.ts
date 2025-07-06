import { NextResponse } from 'next/server';
import admin from 'firebase-admin';

// --- ROBUST INITIALIZATION BLOCK ---
// This code runs ONCE when the server starts, not on every request.
try {
  console.log("initinggggggggggg")
  if (!admin.apps.length) {

    console.log("Initializing Firebase Admin SDK...");
    const serviceAccountString = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;
    if (!serviceAccountString) {
      throw new Error("CRITICAL: FIREBASE_SERVICE_ACCOUNT_KEY environment variable is not set.");
    }
    
    admin.initializeApp({
      credential: admin.credential.cert(JSON.parse(serviceAccountString))
    });
    console.log("Firebase Admin SDK initialized successfully.");
  }
} catch (error: any) {
  console.error("CRITICAL: Firebase Admin SDK initialization failed.", error.message);
}
// ------------------------------------

export async function POST(request: Request) {
  console.log("--- /api/notify POST request received ---"); // This is your new "trying" log

  try {
    const body = await request.json();
    const { userId, title, body: messageBody } = body;

    if (!userId || !title || !messageBody) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    const db = admin.firestore();
    const messaging = admin.messaging();

    const tokensSnapshot = await db.collection('fcmTokens').where('userId', '==', userId).get();
    
    if (tokensSnapshot.empty) {
      console.log(`No FCM tokens found for user: ${userId}`);
      return NextResponse.json({ message: `No devices registered for user: ${userId}.` }, { status: 404 });
    }

    const tokens = tokensSnapshot.docs.map(doc => doc.data().token);
    
    const message = {
      notification: { title, body: messageBody },
      tokens: tokens,
    };

    const response = await messaging.sendEachForMulticast(message);
    
    const successCount = response.responses.filter(r => r.success).length;
    console.log(`${successCount} messages were sent successfully to user ${userId}`);
    
    return NextResponse.json({ success: true, message: `Notification sent to ${successCount} device(s).` });

  } catch (error: any) {
    console.error("Error inside POST /api/notify:", error);
    return NextResponse.json({ error: 'Internal Server Error', details: error.message }, { status: 500 });
  }
}