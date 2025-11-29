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

        group "api/order", {
            post "/newOrder"(controller: "ordersModule", action: "newOrder")
            get "/listOrders"(controller: "ordersModule", action: "listOrders")
            group "/$uuid", {
                get "/info"(controller: "ordersModule", action: "orderInfo")
                group "/edit/$uuidDish",{
                    patch "/dish"(controller: "ordersModule", action: "editOrder")
                }
                patch "/edit"(controller: "ordersModule", action: "editOrder")
                patch "/cancel"(controller: "ordersModule", action: "editOrderStatus") {
                    status = "Cancelled"
                }
                patch "/prepare"(controller: "ordersModule", action: "editOrderStatus") {
                    status = "Preparing"
                }
                patch "/finish"(controller: "ordersModule", action: "editOrderStatus") {
                    status = "Finished"
                }
                patch "/queue"(controller: "ordersModule", action: "editOrderStatus") {
                    status = "Queue"
                }
                /*patch "/pend"(controller: "ordersModule", action: "editOrderStatus") {
                    status = "Pending"
                }
                */
            }
        }
        group "api/shoppingCart", {
            get "/list"(controller: "shoppingCart", action: "listOrderShoppingCart")
            post "/new"(controller: "shoppingCart", action: "newOrderShoppingCart")
            group "/$uuidSC", {
                get "/info"(controller: "shoppingCart", action: "shoppingCartInfo")
                post "/addItem"(controller: "shoppingCart", action: "addItemShoppingCart")
                delete "/deleteItem/$uuidDish"(controller: "shoppingCart", action: "deleteItemShoppingCart")
                patch "/finish"(controller: "shoppingCart", action: "editStatusShoppingCart"){
                    status = "Finished"
                }
                delete "/delete"(controller: "shoppingCart", action: "editStatusShoppingCart"){
                    status = "Delete"
                }
            }
        }

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }

}
