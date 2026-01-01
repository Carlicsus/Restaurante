
export interface Cart {
  id: number
  status: string
  dateCreated: string
  lastUpdated: string
  user: User
  dishes: Dish[]
}

interface User {
  uuid: number
  username: string
}

interface Dish {
  uuid: string
  quantityDish: number
  unitPrice: number
  dish: Dish2
}

interface Dish2 {
  uuid: string
  name: string
}