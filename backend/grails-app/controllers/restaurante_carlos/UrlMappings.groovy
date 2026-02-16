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
                get "/ranking/rating"(controller: "platillo", action: "dishRankingByRating")
                get "/ranking/topselling"(controller: "platillo", action: "topSellingDishes")
                get "/chart-top-dishes"(controller: "platillo", action: "topDishesChart")
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
                    // Imagen: subida y borrado (protegidos) 
                    post "/upload-image"(controller: "platillo", action: "uploadDishImage")
                    delete "/image"(controller: "platillo", action: "deleteDishImage")
                    get "/image"(controller: "platillo", action: "getDishImage")
                }
            }

            // Imagen: descarga pública
            group "/images", {
                get "/$fileName"(controller: "platillo", action: "downloadDishImage")
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

                get  "/$id/photo"(controller: "user", action: "getUserPhoto")
                post "/$id/photo"(controller: "user", action: "uploadUserPhoto")

                get "/info/$username"(controller: "user", action: "getUserInfo")
                patch "/$id/password"(controller: "user", action: "changeUserPassword")

            }

            group "/role", {

                get "/"(controller: "role", action: "index")
                get "/$id"(controller: "role", action: "show")

                post "/"(controller: "role", action: "save")
                put "/$id"(controller: "role", action: "update")
                delete "/$id"(controller: "role", action: "delete")
            }

            group "/user-role", {

                get "/user/$userId"(controller: "userRole", action: "getRolesByUser")

                post "/"(controller: "userRole", action: "assignRole")

                put "/"(controller: "userRole", action: "updateRole")

                delete "/"(controller: "userRole", action: "removeRole")
            }
            group "/sale", {
                get "/debtors/all"(controller: "sale", action: "listDebtors")
                get "/debtors/$userUuid/details"(controller: "sale", action: "getDetailsByUser"){
                    constraints {
                        userUuid(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
                post "/orders/pay-specific"(controller: "sale", action: "paySingleSale")
                post "/orders/pay-dish"(controller: "sale", action: "paySingleDish")
                post "/orders/pay-all-user"(controller: "sale", action: "payAllSalesForUser")
                get "/date"(controller: "sale", action: "getUserSalesByDateRange")
                post "/user-expenses-chart"(controller: "sale", action: "getUserSpendingChart")
                get "/pending"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 1
                }
                get "/payed"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 2
                }
                get "/all"(controller: "sale", action: "getSalesByUser") {
                    typeSale = 3
                }
                get "/$saleUuid"(controller: "sale", action: "getOneSaleInfo"){
                    constraints {
                        saleUuid(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
            }
            group "/order", {
                post "/newOrder"(controller: "ordersModule", action: "newOrder")
                get "/listOrders"(controller: "ordersModule", action: "listOrders")
                get "/listOrdersByUser/$userId"(controller: "ordersModule", action: "listOrdersByUser")
                get "/myOrders"(controller: "ordersModule", action: "getMyOrders")
                get "/rejections"(controller: "ordersModule", action: "listRejections")
                group "/$uuidOrder", {
                    get "/info"(controller: "ordersModule", action: "orderInfo")
                    group "/edit/$uuidDish",{
                        patch "/dish"(controller: "ordersModule", action: "editOrder")
                        patch "/reject"(controller: "ordersModule", action: "rejectDish")
                    }
                    patch "/edit"(controller: "ordersModule", action: "editOrder")
                    patch "/cancel"(controller: "ordersModule", action: "editOrderStatus") {status = "Cancelled"}
                    /*patch "'/cancel/comment'"(controller: "ordersModule", action: "cancelOrder") {
                        status = "Cancelled"
                    }*/
                    patch "/prepare"(controller: "ordersModule", action: "editOrderStatus") {
                        status = "Preparing"
                    }
                    patch "/finish"(controller: "ordersModule", action: "editOrderStatus") {
                        status = "Finished"
                    }
                    patch "/queue"(controller: "ordersModule", action: "editOrderStatus") {
                        status = "Queue"
                    }
                    group "/rejection/$rejectionUuid", {
                        get "/info"(controller: "ordersModule", action: "rejectionInfo")
                        patch "/approve"(controller: "ordersModule", action: "approveRejection")
                        patch "/cancel"(controller: "ordersModule", action: "cancelRejection")
                    }
                }
            }
            group "/shoppingCart", {
                get "/list"(controller: "shoppingCart", action: "listOrderShoppingCart")
                get "/byUser" (controller: "shoppingCart", action: "getCartByUser")
                post "/new"(controller: "shoppingCart", action: "newOrderShoppingCart")
                group "/$uuidSC", {
                    get "/info"(controller: "shoppingCart", action: "shoppingCartInfo")
                    post "/addItem"(controller: "shoppingCart", action: "addItemShoppingCart")
                    delete "/deleteItem/$uuidItem"(controller: "shoppingCart", action: "deleteItemShoppingCart")
                    put "/addNumberDish/$uuidItem"(controller:"shoppingCart", action:"addNumberDish")
                    put "/restNumberDish/$uuidItem"(controller:"shoppingCart", action:"restNumberDish")
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
                get "/stats/$dishUuid"(controller: "review", action: "statisticsDish"){
                    constraints {
                        dishUuid(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
                post "/new"(controller: "review", action: "createReview")
                group "/$reviewUuid", {
                    delete "/delete"(controller: "review", action: "statusReview"){ status = 2 }
                    patch "/deactivate"(controller: "review", action: "statusReview"){ status = 0 }
                    patch "/activate"(controller: "review", action: "statusReview"){ status = 1 }
                    patch "/edit"(controller: "review", action: "editReview")
                    constraints {
                        reviewUuid(matches: /^[a-fA-F0-9]{32}/)
                    }
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