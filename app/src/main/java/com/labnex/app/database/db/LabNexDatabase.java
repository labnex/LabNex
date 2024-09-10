package com.labnex.app.database.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.labnex.app.database.dao.AppSettingsDao;
import com.labnex.app.database.dao.NotesDao;
import com.labnex.app.database.dao.ProjectsDao;
import com.labnex.app.database.dao.UserAccountsDao;
import com.labnex.app.database.models.AppSettings;
import com.labnex.app.database.models.Notes;
import com.labnex.app.database.models.Projects;
import com.labnex.app.database.models.UserAccount;

/**
 * @author mmarif
 */
@Database(
		entities = {Projects.class, UserAccount.class, Notes.class, AppSettings.class},
		version = 1,
		exportSchema = false)
public abstract class LabNexDatabase extends RoomDatabase {

	private static final String DB_NAME = "labnex";

	private static volatile LabNexDatabase labNexDatabase;

	/*private static final Migration MIGRATION_1_2 =
	new Migration(1, 2) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE 'userAccounts' ADD COLUMN 'tokenExpiry' TEXT");
		}
	};*/
	public static LabNexDatabase getDatabaseInstance(Context context) {

		if (labNexDatabase == null) {
			synchronized (LabNexDatabase.class) {
				if (labNexDatabase == null) {

					labNexDatabase =
							Room.databaseBuilder(context, LabNexDatabase.class, DB_NAME)
									// .fallbackToDestructiveMigration()
									.allowMainThreadQueries()
									// .addMigrations(MIGRATION_1_2)
									.build();
				}
			}
		}

		return labNexDatabase;
	}

	public abstract ProjectsDao projectsDao();

	public abstract UserAccountsDao userAccountsDao();

	public abstract NotesDao notesDao();

	public abstract AppSettingsDao appSettingsDao();
}
