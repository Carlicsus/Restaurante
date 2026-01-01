export interface Menu {
  uuid: string
  name: string
  dishes: Dish[]
}

export interface Dish {
  id:number
  uuid: string
  name: string
  description: string
  cost: number
  status: number
  availableDishes: number
  availableDate: any
}