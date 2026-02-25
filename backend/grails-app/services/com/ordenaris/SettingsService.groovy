package com.ordenaris

import grails.gorm.transactions.Transactional
import grails.web.context.ServletContextHolder as SCH

@Transactional
class SettingsService {
    def servletContext = SCH.servletContext

    def updateInfoDB(logId ){
        Settings.withTransaction{
            try{
                Log.logger(Log.INFO, logId, "Obtener Settings", "Llega al servicio." )
                def obj = [:]
                Settings.list().each{
                    obj["${it.identifier}"] = it.data
                }
                servletContext["Conf"] = obj
                Log.logger(Log.INFO, logId, "Obtener toda la información de la base de datos.", "Fin de solicitud. La información ha sido actualizada.")
                return [ data: [ success: true], status: 200 ]
            }catch(e){
                Log.logger(Log.ERROR, logId, "Obtener toda la información de la base de datos.", "Ha ocurrido un error.", e.getMessage() )
                return TypeError.internalError(logId)
            }
        }
    }
    def registerInitData(){
        Settings.withTransaction{
            try{
                if(!Settings.findByIdentifier(Constants.VALID_EMAILS)) new Settings( [ identifier: Constants.VALID_EMAILS, data: "@ordenaris.com, @innovattia.com, @orquestia.com" ] ).save()
            }catch(e){
                Log.logger(Log.ERROR, null, "Inserción de data inicial.", "Ha ocurrido un error.", e.getMessage() )
            }
        }
    }
    def isValidToRetry = { data ->
        return (servletContext["Conf"].find { key, value -> key.startsWith( Conf.PREFIX_CODE_ALLOWED  ) && value == data })
    }
}
