package restaurante_carlos

class UrlMappings {

    static mappings = {
        group "/api",{
            group "/menu", {
                group "/type", {
                    post "/new"(controller: "menu", action: "newType")
                    get "/list"(controller: "menu", action: "listTypes")
                    get "/view"(controller: "menu", action: "paginateTypes")
                    group "/$uuid", {
                        get "/info"(controller: "menu", action: "typeInfo")  
                        patch "/edit"(controller: "menu", action: "editType")  
                        patch "/activate"(controller: "menu", action: "editTypeStatus"){  
                            status = 1  
                        }
                        patch "/deactivate"(controller: "menu", action: "editTypeStatus") {  
                            status = 0  
                        }
                        delete "/delete"(controller: "menu", action: "editTypeStatus") {
                            status = 2
                        }
                    }
                }
            }

            group "/dish", {  
                post "/new"(controller: "platillo", action: "newDish")  
                get "/list"(controller: "platillo", action: "listDishes")  
                get "/view"(controller: "platillo", action: "paginateDishes")  
                group "/$uuid", {
                    get "/info"(controller: "platillo", action: "dishInfo")  
                    patch "/edit"(controller: "platillo", action: "editDish")  
                    patch "/activate"(controller: "platillo", action: "editDishStatus"){  
                        status = 1 
                    }
                    patch "/deactivate"(controller: "platillo", action: "editDishStatus") {  
                        status = 0
                    }
                    delete "/delete"(controller: "platillo", action: "editDishStatus") {
                        status = 2
                    }
                }
            }

            group "/user", {  
                post "/register"(controller: "user", action: "register")
            }
        }

        group "/api/sale", {  
            get "/date/$userId"(controller: "sale", action: "getUserSalesByDateRange")
            get "/pending/$userId"(controller: "sale", action: "getSalesByUser") {
                typeOrder = 1
            }
            get "/payed/$userId"(controller: "sale", action: "getSalesByUser") {
                typeOrder = 0
            }
            get "/$uuid"(controller: "sale", action: "getOneSaleInfo")  
            
        }

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }

}