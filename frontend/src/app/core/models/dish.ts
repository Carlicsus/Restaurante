export interface Dish {
    uuid: string
    name: string
    description: string
    cost: number
    status: number
    availableDishes: number
    availableDate: any
    menuType: Menu
}

export interface Menu {
    name: string
    status: number
    uuid: string
}