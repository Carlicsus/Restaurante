export interface Order {
  uuid: string
  status: string
  dateCreated: string
  lastUpdated: string
  user: User
  items: Item[]
}

interface User {
  uuid: number
  username: string
}

interface Item {
  uuid: string
  quantityDish: number
  unitPrice: number
  dish: Dish
}

interface Dish {
  uuid: string
  name: string
}
