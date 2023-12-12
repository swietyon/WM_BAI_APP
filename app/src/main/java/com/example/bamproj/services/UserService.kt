package com.example.bamproj.services
import com.example.bamproj.User
import com.example.bamproj.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class UserService(private val userDao: UserDao) {
    suspend fun registerUser(userName: String, password: String, address: String, phoneNumber: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (isUserNameExists(userName)) {
                return@withContext false
            }

            val hashedPassword = hashPassword(password)

            val newUser = User(userName = userName, password = hashedPassword, address = address, phoneNumber = phoneNumber)
            userDao.insert(newUser)

            return@withContext true
        }
    }

    suspend fun validatePassword(userName: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Operacje bazodanowe
            val user = userDao.getUserByUsername(userName) ?: return@withContext false
            return@withContext BCrypt.checkpw(password, user.password)
        }
    }

    suspend fun isUserNameExists(userName: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Operacje bazodanowe
            val existingUser = userDao.getUserByUsername(userName)
            return@withContext existingUser != null
        }
    }

    public fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}