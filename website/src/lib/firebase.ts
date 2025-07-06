// Import the functions you need from the SDKs you need
import { getApps, initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyBirDeEUOSGIz5KxWgCStuajzZOh5i7V4Q",
  authDomain: "resqmesh.firebaseapp.com",
  projectId: "resqmesh",
  storageBucket: "resqmesh.firebasestorage.app",
  messagingSenderId: "485062109155",
  appId: "1:485062109155:web:89059d073a519f814362d6",
  measurementId: "G-DKBJVMYQWS"
};

// Initialize Firebase
// Initialize Firebase
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
const db = getFirestore(app);

export { db ,app};