
package com.example.teamdex_java;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static FirebaseFirestore db;

    public static FirebaseFirestore obtenerDB() {

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        return db;
    }
}
