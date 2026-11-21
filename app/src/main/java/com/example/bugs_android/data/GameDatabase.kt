
package com.example.bugs_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ScoreEntity::class,
        TournamentResultEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun userDao(): UserDAO

    abstract fun scoreDao(): ScoreDAO

    abstract fun tournamentResultDao(): TournamentResultDAO

    companion object {

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tournament_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tournamentId TEXT NOT NULL,
                        tournamentDate INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        userName TEXT NOT NULL,
                        score INTEGER NOT NULL,
                        hits INTEGER NOT NULL,
                        misses INTEGER NOT NULL,
                        accuracy INTEGER NOT NULL
                    )
                """)
            }
        }

        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "bugs_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
