import * as admin from 'firebase-admin';

// Decode the base64 service account key from environment variables
const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64
  ? Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_BASE64, 'base64').toString('utf-8')
  : '';

// Ensure the Admin SDK is initialized only once
if (!admin.apps.length) {
  try {
    if (!serviceAccountJson) {
        throw new Error("FIREBASE_SERVICE_ACCOUNT_BASE64 env variable is not set.");
    }
    admin.initializeApp({
      credential: admin.credential.cert(JSON.parse(serviceAccountJson)),
      databaseURL: process.env.NEXT_PUBLIC_FIREBASE_DATABASE_URL,
    });
    console.log("Firebase Admin SDK initialized successfully.");
  } catch (error: any) {
    console.error("Firebase Admin SDK initialization error:", error.message);
  }
}

// Export the initialized admin services for use in your API routes
export const adminDb = admin.firestore();
export const adminMessaging = admin.messaging();
export default admin;