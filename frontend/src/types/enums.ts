export const UserRole = {
  User: 0,
  Admin: 1,
  Deleted: -1,
} as const

export type RoleValue = (typeof UserRole)[keyof typeof UserRole]

export const OrderStatus = {
  UNPAID: 0,
  PAID: 1,
  PENDING_SHIP: 2,
  SHIPPED: 3,
  COMPLETED: 4,
  CANCELLED: 5,
} as const

export type OrderStatusValue = (typeof OrderStatus)[keyof typeof OrderStatus]
