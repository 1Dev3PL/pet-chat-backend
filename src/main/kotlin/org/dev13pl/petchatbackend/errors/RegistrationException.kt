package org.dev13pl.petchatbackend.errors

class RegistrationException : RuntimeException("User with such email is already exists")