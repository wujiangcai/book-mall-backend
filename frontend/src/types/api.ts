export type Result<T> = {
  code: number
  message: string
  data: T
  timestamp: number
}

export type PageResult<T> = {
  total: number
  list: T[]
  page: number
  size: number
}

import type { OrderStatusValue, RoleValue } from './enums'

export type AuthResponse = {
  token: string
  userId: number
  username: string
  role: RoleValue
}

export type UserInfoResponse = {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
}

export type BookListItem = {
  id: number
  bookName: string
  author?: string
  publisher?: string
  isbn?: string
  categoryId: number
  categoryName?: string
  price: number
  stock: number
  coverImage?: string
}

export type BookDetail = {
  id: number
  bookName: string
  author?: string
  publisher?: string
  isbn?: string
  categoryId: number
  categoryName?: string
  price: number
  stock: number
  coverImage?: string
  description?: string
}

export type CategoryTreeItem = {
  id: number
  categoryName: string
  parentId: number
  sortOrder: number
  status: number
  children?: CategoryTreeItem[]
}

export type CartItem = {
  id: number
  bookId: number
  bookName: string
  price: number
  quantity: number
  stock: number
  coverImage?: string
  totalPrice: number
}

export type Address = {
  id: number
  receiverName: string
  phone: string
  province?: string
  city?: string
  district?: string
  detailAddress: string
  isDefault: number
}

export type OrderItem = {
  bookId: number
  bookName: string
  price: number
  quantity: number
  totalPrice: number
}

export type OrderCreateResponse = {
  orderId: number
  orderNo: string
  totalAmount: number
  status: OrderStatusValue
  createTime: string
  address: {
    receiverName: string
    phone: string
    fullAddress: string
  }
  items: OrderItem[]
}

export type OrderListItem = {
  orderId: number
  orderNo: string
  totalAmount: number
  status: OrderStatusValue
  createTime: string
  itemSummary: string
}

export type OrderDetail = {
  orderId: number
  orderNo: string
  totalAmount: number
  status: OrderStatusValue
  createTime: string
  payTime?: string
  address: {
    receiverName: string
    phone: string
    fullAddress: string
  }
  items: OrderItem[]
}

export type Banner = {
  id: number
  imageUrl: string
  linkUrl?: string
  sortOrder: number
  status: number
  createTime?: string
}

export type AdminUserListItem = {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  role: RoleValue
  status: number
  createTime?: string
}

export type AdminUserDetail = {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  role: RoleValue
  status: number
  createTime?: string
  updateTime?: string
}

export type AdminOrderListItem = {
  id: number
  orderNo: string
  totalAmount: number
  status: OrderStatusValue
  userId: number
  username?: string
  createTime?: string
}

export type AdminOrderDetail = {
  id: number
  orderNo: string
  totalAmount: number
  status: OrderStatusValue
  userId: number
  addressId?: number
  username?: string
  createTime?: string
  payTime?: string
  shipTime?: string
  address?: {
    receiverName: string
    phone: string
    fullAddress: string
  }
  items?: OrderItem[]
}

export type AdminBookListItem = {
  id: number
  bookName: string
  author?: string
  publisher?: string
  isbn?: string
  categoryId: number
  categoryName?: string
  price: number
  stock: number
  coverImage?: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

export type AdminDashboardOverview = {
  customerCount: number
  adminCount: number
  totalBooks: number
  onShelfBooks: number
  totalOrders: number
  pendingShipOrders: number
  lowStockBooks: number
  activeBanners: number
  todayOrders: number
  totalRevenue: number
  todayRevenue: number
}

export type AdminDashboardTrendPoint = {
  day: string
  orderCount: number
  revenue: number
}

export type AdminDashboardOrderStatus = {
  status: number
  label: string
  count: number
}

export type AdminDashboardCategoryStat = {
  categoryId: number
  categoryName: string
  bookCount: number
}

export type AdminDashboardHotBook = {
  bookId: number
  bookName: string
  author?: string
  coverImage?: string
  salesCount: number
  salesAmount: number
}

export type AdminDashboardLowStockBook = {
  id: number
  bookName: string
  author?: string
  stock: number
  status: number
}

export type AdminDashboardRecentOrder = {
  id: number
  orderNo: string
  username?: string
  totalAmount: number
  status: number
  createTime: string
}

export type AdminDashboard = {
  overview: AdminDashboardOverview
  orderTrend: AdminDashboardTrendPoint[]
  orderStatus: AdminDashboardOrderStatus[]
  categoryStats: AdminDashboardCategoryStat[]
  hotBooks: AdminDashboardHotBook[]
  lowStockBooks: AdminDashboardLowStockBook[]
  recentOrders: AdminDashboardRecentOrder[]
}

export type CategoryAdminItem = {
  id: number
  categoryName: string
  parentId: number
  sortOrder: number
  status: number
  createTime?: string
  updateTime?: string
}
