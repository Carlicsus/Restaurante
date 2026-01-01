export interface Payment {
}

export interface Sale {
  id: number;
  uuid: string;
  total: number;
  status: 'Pending' | 'Paid';
  dateCreated: string;
  lastUpdated: string;
  customerOrderId: number;
}

export interface Debtor {
  username: string;
  pendingOrdersCount: number;
  totalPendingAmount: number;
}

export interface DebtorsSummary {
  totalDebtors: number;
  totalDebtAmount: number;
}

export interface DebtorsResponse {
  success: boolean;
  data: {
    debtors: Debtor[];
    summary: DebtorsSummary;
  };
  message: string;
}

export interface OrderItem {
  dishName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderDetail {
  saleUuid: string;
  orderUuid: string;
  amount: number;
  orderStatus: string;
  dateCreated: string;
  daysPending: number;
  items: OrderItem[];
}

export interface DebtorDetail {
  user: {
    username: string;
  };
  pendingOrders: OrderDetail[];
  summary: {
    totalPendingOrders: number;
    totalPendingAmount: number;
    oldestOrderDate: string | null;
  };
}

export interface DebtorDetailResponse {
  success: boolean;
  data: DebtorDetail;
  message: string;
}

export interface PaymentResponse {
  success: boolean;
  message: string;
  data?: any;
}
