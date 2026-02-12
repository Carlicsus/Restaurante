package com.ordenaris

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Log {
    private static Logger log = LoggerFactory.getLogger(Log)
    public static String TRACE = "TRACE" // Información muy detallada, útil para depuración profunda.
    public static String DEBUG = "DEBUG" // Información de depuración, útil durante el desarrollo.
    public static String INFO = "INFO" // Información general sobre el funcionamiento de la aplicación.
    public static String WARN = "WARN" // Advertencias sobre situaciones inesperadas, pero que no impiden el funcionamiento.
    public static String ERROR = "ERROR" // Errores que han ocurrido y que pueden afectar el funcionamiento.

    public static logger(logLevel, logId, process, description, info = null, res = null) {
        String app = System.getProperty("appName")
        def logInfo = "| $logId | $app | $process | $description"
        if (info) logInfo += " | $info"
        if (res) logInfo += " | $res"      
        log."${logLevel.toLowerCase()}"(logInfo)
    }
}