// src/firebase/config.js
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyCjojcstt0Rf0nkx-mr2nBl3v5vlYxmQ8s",
  authDomain: "amicoffe-25ec1.firebaseapp.com",
  projectId: "amicoffe-25ec1",
  storageBucket: "amicoffe-25ec1.firebasestorage.app",
  messagingSenderId: "867398542261",
  appId: "1:867398542261:web:01a6a5afa4a1e6b4bdeffc",
};

const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);
export const auth = getAuth(app);