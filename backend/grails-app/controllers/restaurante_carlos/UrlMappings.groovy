package restaurante_carlos

class UrlMappings {

    static mappings = {
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

        group "/sale", {  
            get "/list"(controller: "sale", action: "listSales") 
            get "/date"(controller: "sale", action: "getSalesByDateRange")
            get "/$customerOrderId"(controller: "sale", action: "getSalesByCustomerOrder")
            group "/$uuid", {
                get "/info"(controller: "sale", action: "saleInfo")  
                patch "/edit"(controller: "sale", action: "updateSale")  
                patch "/payed"(controller: "sale", action: "updateSaleStatus"){  
                    status = 1 
                }
            }
        }

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }

}