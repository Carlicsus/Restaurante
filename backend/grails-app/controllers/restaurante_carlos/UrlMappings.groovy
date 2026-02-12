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
                get "/paginate"(controller: "user", action: "paginateUsers")
                get "/me/photo"(controller: "user", action: "getMyPhoto") 
                post "/me/photo"(controller: "user", action: "uploadMyPhoto")
                patch "/requestChangeCrd"(controller: "user", action: "requestChangeCrd")
                group "/$uuid", {
                    constraints{
                        uuid(matches: /^[a-fA-F0-9]{32}/)
                    }
                    patch "/change-status/$status"(controller: "user", action: "changeStatus"){
                        constraints {
                            status inList:[
                                "active",
                                "deactivate",
                                "block",
                                "unlock"
                            ]
                        }
                    }
                    get "/info"(controller: "user", action: "getUserInfo")
                    patch "/changeCrd"(controller: "user", action: "changeUserCrd")
                    get "/photo"(controller: "user", action: "getUserPhoto")
                    post "/photo"(controller: "user", action: "uploadUserPhoto")
                }
            }

            group "/role", {

                get "/listAll"(controller: "role", action: "listAllRoles")
                post "/create"(controller: "role", action: "createNewRole")
                group "/$uuid", {
                    constraints{
                        uuid(matches: /^[a-fA-F0-9]{32}/)
                    }
                    get "/info"(controller: "role", action: "getRoleInfo")
                    delete "/delete"(controller: "role", action: "deleteRole")
                    patch "/changeAuthority"(controller: "role", action: "changeAuthority")
                }

            }

            group "/userRole", {
                get "/getRoles/$uuidUser"(controller: "userRole", action: "getRolesByUser"){
                    constraints{
                        uuidUser(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
                post "/assignRole/$uuidUser/$uuidRole"(controller: "userRole", action: "assignRole"){
                    constraints{
                        uuidUser(matches: /^[a-fA-F0-9]{32}/)
                        uuidRole(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
                delete "/removeRole/$uuidUser/$uuidRole"(controller: "userRole", action: "removeRole"){
                    constraints{
                        uuidUser(matches: /^[a-fA-F0-9]{32}/)
                        uuidRole(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
                patch "/changeRole/$uuidUser/$uuidRole/$uuidNewRole"(controller: "userRole", action: "changeRole"){
                    constraints{
                        uuidUser(matches: /^[a-fA-F0-9]{32}/)
                        uuidRole(matches: /^[a-fA-F0-9]{32}/)
                        uuidNewRole(matches: /^[a-fA-F0-9]{32}/)
                    }
                }
            }

            group "/schedule", {
                get "/listAllSchedules"(controller: "schedule", action: "listAllSchedules")
                group "/user/$uuidUser", {
                    get "/getUserSchedule"(controller: "schedule", action: "getScheduleInfo")
                    post "/createSchedule"(controller: "schedule", action: "createSchedule")
                    put "/updateSchedule"(controller: "schedule", action: "updateSchedule")
                    delete "/deleteSchedule"(controller: "schedule", action: "deleteSchedule")
                }
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
        }
        "/"(controller: 'application', action:'index')
        "401"(controller: "application", action: "unauthorized")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}