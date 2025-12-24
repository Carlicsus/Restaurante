package restaurante_carlos

class UrlMappings {

    static mappings = {
        
        group "/api",{
            group "/menu", {
                group "/type", {
                    post "/new"(controller: "menu", action: "newType")
                    get "/listMenus"(controller: "menu", action: "listTypes")
                    get "/view"(controller: "menu", action: "paginateTypes")
                    group "/$uuid", {
                        get "/info"(controller: "menu", action: "typeInfo")  
                        get "/listSubMenu"(controller:"menu", action:"listSubmenusByParent")
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
                get "/ranking-dishes"(controller: "platillo", action: "dishRanking")
                group "/$uuid", {
                    get "/info"(controller: "platillo", action: "dishInfo"){
                        status = 1
                    }  
                    get "/clone"(controller:"platillo", action:"dishInfo"){
                    status = 2
                    }
                    post "/clone"(controller:"platillo", action:"newDish"){
                    }
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
                get "/view"(controller: "user", action: "paginateUsers")

                patch "/enable/$username"(controller: "user", action: "setEnabled"){
                    enable=true
                }
                patch "/disable/$username"(controller: "user", action: "setEnabled"){
                    enable=false
                }

                patch "/lock/$username"(controller: "user", action: "setLocked"){
                    lock=true
                }
                patch "/unlock/$username"(controller: "user", action: "setLocked"){
                    lock=false
                }

                get "/me/photo"(controller: "user", action: "myPhoto") 
                post "/me/photo"(controller: "user", action: "uploadPhoto") 

            }

            group "/role", {

                get "/"(controller: "role", action: "index")
                get "/$id"(controller: "role", action: "show")

                post "/"(controller: "role", action: "save")
                put "/$id"(controller: "role", action: "update")
                delete "/$id"(controller: "role", action: "delete")
            }

            group "/user-role", {

                // Obtener todos los roles de un usuario
                get "/user/$userId"(controller: "userRole", action: "getRolesByUser")

                // Asignar un rol a un usuario
                post "/"(controller: "userRole", action: "assignRole")

                // Cambiar un rol por otro
                put "/"(controller: "userRole", action: "updateRole")

                // Eliminar un rol a un usuario
                delete "/"(controller: "userRole", action: "removeRole")
            }
            group "/sale", {
                get "/debtors/all"(controller: "sale", action: "listDebtors")
                get "/debtors/$username/details"(controller: "sale", action: "getDetailsByusername")
                post "/orders/pay-specific"(controller: "sale", action: "paySingleSale")
                post "/orders/pay-dish"(controller: "sale", action: "paySingleDish")
                post "/orders/pay-all-user"(controller: "sale", action: "payAllSalesForUser")
                get "/date/$userId"(controller: "sale", action: "getUserSalesByDateRange")
                post "/UserExpensesChart"(controller: "sale", action: "getUserSpendingChart")
                get "/pending/$userId"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 1
                }
                get "/payed/$userId"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 2
                }
                get "/all/$userId"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 3
                }
                get "/$uuid"(controller: "sale", action: "getOneSaleInfo")  
            }
            group "/order", {
                post "/newOrder"(controller: "ordersModule", action: "newOrder")
                get "/listOrders"(controller: "ordersModule", action: "listOrders")
                group "/$uuidOrder", {
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
            group "/shoppingCart", {
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
            group "/review", {
                get "/list"(controller: "review", action: "listReviews")
                get "/stats/$dishId"(controller: "review", action: "reviewsWithStats")
                post "/new"(controller: "review", action: "createReview")
                group "/$uuid", {
                    delete "/delete"(controller: "review", action: "deleteReview")
                    patch "/edit"(controller: "review", action: "editReview")
                }
            }

            group "/schedule", {
                get "/"(controller: "schedule", action: "index")
                post "/"(controller: "schedule", action: "save")
                get "/is-open"(controller: "schedule", action: "isOpen")
                delete "/$id"(controller: "schedule", action: "delete")
                get "/$id"(controller: "schedule", action: "show")
            }
        }
        "/"(controller: 'application', action:'index')
        "401"(controller: "application", action: "unauthorized")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}