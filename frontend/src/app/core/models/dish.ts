export interface Menu {
  uuid: string
  name: string
  dishes: Dish[]
  status: number
  dateCreated: Date
  lastUpdated: Date
}

export interface Dish {
  id?: number
  uuid: string
  name: string
  description: string
  cost: number
  status: number
  availableDishes: number
  availableDate?: any
  dateCreated?: Date
  lastUpdated?: Date
  menuType?: string | Menu
  imageUrl?: string
}