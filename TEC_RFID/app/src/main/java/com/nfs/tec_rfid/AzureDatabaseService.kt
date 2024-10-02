package com.nfs.tec_rfid
import org.conscrypt.Conscrypt
import java.security.Security
import android.content.Context
import android.database.SQLException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.sql.Connection
import java.sql.DriverManager
import javax.net.ssl.SSLContext

class AzureDatabaseService(private val context: Context) {
    private val TAG = "AzureDatabaseService"
    private val secureStorage = SecureStorage(context)

    suspend fun connectToDatabase(): Connection? {

        secureStorage.saveCredentials("TEC_ADMIN","PROYECTO2024!")
        val user = secureStorage.getUser() ?: return null
        val password = secureStorage.getPassword() ?: return null

        val connectionString = "jdbc:sqlserver://tecrfidserver.database.windows.net:1433;" +
                "database=RFID_TEC;user=$user;password=$password;" +
                "encrypt=true;trustServerCertificate=true;" + // Enable encryption
                "hostNameInCertificate=*.database.windows.net;loginTimeout=30;sslProtocol=TLSv1;"
        // Add Conscrypt as the default security provider
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        Security.getProviders().forEach {
            Log.d("SecurityProvider", "Provider: ${it.name}, Version: ${it.version}")
        }
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting to connect to the database")

                // Load the SQL Server driver
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")

                // Connect using the secure connection string
                DriverManager.getConnection(connectionString).also {
                    Log.d(TAG, "Database connection successful")
                }
            } catch (e: SQLException) {
                Log.e(TAG, "SQL Error connecting to the database", e)
                null
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "Driver class not found", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error connecting to the database", e)
                null
            }
        }
    }
    suspend fun connectWithRetry(retries: Int = 3): Connection? {
        var attempt = 0
        var connection: Connection? = null

        while (attempt < retries) {
            connection = connectToDatabase()
            if (connection != null) {
                return connection
            }
            attempt++
            Log.d(TAG, "Retrying database connection... attempt $attempt")
            delay(2000) // Adding delay between retries
        }

        Log.e(TAG, "Failed to connect to the database after $retries attempts")
        return null
    }

    fun isConnectionValid(connection: Connection?): Boolean {
        return try {
            connection?.isValid(5) ?: false // Checks if connection is valid within 5 seconds
        } catch (e: SQLException) {
            Log.e(TAG, "Connection validation failed", e)
            false
        }
    }

    fun closeConnection(connection: Connection?) {
        try {
            connection?.close()
            Log.d(TAG, "Database connection closed")
        } catch (e: SQLException) {
            Log.e(TAG, "Error closing the database connection", e)
        }
    }
}
