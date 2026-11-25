package mx.edu.utez.mirecetario.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import mx.edu.utez.mirecetario.domain.model.Photo
import mx.edu.utez.mirecetario.domain.model.Recipe
import mx.edu.utez.mirecetario.domain.model.RecipeInput
import mx.edu.utez.mirecetario.domain.model.User
import java.util.UUID

class RecetarioDb(context: Context) : SQLiteOpenHelper(context, "recetario.db", null, VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                avatar_path TEXT,
                created_at INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE sessions(
                token TEXT PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                created_at INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE recipes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                title TEXT NOT NULL,
                difficulty INTEGER NOT NULL DEFAULT 2,
                prep_minutes INTEGER NOT NULL DEFAULT 0,
                cook_minutes INTEGER NOT NULL DEFAULT 0,
                bake_minutes INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE ingredients(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
                text TEXT NOT NULL,
                position INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE instructions(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
                text TEXT NOT NULL,
                position INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE photos(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
                path TEXT NOT NULL,
                mime TEXT NOT NULL DEFAULT 'image/jpeg',
                position INTEGER NOT NULL)"""
        )
        db.execSQL("CREATE INDEX idx_recipes_user ON recipes(user_id)")
        db.execSQL("CREATE INDEX idx_ingredients_recipe ON ingredients(recipe_id)")
        db.execSQL("CREATE INDEX idx_instructions_recipe ON instructions(recipe_id)")
        db.execSQL("CREATE INDEX idx_photos_recipe ON photos(recipe_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN avatar_path TEXT")
        }
    }

    companion object {
        const val VERSION = 2
    }
}

class UserRow(val user: User, val passwordHash: String, val salt: String)

private const val SESSION_TTL_MS = 30L * 24 * 60 * 60 * 1000

/** Acceso a datos (CRUD). Solo lo usa el servidor local; las pantallas nunca llaman aquí. */
class RecetarioDao(private val helper: RecetarioDb) {

    // ---- Usuarios y sesiones ----

    /** Devuelve null si el correo ya existe. */
    fun createUser(name: String, email: String, hash: String, salt: String): Long? {
        val values = ContentValues().apply {
            put("name", name); put("email", email); put("password_hash", hash)
            put("salt", salt); put("created_at", System.currentTimeMillis())
        }
        return try {
            helper.writableDatabase.insertOrThrow("users", null, values)
        } catch (e: SQLiteConstraintException) {
            null
        }
    }

    fun findUserByEmail(email: String): UserRow? =
        helper.readableDatabase.rawQuery(
            "SELECT id,name,email,avatar_path,password_hash,salt FROM users WHERE email = ?", arrayOf(email)
        ).use { c ->
            if (c.moveToFirst()) UserRow(
                user = User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3)),
                passwordHash = c.getString(4),
                salt = c.getString(5)
            )
            else null
        }

    fun getUser(id: Long): User? =
        helper.readableDatabase.rawQuery("SELECT id,name,email,avatar_path FROM users WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3)) else null
        }

    fun updateUserAvatar(userId: Long, avatarPath: String?): Boolean {
        val values = ContentValues().apply {
            if (avatarPath == null) putNull("avatar_path") else put("avatar_path", avatarPath)
        }
        return helper.writableDatabase.update("users", values, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun createSession(userId: Long): String {
        val token = UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "")
        val values = ContentValues().apply {
            put("token", token); put("user_id", userId); put("created_at", System.currentTimeMillis())
        }
        helper.writableDatabase.insertOrThrow("sessions", null, values)
        return token
    }

    /** Devuelve el usuario de una sesión vigente; null si no existe o caducó (30 días desde su creación). */
    fun userIdForToken(token: String): Long? =
        helper.readableDatabase.rawQuery(
            "SELECT user_id FROM sessions WHERE token = ? AND created_at > ?",
            arrayOf(token, (System.currentTimeMillis() - SESSION_TTL_MS).toString())
        ).use { c ->
            if (c.moveToFirst()) c.getLong(0) else null
        }

    fun purgeExpiredSessions() {
        helper.writableDatabase.delete(
            "sessions", "created_at <= ?", arrayOf((System.currentTimeMillis() - SESSION_TTL_MS).toString())
        )
    }

    fun deleteSession(token: String) {
        helper.writableDatabase.delete("sessions", "token = ?", arrayOf(token))
    }

    // ---- Recetas ----

    fun listRecipes(userId: Long): List<Recipe> {
        val ids = helper.readableDatabase.rawQuery(
            "SELECT id FROM recipes WHERE user_id = ? ORDER BY updated_at DESC", arrayOf(userId.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }
        return ids.mapNotNull { getRecipe(it) }
    }

    fun getRecipe(id: Long): Recipe? {
        val db = helper.readableDatabase
        val args = arrayOf(id.toString())
        val base = db.rawQuery(
            "SELECT user_id,title,difficulty,prep_minutes,cook_minutes,bake_minutes,updated_at FROM recipes WHERE id = ?", args
        ).use { c ->
            if (!c.moveToFirst()) return null
            Recipe(
                id, c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5),
                emptyList(), emptyList(), emptyList(), c.getLong(6)
            )
        }
        return base.copy(
            ingredients = texts(db, "ingredients", args),
            instructions = texts(db, "instructions", args),
            photos = db.rawQuery("SELECT id,path,position FROM photos WHERE recipe_id = ? ORDER BY position,id", args).use { c ->
                buildList { while (c.moveToNext()) add(Photo(c.getLong(0), c.getString(1), c.getInt(2))) }
            }
        )
    }

    private fun texts(db: SQLiteDatabase, table: String, args: Array<String>): List<String> =
        db.rawQuery("SELECT text FROM $table WHERE recipe_id = ? ORDER BY position,id", args).use { c: Cursor ->
            buildList { while (c.moveToNext()) add(c.getString(0)) }
        }

    fun insertRecipe(userId: Long, input: RecipeInput): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            val values = recipeValues(input).apply {
                put("user_id", userId); put("created_at", now); put("updated_at", now)
            }
            val id = db.insertOrThrow("recipes", null, values)
            replaceTexts(db, id, input)
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun updateRecipe(id: Long, input: RecipeInput) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val values = recipeValues(input).apply { put("updated_at", System.currentTimeMillis()) }
            db.update("recipes", values, "id = ?", arrayOf(id.toString()))
            db.delete("ingredients", "recipe_id = ?", arrayOf(id.toString()))
            db.delete("instructions", "recipe_id = ?", arrayOf(id.toString()))
            replaceTexts(db, id, input)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun recipeValues(i: RecipeInput) = ContentValues().apply {
        put("title", i.title); put("difficulty", i.difficulty)
        put("prep_minutes", i.prepMinutes); put("cook_minutes", i.cookMinutes); put("bake_minutes", i.bakeMinutes)
    }

    private fun replaceTexts(db: SQLiteDatabase, recipeId: Long, input: RecipeInput) {
        input.ingredients.forEachIndexed { pos, text -> insertText(db, "ingredients", recipeId, text, pos) }
        input.instructions.forEachIndexed { pos, text -> insertText(db, "instructions", recipeId, text, pos) }
    }

    private fun insertText(db: SQLiteDatabase, table: String, recipeId: Long, text: String, pos: Int) {
        db.insertOrThrow(table, null, ContentValues().apply {
            put("recipe_id", recipeId); put("text", text); put("position", pos)
        })
    }

    /** Borra la receta (las filas hijas caen en cascada) y devuelve las rutas de sus fotos. */
    fun deleteRecipe(id: Long): List<String> {
        val paths = getRecipe(id)?.photos?.map { it.path }.orEmpty()
        helper.writableDatabase.delete("recipes", "id = ?", arrayOf(id.toString()))
        return paths
    }

    // ---- Fotografías ----

    fun addPhoto(recipeId: Long, path: String): Photo {
        val db = helper.writableDatabase
        val pos = db.rawQuery("SELECT COALESCE(MAX(position),-1)+1 FROM photos WHERE recipe_id = ?", arrayOf(recipeId.toString()))
            .use { c -> c.moveToFirst(); c.getInt(0) }
        val id = db.insertOrThrow("photos", null, ContentValues().apply {
            put("recipe_id", recipeId); put("path", path); put("position", pos)
        })
        return Photo(id, path, pos)
    }

    fun allPhotoPaths(): Set<String> =
        helper.readableDatabase.rawQuery("SELECT path FROM photos", null).use { c ->
            buildSet { while (c.moveToNext()) add(c.getString(0)) }
        }

    fun allAvatarPaths(): Set<String> =
        helper.readableDatabase.rawQuery("SELECT avatar_path FROM users WHERE avatar_path IS NOT NULL AND avatar_path != ''", null).use { c ->
            buildSet { while (c.moveToNext()) c.getString(0)?.let { add(it) } }
        }

    fun getPhoto(recipeId: Long, photoId: Long): Photo? =
        helper.readableDatabase.rawQuery(
            "SELECT id,path,position FROM photos WHERE id = ? AND recipe_id = ?",
            arrayOf(photoId.toString(), recipeId.toString())
        ).use { c -> if (c.moveToFirst()) Photo(c.getLong(0), c.getString(1), c.getInt(2)) else null }

    fun deletePhoto(photoId: Long) {
        helper.writableDatabase.delete("photos", "id = ?", arrayOf(photoId.toString()))
    }
}
