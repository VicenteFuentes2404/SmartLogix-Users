package com.fullstack.users.singleton;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fullstack.users.model.User;
import com.fullstack.users.model.UserStatus;

public class LoginSingletonManager {
    

    //instancia única privada y estatica
    private static LoginSingletonManager instance = null;
    private int totalLoginAttempts;
    private LocalDateTime InitializationDate;

    //constructor privado
    private LoginSingletonManager() {
        this.totalLoginAttempts = 0;
        this.InitializationDate = LocalDateTime.now();

        System.out.println("LoginSingletonManager inicializado");
    }

    //metodo publico estatico para obtener la unica instancia
    //si existe la instancia la devuelve, si no existe la crea y luego la devuelve
    public static LoginSingletonManager getInstance() {
        if (instance == null) {
            synchronized (LoginSingletonManager.class) {
                if (instance == null) {
                    instance = new LoginSingletonManager();
                }
            }
        }

        return instance;
    }

    public boolean validateLogin(User user, String passwordEntered, PasswordEncoder passwordEncoder) {
        registerValidateLogin();

        if (user == null) {
            return false;
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }

        //contraseña encriptada, se compara la contraseña ingresada con la contraseña almacenada en la base de datos
        return passwordEncoder.matches(passwordEntered, user.getPassword());
    }

    private synchronized void registerValidateLogin() {
        this.totalLoginAttempts++;
    }

    public int getTotalLoginAttempts() {
        return totalLoginAttempts;
    }

    public LocalDateTime getInitializationDate() {
        return InitializationDate;
    }
}